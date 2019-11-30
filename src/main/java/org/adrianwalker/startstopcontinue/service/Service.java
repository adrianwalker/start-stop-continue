package org.adrianwalker.startstopcontinue.service;

import java.io.IOException;
import java.io.OutputStream;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.apache.commons.lang.StringEscapeUtils;

public final class Service {

  private static final byte[] LINE_ENDING = {'\r', '\n'};

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

    return cache.readThrough(boardId, f -> dataAccess.readBoard(boardId));
  }

  public final void createNote(final UUID boardId, final Column column, final Note note) {

    note.setId(UUID.randomUUID())
      .setCreated(new Date())
      .setText(truncateNoteText(note.getText()));

    executor.execute(() -> dataAccess.createNote(boardId, column, note));
    cache.write(boardId, column, note);
  }

  public final void updateNote(final UUID boardId, final Column column, final Note data) {

    Note note = cache.read(boardId, column, data.getId())
      .setText(truncateNoteText(data.getText()));

    executor.execute(() -> dataAccess.updateNote(boardId, column, note));
    cache.write(boardId, column, note);
  }

  public final void deleteNote(final UUID boardId, final Column column, final UUID noteId) {

    executor.execute(() -> dataAccess.deleteNote(boardId, column, noteId));
    cache.delete(boardId, column, noteId);
  }

  private String truncateNoteText(final String text) {

    if (maxNoteLength > 0) {
      return text.substring(0, min(text.length(), maxNoteLength));
    }

    return text;
  }

  public final void exportBoard(final UUID boardId, final OutputStream os) {

    Board board = readBoard(boardId);
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
