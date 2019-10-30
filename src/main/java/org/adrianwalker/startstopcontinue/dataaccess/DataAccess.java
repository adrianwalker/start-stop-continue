package org.adrianwalker.startstopcontinue.dataaccess;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public interface DataAccess {
  
  void createBoard(Board board);
  void createNote(UUID boardId, Column column, Note note);
  Board readBoard(UUID boardId);
  void updateNote(UUID boardId, Column column, Note note);
  void deleteNote(UUID boardId, Column column, UUID noteId);
}
