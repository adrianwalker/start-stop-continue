package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import static java.lang.String.format;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.model.NoteType;

public final class FileSystemDataAccess implements DataAccess {

  private static final Comparator<Path> CRAETED_TIME_COMPARATOR = (p1, p2) -> {
    try {
      FileTime t1 = Files.readAttributes(p1, BasicFileAttributes.class).creationTime();
      FileTime t2 = Files.readAttributes(p2, BasicFileAttributes.class).creationTime();
      return t2.compareTo(t1);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  };

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

    Map<NoteType, List<Note>> notes = readNotes(getBoardPath(boardId));

    return new Board().setId(boardId)
      .setStarts(notes.getOrDefault(NoteType.START, new ArrayList<>()))
      .setStops(notes.getOrDefault(NoteType.STOP, new ArrayList<>()))
      .setContinues(notes.getOrDefault(NoteType.CONTINUE, new ArrayList<>()));
  }

  private Map<NoteType, List<Note>> readNotes(final Path boardPath) {

    Stream<Path> notes;

    try {
      notes = Files.list(boardPath);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return notes.sorted(CRAETED_TIME_COMPARATOR)
      .map(notePath -> readNote(notePath))
      .collect(Collectors.groupingBy(Note::getType, toList()));
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
