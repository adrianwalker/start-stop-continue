package org.adrianwalker.startstopcontinue.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.core.Response;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.service.Service;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.adrianwalker.startstopcontinue.cache.NonCachingCache;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import org.adrianwalker.startstopcontinue.pubsub.HashSetEventPubSub;

public final class RestServiceTest {

  private static final int THREADS = 1;

  private static final UUID BOARD_ID = UUID.randomUUID();
  private static final UUID NOTE_ID_1 = UUID.randomUUID();
  private static final UUID NOTE_ID_2 = UUID.randomUUID();
  private static final UUID NOTE_ID_3 = UUID.randomUUID();

  @Mock
  private DataAccess dataAccess;
  private ExecutorService executorService;
  private EventPubSub eventPubSub;

  private Service service;

  @Before
  public void setUp() {

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

    service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, eventPubSub,
      0);
  }

  @Test
  public void testReadBoard() {

    RestService restService = new RestService(service);
    Response response = restService.readBoard(BOARD_ID);

    Board board = (Board) response.getEntity();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(BOARD_ID, board.getId());
    assertEquals(NOTE_ID_1, board.getStarts().get(0).getId());
    assertEquals(NOTE_ID_2, board.getStops().get(0).getId());
    assertEquals(NOTE_ID_3, board.getContinues().get(0).getId());
    assertEquals("Start", board.getStarts().get(0).getText());
    assertEquals("Stop", board.getStops().get(0).getText());
    assertEquals("Continue", board.getContinues().get(0).getText());
  }

  @Test
  public void testCreateNote() {

    RestService restService = new RestService(service);
    Response response = restService.createNote(
      BOARD_ID, Column.START, new Note().setText("Start"));

    Note note = (Note) response.getEntity();

    assertNotNull(note);
    assertTrue(note.getId() instanceof UUID);
  }

  @Test
  public void testUpdateNote() {

    RestService restService = new RestService(service);
    Response response = restService.updateNote(
      BOARD_ID, Column.START, new Note().setId(NOTE_ID_1).setText("Update"));

    Note note = (Note) response.getEntity();

    assertNotNull(note);
    assertEquals(NOTE_ID_1, note.getId());
  }

  @Test
  public void testDeleteNote() {

    RestService restService = new RestService(service);
    Response response = restService.deleteNote(BOARD_ID, Column.START, NOTE_ID_1);

    Note note = (Note) response.getEntity();

    assertNotNull(note);
    assertEquals(NOTE_ID_1, note.getId());
  }
}
