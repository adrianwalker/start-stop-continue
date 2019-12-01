package org.adrianwalker.startstopcontinue.dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.adrianwalker.startstopcontinue.model.Note;

public final class JsonFileSystemDataAccess extends FileSystemDataAccess {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public JsonFileSystemDataAccess(final Path path) {

    super(path);
  }

  @Override
  protected void writeNote(final Path notePath, final Note note) {

    try {
      OBJECT_MAPPER.writeValue(notePath.toFile(), note);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  protected Note readNote(final Path notePath) {

    try {
      return OBJECT_MAPPER.readValue(notePath.toFile(), Note.class);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
