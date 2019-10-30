package org.adrianwalker.startstopcontinue.service;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public final class Service {

  private final DataAccess dataAccess;

  public Service(final DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public final void createBoard(final Board board) {

    dataAccess.createBoard(board);
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    dataAccess.createNote(boardId, column, note);
  }

  public final Board readBoard(final UUID boardId) {

    return dataAccess.readBoard(boardId);
  }

  public final void updateNote(final UUID boardId, final Column column, final Note note) {

    dataAccess.updateNote(boardId, column, note);
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    dataAccess.deleteNote(boardId, column, noteId);
  }
}
