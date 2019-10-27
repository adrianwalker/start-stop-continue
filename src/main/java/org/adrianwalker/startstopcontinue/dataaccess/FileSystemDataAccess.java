package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import static java.lang.String.format;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.model.NoteType;

public final class FileSystemDataAccess implements DataAccess {

  private final Path path;

  public FileSystemDataAccess(final Path path) {
    this.path = path;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public void create(final Board board) {

    writeBoard(board);
  }

  @Override
  public void create(final UUID boardId, final Note note) {

    writeNote(boardId, note);
  }

  @Override
  public Board read(final UUID boardID) {

    return readBoard(boardID);
  }

  @Override
  public Note read(final UUID boardId, final Note note) {

    return readNote(getNotePath(boardId, note));
  }

  @Override
  public void update(final Board board) {

    writeBoard(board);
  }

  @Override
  public void update(final UUID boardId, final Note note) {

    writeNote(boardId, note);
  }

  @Override
  public void delete(final UUID boardId) {

    deleteBoard(boardId);
  }

  @Override
  public void delete(final UUID boardId, final Note note) {

    deleteNote(boardId, note);
  }

  private Path getBoardPath(final UUID boardId) {

    return getPath().resolve(boardId.toString());
  }

  private Path getNotePath(final UUID boardId, final Note note) {

    return getBoardPath(boardId)
      .resolve(format("%s-%s", note.getType().name(), note.getId()));
  }

  private void writeBoard(final Board board) throws RuntimeException {

    Path boardPath = getBoardPath(board.getId());

    try {
      Files.createDirectories(boardPath);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    board.getStarts().forEach(note -> writeNote(board.getId(), note));
    board.getStops().forEach(note -> writeNote(board.getId(), note));
    board.getContinues().forEach(note -> writeNote(board.getId(), note));
  }

  private void writeNote(final UUID boardId, final Note note) throws RuntimeException {

    Path notePath = getNotePath(boardId, note);

    try {
      Files.writeString(notePath, note.getText());
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Board readBoard(final UUID boardId) {

    Set<Note> notes = readNotes(getBoardPath(boardId));

    Predicate<Note> startFilter = note -> note.getType().equals(NoteType.START);
    Predicate<Note> stopFilter = note -> note.getType().equals(NoteType.STOP);
    Predicate<Note> continueFilter = note -> note.getType().equals(NoteType.CONTINUE);

    Stream<Note> startStream = notes.stream().filter(startFilter);
    Stream<Note> stopStream = notes.stream().filter(stopFilter);
    Stream<Note> continueStream = notes.stream().filter(continueFilter);

    return new Board().setId(boardId)
      .setStarts(startStream.collect(toSet()))
      .setStops(stopStream.collect(toSet()))
      .setContinues(continueStream.collect(toSet()));
  }

  private Set<Note> readNotes(final Path boardPath) {

    try {
      return Files.list(boardPath)
        .map(notePath -> readNote(notePath))
        .collect(toSet());

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private Note readNote(final Path notePath) {

    String filename = notePath.getFileName().toString();
    NoteType type = NoteType.valueOf(filename.substring(0, filename.indexOf('-')));
    UUID id = UUID.fromString(filename.substring(filename.indexOf('-') + 1));

    try {
      return new Note()
        .setId(id)
        .setType(type)
        .setText(Files.readString(notePath));

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void deleteBoard(final UUID boardId) {

    try {
      Files.list(getBoardPath(boardId)).forEach(f -> {
        try {
          Files.deleteIfExists(f);
        } catch (final IOException ioe) {
          throw new RuntimeException(ioe);
        }
      });

      Files.deleteIfExists(getBoardPath(boardId));

    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void deleteNote(final UUID boardId, final Note note) {

    try {
      Files.deleteIfExists(getNotePath(boardId, note));
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
