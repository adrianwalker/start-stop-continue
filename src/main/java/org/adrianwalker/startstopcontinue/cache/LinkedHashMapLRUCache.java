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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LinkedHashMapLRUCache.class);

  private static final float LOAD_FACTOR = 0.75f;
  private static final boolean ACCESS_ORDER = true;
  private static final Collector<Note, ?, Map<UUID, Note>> NOTE_COLLECTOR = toMap(Note::getId, note -> note);
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());

  private final Map<UUID, CacheEntry> cache;
  private final Function<UUID, Board> readThroughFunction;

  private static final class CacheEntry {

    private boolean locked;
    private Map<Column, Map<UUID, Note>> data;

    public CacheEntry() {

      this(false, new EnumMap<>(Column.class));
    }

    public CacheEntry(final boolean locked, final Map<Column, Map<UUID, Note>> data) {

      this.locked = locked;
      this.data = data;
    }

    public boolean isLocked() {

      return locked;
    }

    public CacheEntry setLocked(final boolean locked) {

      this.locked = locked;
      return this;
    }

    public Map<Column, Map<UUID, Note>> getData() {

      return data;
    }

    public CacheEntry setData(final Map<Column, Map<UUID, Note>> data) {

      this.data = data;
      return this;
    }
  }

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
    if (cacheHit) {

      board = fromCache(boardId);

    } else {

      board = readThroughFunction.apply(boardId);
      toCache(boardId, board);
    }

    LOGGER.info("boardId = {}, cacheHit = {}", boardId, cacheHit);

    int cacheSize = cache.size();
    LOGGER.info("cacheSize = {}", cacheSize);

    return board;
  }

  @Override
  public void lock(final UUID boardId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).setLocked(true);
  }

  @Override
  public void unlock(final UUID boardId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).setLocked(false);
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    return cache.get(boardId).getData().get(column).get(noteId);
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).getData().get(column).put(note.getId(), note);

    int cacheSize = cache.size();
    LOGGER.info("cacheSize = {}", cacheSize);
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {

    if (!cache.containsKey(boardId)) {
      read(boardId);
    }

    cache.get(boardId).getData().get(column).remove(noteId);
  }

  @Override
  public long size() {

    return cache.size();
  }

  @Override
  public void purge() {

    cache.clear();
  }

  private Board fromCache(final UUID boardId) {

    CacheEntry cacheEntry = cache.get(boardId);

    return new Board().setId(boardId)
      .setLocked(cacheEntry.isLocked())
      .setStarts(cacheEntry.getData().get(Column.START).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()))
      .setStops(cacheEntry.getData().get(Column.STOP).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()))
      .setContinues(cacheEntry.getData().get(Column.CONTINUE).values().stream()
        .sorted(NOTE_COMPARATOR).collect(toList()));
  }

  private void toCache(final UUID boardId, final Board board) {

    CacheEntry cacheEntry = cache.computeIfAbsent(
      boardId,
      ce -> new CacheEntry());

    cacheEntry.setLocked(board.isLocked());

    cacheEntry.getData().computeIfAbsent(Column.START, m -> new HashMap<>())
      .putAll(board.getStarts().stream().collect(NOTE_COLLECTOR));

    cacheEntry.getData().computeIfAbsent(Column.STOP, m -> new HashMap<>())
      .putAll(board.getStops().stream().collect(NOTE_COLLECTOR));

    cacheEntry.getData().computeIfAbsent(Column.CONTINUE, m -> new HashMap<>())
      .putAll(board.getContinues().stream().collect(NOTE_COLLECTOR));
  }
}
