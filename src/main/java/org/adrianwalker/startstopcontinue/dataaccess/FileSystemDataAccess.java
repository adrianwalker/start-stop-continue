package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.model.Column;

public abstract class FileSystemDataAccess implements DataAccess {

  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());
  
  private final Path path;

  public FileSystemDataAccess(final Path path) {
    this.path = path;
  }

  @Override
  public void createBoard(final Board board) {

    writeBoard(board);
  }

  @Override
  public void createNote(final UUID boardId, final Column column, final Note note) {

    writeNote(notePath(boardId, column, note.getId()), note);
  }

  @Override
  public Board readBoard(final UUID boardId) {

    return new Board().setId(boardId)
      .setStarts(readNotes(columnPath(boardId, Column.START)))
      .setStops(readNotes(columnPath(boardId, Column.STOP)))
      .setContinues(readNotes(columnPath(boardId, Column.CONTINUE)));
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

  private void writeBoard(final Board board) throws RuntimeException {

    try {
      Files.createDirectories(boardPath(board.getId()));

      for (Column column : Column.values()) {
        Files.createDirectories(columnPath(board.getId(), column));
      }

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    board.getStarts().forEach(note -> writeNote(notePath(board.getId(), Column.START, note.getId()), note));
    board.getContinues().forEach(note -> writeNote(notePath(board.getId(), Column.CONTINUE, note.getId()), note));
    board.getStops().forEach(note -> writeNote(notePath(board.getId(), Column.STOP, note.getId()), note));
  }

  private List<Note> readNotes(final Path columnPath) {

    Stream<Path> notePaths;
    try {
      notePaths = Files.list(columnPath);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return notePaths
      .map(notePath -> readNote(notePath))
      .sorted(NOTE_COMPARATOR)
      .collect(toList());
  }

  protected abstract void writeNote(final Path notePath, final Note note);

  protected abstract Note readNote(final Path notePath);
}
