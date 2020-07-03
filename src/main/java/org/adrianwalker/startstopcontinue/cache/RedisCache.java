package org.adrianwalker.startstopcontinue.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.StatefulRedisConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.slf4j.LoggerFactory;

public final class RedisCache implements Cache {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String LOCKED = "locked";
  private static final String FIELD_SEPERATOR = "/";
  private static final List<Note> EMPTY_NOTES = Collections.EMPTY_LIST;
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());
  private static final Collector<Map.Entry<String, String>, ?, Map<Column, List<Note>>> NOTE_COLLECTOR = toMap(
    entry -> Column.valueOf(fieldHead(entry.getKey())),
    entry -> List.of(readNote(entry.getValue())),
    (n1, n2) -> combineNotes(n1, n2));

  private final long expirySeconds;
  private final StatefulRedisConnection<String, String> redisConnection;
  private final Function<UUID, Board> readThroughFunction;

  public RedisCache(
    final long expirySeconds,
    final StatefulRedisConnection<String, String> redisConnection,
    final Function<UUID, Board> readThroughFunction) {

    this.expirySeconds = expirySeconds;
    this.redisConnection = redisConnection;
    this.readThroughFunction = readThroughFunction;
  }

  @Override
  public Board read(final UUID boardId) {

    Board board;

    boolean cacheHit = exists(boardId.toString());
    if (cacheHit) {

      board = fromCache(boardId);

    } else {

      board = readThroughFunction.apply(boardId);
      toCache(boardId, board);
    }

    LOGGER.info("boardId = {}, cacheHit = {}", boardId, cacheHit);

    return board;
  }

  @Override
  public void lock(final UUID boardId) {

    if (!exists(boardId.toString())) {
      read(boardId);
    }

    writeLock(boardId, true);
  }

  @Override
  public void unlock(final UUID boardId) {

    if (!exists(boardId.toString())) {
      read(boardId);
    }

    writeLock(boardId, false);
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {

    if (!exists(boardId.toString())) {
      read(boardId);
    }

    return readNote(hget(boardId.toString(), noteField(column, noteId)));
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {

    if (!exists(boardId.toString())) {
      read(boardId);
    }

    hset(boardId.toString(), noteField(column, note.getId()), writeNote(note));
    expire(boardId.toString(), expirySeconds);
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {

    if (!exists(boardId.toString())) {
      read(boardId);
    }

    hdel(boardId.toString(), noteField(column, noteId));
  }

  private static String fieldHead(final String noteField) {

    return noteField.substring(0, noteField.indexOf(FIELD_SEPERATOR));
  }

  private static String noteField(final Column column, final UUID noteId) {

    return column.name() + FIELD_SEPERATOR + noteId.toString();
  }

  private static List<Note> combineNotes(final List<Note>... noteLists) {

    List<Note> combined = new ArrayList<>();
    for (List<Note> noteList : noteLists) {
      combined.addAll(noteList);
    }

    return combined;
  }

  private void writeLock(final UUID boardId, final boolean locked) {

    hset(boardId.toString(), LOCKED, String.valueOf(locked));
  }

  private static Note readNote(final String value) {

    try {
      return OBJECT_MAPPER.readValue(value, Note.class);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private String writeNote(final Note note) {

    try {
      return OBJECT_MAPPER.writeValueAsString(note);
    } catch (final JsonProcessingException jpe) {
      throw new RuntimeException(jpe);
    }
  }

  private boolean exists(final String... keys) {

    return redisConnection.sync().exists(keys) > 0;
  }

  private void expire(final String key, final long seconds) {

    redisConnection.async().expire(key, seconds);
  }

  private Map<String, String> hgetAll(final String key) {

    return redisConnection.sync().hgetall(key);
  }

  private String hget(final String key, final String field) {

    return redisConnection.sync().hget(key, field);
  }

  private void hset(final String key, final String field, final String value) {

    redisConnection.async().hset(key, field, value);
  }

  private void hmset(final String key, final Map<String, String> hash) {

    redisConnection.async().hmset(key, hash);
  }

  private void hdel(final String key, final String... members) {

    redisConnection.async().hdel(key, members);
  }

  private Board fromCache(final UUID boardId) {

    Map<String, String> hash = hgetAll(boardId.toString());

    boolean locked = Boolean.valueOf(hash.remove(LOCKED));
    Map<Column, List<Note>> columns = hash.entrySet().stream().collect(NOTE_COLLECTOR);

    return new Board().setId(boardId)
      .setLocked(locked)
      .setStarts(columns.getOrDefault(Column.START, EMPTY_NOTES).stream()
        .sorted(NOTE_COMPARATOR)
        .collect(toList()))
      .setStops(columns.getOrDefault(Column.STOP, EMPTY_NOTES).stream()
        .sorted(NOTE_COMPARATOR)
        .collect(toList()))
      .setContinues(columns.getOrDefault(Column.CONTINUE, EMPTY_NOTES).stream()
        .sorted(NOTE_COMPARATOR)
        .collect(toList()));
  }

  private void toCache(final UUID boardId, final Board board) {

    Map<String, String> hash = new HashMap<>();

    String locked = String.valueOf(board.isLocked());
    hash.put(LOCKED, locked);

    hash.putAll(board.getStarts().stream()
      .collect(Collectors.toMap(note -> noteField(Column.START, note.getId()),
        note -> writeNote(note))));

    hash.putAll(board.getStops().stream()
      .collect(Collectors.toMap(note -> noteField(Column.STOP, note.getId()),
        note -> writeNote(note))));

    hash.putAll(board.getContinues().stream()
      .collect(Collectors.toMap(note -> noteField(Column.CONTINUE, note.getId()),
        note -> writeNote(note))));

    if (!hash.isEmpty()) {
      hmset(boardId.toString(), hash);
    }
  }
}
