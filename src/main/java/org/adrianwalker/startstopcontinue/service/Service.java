package org.adrianwalker.startstopcontinue.service;

import static java.lang.Math.min;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import static org.adrianwalker.startstopcontinue.Monitoring.logMemoryUsage;
import static org.adrianwalker.startstopcontinue.Monitoring.logOpenFileHandles;
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

  public final UUID createBoard() {

    UUID boardId = UUID.randomUUID();
    dataAccess.createBoard(boardId);

    logMemoryUsage();
    logOpenFileHandles();

    return boardId;
  }

  public final Board readBoard(final UUID boardId) {

    Board board = cache.read(boardId);

    logMemoryUsage();
    logOpenFileHandles();

    return board;
  }

  public final void lockBoard(final UUID boardId) {

    checkLock(boardId);

    executor.execute(() -> {
      dataAccess.lockBoard(boardId);
      cache.lock(boardId);
    });

    logMemoryUsage();
    logOpenFileHandles();
  }

  public final void unlockBoard(final UUID boardId) {

    executor.submit(() -> {
      dataAccess.unlockBoard(boardId);
      cache.unlock(boardId);
    });

    logMemoryUsage();
    logOpenFileHandles();
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    checkLock(boardId);

    note.setId(UUID.randomUUID())
      .setCreated(new Date())
      .setText(truncateNoteText(note.getText()));

    executor.execute(() -> {
      dataAccess.createNote(boardId, column, note);
      cache.write(boardId, column, note);
    });

    logMemoryUsage();
    logOpenFileHandles();
  }

  public final void updateNote(final UUID boardId, final Column column, final Note data) {

    checkLock(boardId);

    Note note = cache.read(boardId, column, data.getId())
      .setText(truncateNoteText(data.getText()));

    executor.execute(() -> {
      dataAccess.updateNote(boardId, column, note);
      cache.write(boardId, column, note);
    });

    logMemoryUsage();
    logOpenFileHandles();
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    checkLock(boardId);

    executor.execute(() -> {
      dataAccess.deleteNote(boardId, column, noteId);
      cache.delete(boardId, column, noteId);
    });

    logMemoryUsage();
    logOpenFileHandles();
  }

  private String truncateNoteText(final String text) {

    if (maxNoteLength > 0) {
      return text.substring(0, min(text.length(), maxNoteLength));
    }

    return text;
  }

  private void checkLock(final UUID boardId) {

    boolean locked = readBoard(boardId).isLocked();

    if (locked) {
      throw new RuntimeException("This board is locked for editing");
    }
  }
}
