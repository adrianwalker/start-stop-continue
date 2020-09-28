package org.adrianwalker.startstopcontinue.dataaccess;

import java.nio.file.Path;
import org.adrianwalker.startstopcontinue.model.Note;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.fromJson;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.toJson;

public final class JsonFileSystemDataAccess extends FileSystemDataAccess {

  public JsonFileSystemDataAccess(final Path path) {

    super(path);
  }

  @Override
  protected void writeNote(final Path notePath, final Note note) {

    toJson(notePath.toFile(), note);
  }

  @Override
  protected Note readNote(final Path notePath) {

    return fromJson(notePath.toFile(), Note.class);
  }
}
