package org.adrianwalker.startstopcontinue.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.adrianwalker.startstopcontinue.model.Note;

public final class JsonFileSystemDataAccess extends FileSystemDataAccess {

  private final ObjectMapper objectMapper;

  public JsonFileSystemDataAccess(final Path path) {

    super(path);

    objectMapper = new ObjectMapper();
  }

  @Override
  protected void writeNote(final Path notePath, final Note note) {

    try {
      objectMapper.writeValue(notePath.toFile(), note);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  protected Note readNote(final Path notePath) {

    try {
      return objectMapper.readValue(notePath.toFile(), Note.class);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
