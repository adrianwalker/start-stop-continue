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

public final class RedisCache implements Cache {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String FIELD_SEPERATOR = "/";
  private static final List<Note> EMPTY_NOTES = Collections.EMPTY_LIST;
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());
  private static final Collector<Map.Entry<String, String>, ?, Map<Column, List<Note>>> NOTE_COLLECTOR = toMap(
    entry -> Column.valueOf(fieldHead(entry.getKey())),
    entry -> List.of(readNote(entry.getValue())),
    (n1, n2) -> combineNotes(n1, n2));

  private final StatefulRedisConnection<String, String> redisConnection;

  public RedisCache(final StatefulRedisConnection<String, String> redisConnection) {

    this.redisConnection = redisConnection;
  }

  @Override
  public Board readThrough(final UUID boardId, final Function<UUID, Board> f) {

    Board board;

    if (exists(boardId.toString())) {

      board = fromCache(boardId);

    } else {

      board = f.apply(boardId);
      toCache(boardId, board);
    }

    return board;
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {

    return readNote(hget(boardId.toString(), noteField(column, noteId)));
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {

    hset(boardId.toString(), noteField(column, note.getId()), writeNote(note));
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {

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

  private static Note readNote(final String value) {

    try {
      return OBJECT_MAPPER.readValue(value, Note.class);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private boolean exists(final String... keys) {

    return redisConnection.sync().exists(keys) > 0;
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

  private String writeNote(final Note note) {

    try {
      return OBJECT_MAPPER.writeValueAsString(note);
    } catch (final JsonProcessingException jpe) {
      throw new RuntimeException(jpe);
    }
  }

  private Board fromCache(final UUID boardId) {

    Map<String, String> notes = hgetAll(boardId.toString());
    Map<Column, List<Note>> columns = notes.entrySet().stream().collect(NOTE_COLLECTOR);

    return new Board().setId(boardId)
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

    Map<String, String> notes = new HashMap<>();

    notes.putAll(board.getStarts().stream()
      .collect(Collectors.toMap(note -> noteField(Column.START, note.getId()),
        note -> writeNote(note))));

    notes.putAll(board.getStops().stream()
      .collect(Collectors.toMap(note -> noteField(Column.STOP, note.getId()),
        note -> writeNote(note))));

    notes.putAll(board.getContinues().stream()
      .collect(Collectors.toMap(note -> noteField(Column.CONTINUE, note.getId()),
        note -> writeNote(note))));

    if (!notes.isEmpty()) {
      hmset(boardId.toString(), notes);
    }
  }
}
