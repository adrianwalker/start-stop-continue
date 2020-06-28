package org.adrianwalker.startstopcontinue.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import static java.util.Collections.EMPTY_LIST;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.ws.rs.core.MediaType;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import static java.util.Map.entry;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class MinioDataAccess implements DataAccess {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String NAME_DELIMITER = "/";
  private static final String LOCK = "lock";
  private static final Comparator<Note> NOTE_COMPARATOR = (n1, n2) -> n1.getCreated().compareTo(n2.getCreated());
  private static final BinaryOperator<List<Note>> NOTE_MERGER = (l, r) -> {
    r.addAll(l);
    r.sort(NOTE_COMPARATOR);

    return r;
  };
  private static final Supplier<EnumMap<Column, List<Note>>> ENUM_MAP_SUPPLIER = () -> new EnumMap<>(Column.class);

  private final MinioClient minioClient;
  private final String bucketName;

  public MinioDataAccess(final MinioClient minioClient, final String bucketName) {

    this.minioClient = minioClient;
    this.bucketName = bucketName;

    try {
      boolean bucketExists = minioClient.bucketExists(bucketName);

      if (!bucketExists) {
        minioClient.makeBucket(bucketName);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createBoard(final UUID boardId) {
  }

  @Override
  public void lockBoard(UUID boardId) {

    writeLock(lockName(boardId));
  }

  @Override
  public void createNote(final UUID boardId, final Column column, final Note note) {

    writeNote(noteName(boardId, column, note.getId()), note);
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
  public void updateNote(final UUID boardId, final Column column, final Note note) {

    writeNote(noteName(boardId, column, note.getId()), note);
  }

  @Override
  public void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    try {
      minioClient.removeObject(this.bucketName, noteName(boardId, column, noteId));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String boardName(final UUID boardId) {

    return boardId.toString();
  }

  private String lockName(final UUID boardId) {

    return String.join(NAME_DELIMITER, boardName(boardId), LOCK);
  }

  private String columnName(final UUID boardId, final Column column) {

    return String.join(NAME_DELIMITER, boardName(boardId), column.name());
  }

  private String noteName(final UUID boardId, final Column column, UUID noteId) {

    return String.join(NAME_DELIMITER, columnName(boardId, column), noteId.toString());
  }

  private String objectName(Result<Item> result) {
    try {
      return result.get().objectName();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<Column, List<Note>> readNotes(final UUID boardId) {

    return Stream.of(Column.values()).parallel()
      .map(
        column -> entry(
          column,
          columnName(boardId, column))
      ).map(
        columnName -> entry(
          columnName.getKey(),
          minioClient.listObjects(this.bucketName, columnName.getValue(), true))
      ).map(
        columnResults -> entry(
          columnResults.getKey(),
          StreamSupport.stream(columnResults.getValue().spliterator(), true))
      ).map(
        columnResults -> entry(
          columnResults.getKey(),
          columnResults.getValue().map(result -> readNote(objectName(result))))
      ).collect(toMap(
        entry -> entry.getKey(),
        entry -> entry.getValue()
          .sorted(NOTE_COMPARATOR)
          .collect(toList()),
        NOTE_MERGER,
        ENUM_MAP_SUPPLIER));
  }

  private boolean locked(final UUID boardId) {

    try {
      minioClient.statObject(this.bucketName, lockName(boardId));
    } catch (final ErrorResponseException ere) {

      if (ere.errorResponse().errorCode() == ErrorCode.NO_SUCH_KEY) {
        return false;
      }

      if (ere.errorResponse().errorCode() == ErrorCode.NO_SUCH_OBJECT) {
        return false;
      }

      throw new RuntimeException(ere);

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  private void writeLock(final String name) {

    writeObject(name, Collections.EMPTY_MAP);
  }

  private void writeNote(final String name, final Note note) {

    writeObject(name, note);
  }

  private void writeObject(final String name, final Object obj) {

    try ( PipedInputStream pis = new PipedInputStream();//
        PipedOutputStream pos = new PipedOutputStream(pis)) {

      OBJECT_MAPPER.writeValue(pos, obj);
      minioClient.putObject(this.bucketName, name, pis, MediaType.APPLICATION_JSON);

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Note readNote(final String name) {

    try ( InputStream is = minioClient.getObject(this.bucketName, name)) {

      return OBJECT_MAPPER.readValue(is, Note.class);

    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
