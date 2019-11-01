package org.adrianwalker.startstopcontinue.service;

import static java.lang.Math.min;
import java.util.UUID;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public final class Service {

  private final DataAccess dataAccess;
  private final int maxNoteLength;

  public Service(final DataAccess dataAccess, final int maxNoteLength) {
    this.dataAccess = dataAccess;
    this.maxNoteLength = maxNoteLength;
  }

  public final void createBoard(final Board board) {

    dataAccess.createBoard(board);
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    truncateNoteText(note);

    dataAccess.createNote(boardId, column, note);
  }

  public final Board readBoard(final UUID boardId) {

    return dataAccess.readBoard(boardId);
  }

  public final void updateNote(final UUID boardId, final Column column, final Note note) {

    truncateNoteText(note);

    dataAccess.updateNote(boardId, column, note);
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    dataAccess.deleteNote(boardId, column, noteId);
  }

  private void truncateNoteText(final Note note) {

    if (maxNoteLength > 0) {
      String text = note.getText();
      note.setText(text.substring(0, min(text.length(), maxNoteLength)));
    }
  }
}
