package org.adrianwalker.startstopcontinue.service;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Note;

public final class Service {

  private final DataAccess dataAccess;

  public Service(final DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public final void create(final Board board) {
    
    dataAccess.create(board);
  }

  public final void create(final UUID boardId, final Note note) {
    
    dataAccess.create(boardId, note);
  }

  public final Board read(final UUID boardId) {
    
    return dataAccess.read(boardId);
  }

  public final Note read(final UUID boardId, final Note note) {
    
    return dataAccess.read(boardId, note);
  }

  public final void update(final Board board) {
    
    dataAccess.update(board);
  }

  public final void update(final UUID boardId, final Note note) {
    
    dataAccess.update(boardId, note);
  }

  public final void delete(final UUID boardId) {
    
    dataAccess.delete(boardId);
  }

  public final void delete(final UUID boardId, final Note note) {
    
    dataAccess.delete(boardId, note);
  }
}
