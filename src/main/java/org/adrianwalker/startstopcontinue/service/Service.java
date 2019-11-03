package org.adrianwalker.startstopcontinue.service;

import java.io.IOException;
import java.io.OutputStream;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.apache.commons.lang.StringEscapeUtils;

public final class Service {

  private static final byte[] LINE_ENDING = {'\r', '\n'};

  private final DataAccess dataAccess;
  private final Cache<UUID, Board> cache;
  private final ExecutorService executor;
  private final int maxNoteLength;

  public Service(final DataAccess dataAccess, final Cache<UUID, Board> cache, final int threads, final int maxNoteLength) {

    this.dataAccess = dataAccess;
    this.cache = cache;
    this.executor = Executors.newFixedThreadPool(threads);
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

    return cacheRead(boardId);
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    note.setId(UUID.randomUUID())
      .setCreated(new Date())
      .setText(truncateNoteText(note.getText()));

    executor.execute(() -> dataAccess.createNote(boardId, column, note));
    cacheAdd(boardId, column, note);
  }

  public final void updateNote(final UUID boardId, final Column column, final Note data) {

    Note note = cacheRead(boardId, column, data.getId())
      .setText(truncateNoteText(data.getText()));

    executor.execute(() -> dataAccess.updateNote(boardId, column, note));
    cacheUpdate(boardId, column, note);
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    executor.execute(() -> dataAccess.deleteNote(boardId, column, noteId));
    cacheDelete(boardId, column, noteId);
  }

  private List<Note> notes(final Board board, final Column column) {

    List<Note> notes = null;
    switch (column) {
      case START:
        notes = board.getStarts();
        break;
      case STOP:
        notes = board.getStops();
        break;
      case CONTINUE:
        notes = board.getContinues();
        break;
    }
    return notes;
  }

  private Board cacheRead(final UUID boardId) {

    return cache.readThrough(boardId, f -> dataAccess.readBoard(boardId));
  }

  private Note cacheRead(final UUID boardId, final Column column, final UUID noteId) {

    List<Note> notes = notes(cacheRead(boardId), column);
    return notes.get(notes.indexOf(new Note().setId(noteId)));
  }

  private void cacheAdd(final UUID boardId, final Column column, final Note note) {

    notes(cacheRead(boardId), column).add(note);
  }

  private void cacheUpdate(final UUID boardId, final Column column, final Note note) {

    cacheRead(boardId, column, note.getId()).setText(note.getText());
  }

  private void cacheDelete(final UUID boardId, final Column column, final UUID noteId) {

    notes(cacheRead(boardId), column).remove(new Note().setId(noteId));
  }

  private String truncateNoteText(final String text) {

    if (maxNoteLength > 0) {
      return text.substring(0, min(text.length(), maxNoteLength));
    }

    return text;
  }

  public final void exportBoard(final UUID boardId, final OutputStream os) {

    Board board = cacheRead(boardId);
    write(os, Column.START.name().getBytes());
    exportNotes(board.getStarts(), os);
    write(os, LINE_ENDING);
    write(os, LINE_ENDING);

    write(os, Column.STOP.name().getBytes());
    exportNotes(board.getStops(), os);
    write(os, LINE_ENDING);
    write(os, LINE_ENDING);

    write(os, Column.CONTINUE.name().getBytes());
    exportNotes(board.getContinues(), os);
  }

  private void exportNotes(final List<Note> notes, final OutputStream os) {

    notes.stream()
      .map(note -> note.getText())
      .map(text -> StringEscapeUtils.unescapeHtml(text))
      .map(text -> text.getBytes())
      .forEach(bytes -> {
        write(os, LINE_ENDING);
        write(os, LINE_ENDING);
        write(os, bytes);
      });
  }

  private void write(final OutputStream os, byte[] b) throws RuntimeException {
    try {
      os.write(b);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
