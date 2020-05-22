package org.adrianwalker.startstopcontinue.cache;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public interface Cache {

  Board read(UUID boardId);

  Note read(UUID boardId, Column column, UUID noteId);

  void write(UUID boardId, Column column, Note note);

  void delete(UUID boardId);
  
  void delete(UUID boardId, Column column, UUID noteId);
}
