package org.adrianwalker.startstopcontinue.cache;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.slf4j.LoggerFactory;

public final class LinkedHashMapLRUCache implements Cache {

  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LinkedHashMapLRUCache.class);

  private static final float LOAD_FACTOR = 0.75f;
  private static final boolean ACCESS_ORDER = true;
  private static final Collector<Note, ?, Map<UUID, Note>> NOTE_COLLECTOR = toMap(Note::getId, note -> note);
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());

  private final Map<UUID, Map<Column, Map<UUID, Note>>> cache;
  private final Function<UUID, Board> readThroughFunction;

  public LinkedHashMapLRUCache(final int cacheSize, final Function<UUID, Board> readThroughFunction) {

    cache = new LinkedHashMap(cacheSize + 1, LOAD_FACTOR, ACCESS_ORDER) {

      @Override
      public boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > cacheSize;
      }
    };

    this.readThroughFunction = readThroughFunction;
  }

  @Override
  public Board read(final UUID boardId) {

    Board board;

    boolean cacheHit = cache.containsKey(boardId);
    if (!cacheHit) {
      toCache(boardId, readThroughFunction.apply(boardId));
    }

    board = fromCache(boardId);

    LOGGER.info("boardId = {}, cacheHit = {}", boardId, cacheHit);

    int cacheSize = cache.size();
    LOGGER.info("cacheSize = {}", cacheSize);

    return board;
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    return cache.get(boardId).get(column).get(noteId);
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).get(column).put(note.getId(), note);

    int cacheSize = cache.size();
    LOGGER.info("cacheSize = {}", cacheSize);
  }

  @Override
  public void delete(final UUID boardId) {

    cache.remove(boardId);

    int cacheSize = cache.size();
    LOGGER.info("cacheSize = {}", cacheSize);
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).get(column).remove(noteId);
  }

  private Board fromCache(final UUID boardId) {

    Map<Column, Map<UUID, Note>> columns = cache.get(boardId);

    return new Board().setId(boardId)
      .setStarts(columns.get(Column.START).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()))
      .setStops(columns.get(Column.STOP).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()))
      .setContinues(columns.get(Column.CONTINUE).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()));
  }

  private void toCache(final UUID boardId, final Board board) {

    Map<Column, Map<UUID, Note>> columns = cache.computeIfAbsent(boardId, m -> new EnumMap<>(Column.class));

    columns.computeIfAbsent(Column.START, m -> new HashMap<>())
      .putAll(board.getStarts().stream().collect(NOTE_COLLECTOR));

    columns.computeIfAbsent(Column.STOP, m -> new HashMap<>())
      .putAll(board.getStops().stream().collect(NOTE_COLLECTOR));

    columns.computeIfAbsent(Column.CONTINUE, m -> new HashMap<>())
      .putAll(board.getContinues().stream().collect(NOTE_COLLECTOR));
  }
}
