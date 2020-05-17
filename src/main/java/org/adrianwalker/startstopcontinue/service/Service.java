package org.adrianwalker.startstopcontinue.service;

import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.cache.Cache;

public final class Service {

  private final DataAccess dataAccess;
  private final Cache cache;
  private final ExecutorService executor;
  private final int maxNoteLength;

  public Service(final DataAccess dataAccess, final Cache cache, final ExecutorService executor, final int maxNoteLength) {

    this.dataAccess = dataAccess;
    this.cache = cache;
    this.executor = executor;
    this.maxNoteLength = maxNoteLength;
  }

  public final Board createBoard() {

    Board board = new Board()
      .setId(UUID.randomUUID())
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>());

    dataAccess.createBoard(board);

    return board;
  }

  public final Board readBoard(final UUID boardId) {

    return cache.read(boardId);
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    note.setId(UUID.randomUUID())
      .setCreated(new Date())
      .setText(truncateNoteText(note.getText()));

    executor.execute(() -> {
      dataAccess.createNote(boardId, column, note);
      cache.write(boardId, column, note);
    });
  }

  public final void updateNote(final UUID boardId, final Column column, final Note data) {

    Note note = cache.read(boardId, column, data.getId())
      .setText(truncateNoteText(data.getText()));

    executor.execute(() -> {
      dataAccess.updateNote(boardId, column, note);
      cache.write(boardId, column, note);
    });
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    executor.execute(() -> {
      dataAccess.deleteNote(boardId, column, noteId);
      cache.delete(boardId, column, noteId);
    });
  }

  private String truncateNoteText(final String text) {

    if (maxNoteLength > 0) {
      return text.substring(0, min(text.length(), maxNoteLength));
    }

    return text;
  }
}
