package org.adrianwalker.startstopcontinue.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.adrianwalker.startstopcontinue.cache.NonCachingCache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import org.adrianwalker.startstopcontinue.pubsub.HashSetEventPubSub;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public final class ServiceTest {

  private static final int THREADS = 1;

  private static final UUID BOARD_ID = UUID.randomUUID();
  private static final UUID NOTE_ID_1 = UUID.randomUUID();
  private static final UUID NOTE_ID_2 = UUID.randomUUID();
  private static final UUID NOTE_ID_3 = UUID.randomUUID();

  @Mock
  private DataAccess dataAccess;
  private ExecutorService executorService;
  private EventPubSub eventPubSub;

  @Before
  public void setUp() throws Exception {

    MockitoAnnotations.initMocks(this);
    executorService = Executors.newFixedThreadPool(THREADS);
    eventPubSub = new HashSetEventPubSub();

    List<Note> starts = new ArrayList<>();
    starts.add(new Note().setId(NOTE_ID_1).setColor("#ffffff").setText("Start"));

    List<Note> stops = new ArrayList<>();
    stops.add(new Note().setId(NOTE_ID_2).setColor("#ffffff").setText("Stop"));

    List<Note> continues = new ArrayList<>();
    continues.add(new Note().setId(NOTE_ID_3).setColor("#ffffff").setText("Continue"));

    when(dataAccess.readBoard(any(UUID.class))).thenReturn(new Board()
      .setId(BOARD_ID)
      .setStarts(starts)
      .setStops(stops)
      .setContinues(continues));
  }

  @Test
  public void testCreateBoard() {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      0);
    UUID boardId = service.createBoard();
    assertNotNull(boardId);
  }

  @Test
  public void testReadBoard() {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      0);
    Board board = service.readBoard(BOARD_ID);
    assertNotNull(board);
    assertEquals(BOARD_ID, board.getId());
    assertEquals(1, board.getStarts().size());
    assertEquals(1, board.getStops().size());
    assertEquals(1, board.getContinues().size());
  }

  @Test
  public void testCreateNote() throws InterruptedException {

    testCreateNote(0);
  }

  @Test
  public void testCreateTruncatedNote() throws InterruptedException {

    testCreateNote(3);
  }

  private void testCreateNote(final int maxNoteLength) throws InterruptedException {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      maxNoteLength);
    service.createNote(BOARD_ID, Column.START, new Note().setColor("#ffffff").setText("Start"));
    service.createNote(BOARD_ID, Column.STOP, new Note().setColor("#ffffff").setText("Stop"));
    service.createNote(BOARD_ID, Column.CONTINUE, new Note().setColor("#ffffff").setText("Continue"));

    executorService.awaitTermination(10, TimeUnit.SECONDS);

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<Column> columnCaptor = ArgumentCaptor.forClass(Column.class);
    ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
    verify(dataAccess, times(3)).createNote(uuidCaptor.capture(), columnCaptor.capture(), noteCaptor.capture());

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(0));
    assertEquals(Column.START, columnCaptor.getAllValues().get(0));
    assertNotNull(noteCaptor.getAllValues().get(0).getId());
    if (maxNoteLength > 0) {
      assertEquals("Start".substring(0, maxNoteLength), noteCaptor.getAllValues().get(0).getText());
    } else {
      assertEquals("Start", noteCaptor.getAllValues().get(0).getText());
    }

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(1));
    assertEquals(Column.STOP, columnCaptor.getAllValues().get(1));
    assertNotNull(noteCaptor.getAllValues().get(1).getId());
    assertNotNull(noteCaptor.getAllValues().get(0).getId());
    if (maxNoteLength > 0) {
      assertEquals("Stop".substring(0, maxNoteLength), noteCaptor.getAllValues().get(1).getText());
    } else {
      assertEquals("Stop", noteCaptor.getAllValues().get(1).getText());
    }

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(2));
    assertEquals(Column.CONTINUE, columnCaptor.getAllValues().get(2));
    assertNotNull(noteCaptor.getAllValues().get(2).getId());
    assertNotNull(noteCaptor.getAllValues().get(0).getId());
    if (maxNoteLength > 0) {
      assertEquals("Continue".substring(0, maxNoteLength), noteCaptor.getAllValues().get(2).getText());
    } else {
      assertEquals("Continue", noteCaptor.getAllValues().get(2).getText());
    }
  }

  @Test
  public void testUpdateNote() throws InterruptedException {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      0);
    service.updateNote(BOARD_ID, Column.START, new Note().setId(NOTE_ID_1).setText("Start"));
    service.updateNote(BOARD_ID, Column.STOP, new Note().setId(NOTE_ID_2).setText("Stop"));
    service.updateNote(BOARD_ID, Column.CONTINUE, new Note().setId(NOTE_ID_3).setText("Continue"));

    executorService.awaitTermination(10, TimeUnit.SECONDS);

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<Column> columnCaptor = ArgumentCaptor.forClass(Column.class);
    ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
    verify(dataAccess, times(3)).updateNote(uuidCaptor.capture(), columnCaptor.capture(), noteCaptor.capture());

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(0));
    assertEquals(Column.START, columnCaptor.getAllValues().get(0));
    assertNotNull(noteCaptor.getAllValues().get(0).getId());
    assertEquals("Start", noteCaptor.getAllValues().get(0).getText());

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(1));
    assertEquals(Column.STOP, columnCaptor.getAllValues().get(1));
    assertNotNull(noteCaptor.getAllValues().get(1).getId());
    assertEquals("Stop", noteCaptor.getAllValues().get(1).getText());

    assertEquals(BOARD_ID, uuidCaptor.getAllValues().get(2));
    assertEquals(Column.CONTINUE, columnCaptor.getAllValues().get(2));
    assertNotNull(noteCaptor.getAllValues().get(2).getId());
    assertEquals("Continue", noteCaptor.getAllValues().get(2).getText());
  }

  @Test
  public void testDeleteNote() throws InterruptedException {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      0);
    service.deleteNote(BOARD_ID, Column.START, NOTE_ID_1);
    service.deleteNote(BOARD_ID, Column.STOP, NOTE_ID_2);
    service.deleteNote(BOARD_ID, Column.CONTINUE, NOTE_ID_3);

    executorService.awaitTermination(10, TimeUnit.SECONDS);

    ArgumentCaptor<UUID> uuidCaptor1 = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<Column> columnCaptor = ArgumentCaptor.forClass(Column.class);
    ArgumentCaptor<UUID> uuidCaptor2 = ArgumentCaptor.forClass(UUID.class);
    verify(dataAccess, times(3)).deleteNote(uuidCaptor1.capture(), columnCaptor.capture(), uuidCaptor2.capture());

    assertEquals(BOARD_ID, uuidCaptor1.getAllValues().get(0));
    assertEquals(Column.START, columnCaptor.getAllValues().get(0));
    assertEquals(NOTE_ID_1, uuidCaptor2.getAllValues().get(0));

    assertEquals(BOARD_ID, uuidCaptor1.getAllValues().get(1));
    assertEquals(Column.STOP, columnCaptor.getAllValues().get(1));
    assertEquals(NOTE_ID_2, uuidCaptor2.getAllValues().get(1));

    assertEquals(BOARD_ID, uuidCaptor1.getAllValues().get(2));
    assertEquals(Column.CONTINUE, columnCaptor.getAllValues().get(2));
    assertEquals(NOTE_ID_3, uuidCaptor2.getAllValues().get(2));
  }
}
