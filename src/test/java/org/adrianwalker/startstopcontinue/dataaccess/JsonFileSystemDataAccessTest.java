package org.adrianwalker.startstopcontinue.dataaccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.junit.Test;
import static org.junit.Assert.*;

public final class JsonFileSystemDataAccessTest {

  @Test
  public void testCreateReadUpdateDelete() {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("target/test"));

    UUID boardId = UUID.randomUUID();
    Board board = new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>());

    UUID noteId1 = UUID.randomUUID();
    UUID noteId2 = UUID.randomUUID();
    UUID noteId3 = UUID.randomUUID();

    Date date = new Date();
    String color = "#ffffff";
    String textCreate = "Test 1";
    String textUpdate = "Test 2";

    dataAccess.createBoard(board);
    assertTrue(Files.exists(Path.of("target/test", boardId.toString())));

    dataAccess.createNote(boardId, Column.START, new Note()
      .setId(noteId1).setCreated(date).setColor(color).setText(textCreate));
    assertTrue(Files.exists(Path.of("target/test", boardId.toString(), Column.START.toString(), noteId1.toString())));

    dataAccess.createNote(boardId, Column.STOP, new Note()
      .setId(noteId3).setCreated(date).setColor(color).setText(textCreate));
    assertTrue(Files.exists(Path.of("target/test", boardId.toString(), Column.STOP.toString(), noteId3.toString())));

    dataAccess.createNote(boardId, Column.CONTINUE, new Note()
      .setId(noteId2).setCreated(date).setColor(color).setText(textCreate));
    assertTrue(Files.exists(Path.of("target/test", boardId.toString(), Column.CONTINUE.toString(), noteId2.toString())));

    board = dataAccess.readBoard(boardId);
    assertNotNull(board);
    assertEquals(boardId, board.getId());

    assertNotNull(board.getStarts());
    assertNotNull(board.getContinues());
    assertNotNull(board.getStops());

    assertEquals(noteId1, board.getStarts().get(0).getId());
    assertEquals(noteId3, board.getStops().get(0).getId());
    assertEquals(noteId2, board.getContinues().get(0).getId());

    assertEquals(date, board.getStarts().get(0).getCreated());
    assertEquals(date, board.getStops().get(0).getCreated());
    assertEquals(date, board.getContinues().get(0).getCreated());

    assertEquals(color, board.getStarts().get(0).getColor());
    assertEquals(color, board.getStops().get(0).getColor());
    assertEquals(color, board.getContinues().get(0).getColor());

    assertEquals(textCreate, board.getStarts().get(0).getText());
    assertEquals(textCreate, board.getStops().get(0).getText());
    assertEquals(textCreate, board.getContinues().get(0).getText());

    dataAccess.updateNote(boardId, Column.START, new Note().setId(noteId1).setText(textUpdate));
    dataAccess.updateNote(boardId, Column.STOP, new Note().setId(noteId3).setText(textUpdate));
    dataAccess.updateNote(boardId, Column.CONTINUE, new Note().setId(noteId2).setText(textUpdate));

    assertEquals(textUpdate, dataAccess.readBoard(boardId).getStarts().get(0).getText());
    assertEquals(textUpdate, dataAccess.readBoard(boardId).getStops().get(0).getText());
    assertEquals(textUpdate, dataAccess.readBoard(boardId).getContinues().get(0).getText());

    dataAccess.deleteNote(boardId, Column.START, noteId1);
    dataAccess.deleteNote(boardId, Column.STOP, noteId3);
    dataAccess.deleteNote(boardId, Column.CONTINUE, noteId2);

    assertEquals(0, dataAccess.readBoard(boardId).getStarts().size());
    assertEquals(0, dataAccess.readBoard(boardId).getStops().size());
    assertEquals(0, dataAccess.readBoard(boardId).getContinues().size());
  }

  @Test(expected = RuntimeException.class)
  public void testBoardWriteException() {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("/doesnotexist"));

    UUID boardId = UUID.randomUUID();
    Board board = new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>());

    dataAccess.createBoard(board);
  }

  @Test(expected = RuntimeException.class)
  public void testBoardReadException() {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("target/test"));
    dataAccess.readBoard(UUID.randomUUID());
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteNoteException() {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("target/test"));
    dataAccess.deleteNote(UUID.randomUUID(), Column.CONTINUE, UUID.randomUUID());
  }

  @Test(expected = RuntimeException.class)
  public void testWriteNoteException() {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("target/test"));
    dataAccess.createNote(UUID.randomUUID(), Column.CONTINUE, new Note().setId(UUID.randomUUID()));
  }

  @Test(expected = RuntimeException.class)
  public void testReadNoteException() throws IOException {

    DataAccess dataAccess = new JsonFileSystemDataAccess(Path.of("target/test"));

    UUID boardId = UUID.randomUUID();
    Board board = new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>());
    dataAccess.createBoard(board);

    UUID noteId = UUID.randomUUID();
    dataAccess.createNote(boardId, Column.START, new Note().setId(noteId));

    Files.writeString(Path.of("target/test", boardId.toString(), Column.START.toString(), noteId.toString()), "");

    dataAccess.readBoard(boardId);
  }
}
