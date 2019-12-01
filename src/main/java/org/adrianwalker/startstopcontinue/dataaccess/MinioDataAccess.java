package org.adrianwalker.startstopcontinue.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.ws.rs.core.MediaType;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public final class MinioDataAccess implements DataAccess {

  private static final String BUCKET_NAME = "start-stop-continue";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String NAME_DELIMITER = "/";
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());

  private final MinioClient minioClient;

  public MinioDataAccess(final String endpoint, final int port, final String accessKey, final String secretKey) {

    try {
      minioClient = new MinioClient(endpoint, port, accessKey, secretKey);
      boolean bucketExists = minioClient.bucketExists(BUCKET_NAME);

      if (!bucketExists) {
        minioClient.makeBucket(BUCKET_NAME);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createBoard(final Board board) {

    writeBoard(board);
  }

  @Override
  public void createNote(final UUID boardId, final Column column, final Note note) {

    writeNote(noteName(boardId, column, note.getId()), note);
  }

  @Override
  public Board readBoard(final UUID boardId) {

    return new Board().setId(boardId)
      .setStarts(readNotes(columnName(boardId, Column.START)))
      .setStops(readNotes(columnName(boardId, Column.STOP)))
      .setContinues(readNotes(columnName(boardId, Column.CONTINUE)));
  }

  @Override
  public void updateNote(final UUID boardId, final Column column, final Note note) {

    writeNote(noteName(boardId, column, note.getId()), note);
  }

  @Override
  public void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    try {
      minioClient.removeObject(BUCKET_NAME, noteName(boardId, column, noteId));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String boardName(final UUID boardId) {

    return boardId.toString();
  }

  private String columnName(final UUID boardId, final Column column) {

    return String.join(NAME_DELIMITER, boardName(boardId), column.name());
  }

  private String noteName(final UUID boardId, final Column column, UUID noteId) {

    return String.join(NAME_DELIMITER, columnName(boardId, column), noteId.toString());
  }

  private void writeBoard(final Board board) {

    board.getStarts().forEach(note -> writeNote(noteName(board.getId(), Column.START, note.getId()), note));
    board.getContinues().forEach(note -> writeNote(noteName(board.getId(), Column.CONTINUE, note.getId()), note));
    board.getStops().forEach(note -> writeNote(noteName(board.getId(), Column.STOP, note.getId()), note));
  }

  private List<Note> readNotes(final String columnName) {

    Stream<Result<Item>> results;
    results = StreamSupport
      .stream(minioClient
        .listObjects(BUCKET_NAME, columnName, true)
        .spliterator(), true);

    Stream<Note> notes = results.map(result -> {
      try {
        return readNote(result.get().objectName());
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });

    return notes
      .sorted(NOTE_COMPARATOR)
      .collect(toList());
  }

  private void writeNote(final String name, final Note note) {

    try ( PipedInputStream pis = new PipedInputStream();//
        PipedOutputStream pos = new PipedOutputStream(pis)) {

      OBJECT_MAPPER.writeValue(pos, note);
      minioClient.putObject(BUCKET_NAME, name, pis, MediaType.APPLICATION_JSON);

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Note readNote(final String name) {

    try ( InputStream is = minioClient.getObject(BUCKET_NAME, name)) {

      return OBJECT_MAPPER.readValue(is, Note.class);

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
