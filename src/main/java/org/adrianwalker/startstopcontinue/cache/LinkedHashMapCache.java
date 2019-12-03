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

public final class LinkedHashMapCache implements Cache {

  private static final float LOAD_FACTOR = 0.75f;
  private static final boolean ACCESS_ORDER = true;
  private static final Collector<Note, ?, Map<UUID, Note>> NOTE_COLLECTOR = toMap(Note::getId, note -> note);
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());

  private final Map<UUID, Map<Column, Map<UUID, Note>>> cache;

  public LinkedHashMapCache(final int cacheSize) {

    cache = new LinkedHashMap(cacheSize + 1, LOAD_FACTOR, ACCESS_ORDER) {

      @Override
      public boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > cacheSize;
      }
    };
  }

  @Override
  public Board readThrough(final UUID boardId, final Function<UUID, Board> f) {

    Board board;

    if (cache.containsKey(boardId)) {

      board = fromCache(boardId);

    } else {

      board = f.apply(boardId);
      toCache(boardId, board);
    }

    return board;
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {

    return cache.get(boardId).get(column).get(noteId);
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {

    cache.get(boardId).get(column).put(note.getId(), note);
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {

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