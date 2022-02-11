package org.adrianwalker.startstopcontinue.service;

import static java.lang.Math.min;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import static org.adrianwalker.startstopcontinue.Monitoring.logMemoryUsage;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.cache.Cache;
import static org.adrianwalker.startstopcontinue.Monitoring.logFileDescriptors;
import org.adrianwalker.startstopcontinue.pubsub.Event;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;

public final class Service {

  private final DataAccess dataAccess;
  private final Cache cache;
  private final ExecutorService executor;
  private final EventPubSub eventPubSub;
  private final int maxNoteLength;

  public Service(
    final DataAccess dataAccess, final Cache cache,
    final ExecutorService executor, final EventPubSub eventPubSub,
    final int maxNoteLength) {

    this.dataAccess = dataAccess;
    this.cache = cache;
    this.executor = executor;
    this.eventPubSub = eventPubSub;
    this.maxNoteLength = maxNoteLength;
  }

  public final UUID createBoard() {

    UUID boardId = UUID.randomUUID();
    dataAccess.createBoard(boardId);

    logMemoryUsage();
    logFileDescriptors();

    return boardId;
  }

  public final Board readBoard(final UUID boardId) {

    Board board = cache.read(boardId);

    logMemoryUsage();
    logFileDescriptors();

    return board;
  }

  public final void lockBoard(final UUID boardId) {

    checkLock(boardId);

    executor.execute(() -> {
      dataAccess.lockBoard(boardId);
      cache.lock(boardId);
    });

    logMemoryUsage();
    logFileDescriptors();
  }

  public final void unlockBoard(final UUID boardId) {

    executor.execute(() -> {
      dataAccess.unlockBoard(boardId);
      cache.unlock(boardId);
    });

    logMemoryUsage();
    logFileDescriptors();
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
    logFileDescriptors();
  }

  public final void updateNote(final UUID boardId, final Column column, final Note note) {

    checkLock(boardId);

    Note update = cache.read(boardId, column, note.getId())
      .setText(truncateNoteText(note.getText()));

    executor.execute(() -> {
      dataAccess.updateNote(boardId, column, update);
      cache.write(boardId, column, update);
    });

    logMemoryUsage();
    logFileDescriptors();
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    checkLock(boardId);

    executor.execute(() -> {
      dataAccess.deleteNote(boardId, column, noteId);
      cache.delete(boardId, column, noteId);
    });

    logMemoryUsage();
    logFileDescriptors();
  }

  public final void publishEvent(final UUID boardId, final Event event) {

    executor.execute(() -> {
      eventPubSub.publish(boardId, event);
    });

    logMemoryUsage();
    logFileDescriptors();
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

  public long cacheSize() {

    return cache.size();
  }

  public void cachePurge() {

    cache.purge();
  }
}
