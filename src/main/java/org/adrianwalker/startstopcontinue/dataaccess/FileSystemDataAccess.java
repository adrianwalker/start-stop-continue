package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.util.Collections.EMPTY_LIST;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.model.Column;

public abstract class FileSystemDataAccess implements DataAccess {

  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());
  private static final BinaryOperator<List<Note>> NOTE_MERGER = (l, r) -> {
    r.addAll(l);
    r.sort(NOTE_COMPARATOR);

    return r;
  };
  private static final Supplier<EnumMap<Column, List<Note>>> ENUM_MAP_SUPPLIER = () -> new EnumMap<>(Column.class);
  private static final String LOCK = "lock";

  private final Path path;

  public FileSystemDataAccess(final Path path) {
    this.path = path;
  }

  @Override
  public void createBoard(final UUID boardId) {

    writeBoard(boardId);
  }

  @Override
  public Board readBoard(final UUID boardId) {

    boolean locked = locked(boardId);
    Map<Column, List<Note>> columnNotes = readNotes(boardId);

    return new Board().setId(boardId)
      .setLocked(locked)
      .setStarts(columnNotes.getOrDefault(Column.START, EMPTY_LIST))
      .setStops(columnNotes.getOrDefault(Column.STOP, EMPTY_LIST))
      .setContinues(columnNotes.getOrDefault(Column.CONTINUE, EMPTY_LIST));
  }

  @Override
  public void lockBoard(final UUID boardId) {

    writeLock(boardId);
  }

  @Override
  public void unlockBoard(final UUID boardId) {

    deleteLock(boardId);
  }

  @Override
  public void createNote(final UUID boardId, final Column column, final Note note) {

    writeNote(notePath(boardId, column, note.getId()), note);
  }

  @Override
  public void updateNote(final UUID boardId, final Column column, final Note note) {

    writeNote(notePath(boardId, column, note.getId()), note);
  }

  @Override
  public void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    try {
      Files.delete(notePath(boardId, column, noteId));
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Path boardPath(final UUID boardId) {

    return path.resolve(boardId.toString());
  }

  private Path columnPath(final UUID boardId, final Column column) {

    return boardPath(boardId).resolve(column.name());
  }

  protected final Path notePath(final UUID boardId, final Column column, UUID noteId) {

    return columnPath(boardId, column)
      .resolve(noteId.toString());
  }

  private void writeBoard(final UUID boardId) {

    try {
      Files.createDirectories(boardPath(boardId));

      for (Column column : Column.values()) {
        Files.createDirectories(columnPath(boardId, column));
      }

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Stream<Path> filesList(final Path path) {

    try ( Stream<Path> stream = Files.list(path)) {
      return stream.collect(toList()).stream().parallel();
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Map<Column, List<Note>> readNotes(final UUID boardId) {

    return Stream.of(Column.values()).parallel()
      .map(
        column -> entry(
          column,
          columnPath(boardId, column))
      ).map(
        columnPath -> entry(
          columnPath.getKey(),
          filesList(columnPath.getValue()))
      ).map(
        columnNotePaths -> entry(
          columnNotePaths.getKey(),
          columnNotePaths.getValue()
            .map(notePath -> readNote(notePath)))
      ).collect(toMap(
        entry -> entry.getKey(),
        entry -> entry.getValue()
          .sorted(NOTE_COMPARATOR)
          .collect(toList()),
        NOTE_MERGER,
        ENUM_MAP_SUPPLIER));
  }

  private boolean locked(final UUID boardId) {

    Path lockPath = boardPath(boardId).resolve(LOCK);

    return Files.exists(lockPath) && Files.isRegularFile(lockPath);
  }

  private void writeLock(final UUID boardId) {

    Path lockPath = boardPath(boardId).resolve(LOCK);

    try {
      Files.createFile(lockPath);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void deleteLock(final UUID boardId) {

    Path lockPath = boardPath(boardId).resolve(LOCK);

    try {
      Files.deleteIfExists(lockPath);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  protected abstract void writeNote(final Path notePath, final Note note);

  protected abstract Note readNote(final Path notePath);
}
