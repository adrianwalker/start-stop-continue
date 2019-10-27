package org.adrianwalker.startstopcontinue.dataaccess;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;

public interface DataAccess {
  
  void create(Board board);
  void create(UUID boardId, Note note);
  
  Board read(UUID boardId);
  Note read(UUID boardId, Note note);
  
  void update(Board board);
  void update(UUID boardId, Note note);
  
  void delete(UUID boardId);
  void delete(UUID boardId, Note note);
}
