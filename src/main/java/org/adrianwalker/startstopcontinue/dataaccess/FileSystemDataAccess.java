package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.model.Column;

public final class FileSystemDataAccess implements DataAccess {

  private final Path path;

  public FileSystemDataAccess(final Path path) {
    this.path = path;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public void createBoard(final Board board) {

    writeBoard(board);
  }

  @Override
  public void createNote(final UUID boardId, final Column column, final Note note) {

    writeNote(boardId, column, note);
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

    writeNote(boardId, column, note);
  }

  @Override
  public void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    try {
      Files.deleteIfExists(notePath(boardId, column, noteId));
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Path boardPath(final UUID boardId) {

    return getPath().resolve(boardId.toString());
  }

  private Path columnPath(final UUID boardId, final Column column) {

    return boardPath(boardId).resolve(column.name());
  }

  private Path notePath(final UUID boardId, final Column column, UUID noteId) {

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

    board.getStarts().forEach(note -> writeNote(board.getId(), Column.START, note));
    board.getStops().forEach(note -> writeNote(board.getId(), Column.STOP, note));
    board.getContinues().forEach(note -> writeNote(board.getId(), Column.CONTINUE, note));
  }

  private void writeNote(final UUID boardId, final Column column, final Note note) throws RuntimeException {

    Path notePath = notePath(boardId, column, note.getId());

    try {
      Files.writeString(notePath, note.getText());
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
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
      .collect(toList());
  }

  private Note readNote(final Path notePath) {

    String filename = notePath.getFileName().toString();
    UUID id = UUID.fromString(filename);

    try {
      return new Note()
        .setId(id)
        .setText(Files.readString(notePath));

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
