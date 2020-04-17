package org.adrianwalker.startstopcontinue.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.ID;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.service.Service;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public final class RestServiceTest {

  private static final int THREADS = 1;

  private static final UUID BOARD_ID = UUID.randomUUID();
  private static final UUID NOTE_ID_1 = UUID.randomUUID();
  private static final UUID NOTE_ID_2 = UUID.randomUUID();
  private static final UUID NOTE_ID_3 = UUID.randomUUID();

  @Mock
  private DataAccess dataAccess;

  private ExecutorService executorService;

  private Service service;

  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);
    executorService = Executors.newFixedThreadPool(THREADS);

    List<Note> starts = new ArrayList<>();
    starts.add(new Note().setId(NOTE_ID_1).setColor("#ffffff").setText("Start").setVersion(1));

    List<Note> stops = new ArrayList<>();
    stops.add(new Note().setId(NOTE_ID_2).setColor("#ffffff").setText("Stop").setVersion(1));

    List<Note> continues = new ArrayList<>();
    continues.add(new Note().setId(NOTE_ID_3).setColor("#ffffff").setText("Continue").setVersion(1));

    when(dataAccess.readBoard(any(UUID.class))).thenReturn(new Board()
      .setId(BOARD_ID)
      .setStarts(starts)
      .setStops(stops)
      .setContinues(continues));

    service = new Service(dataAccess, nonCachingCache(), executorService, 0);
  }

  public static Cache nonCachingCache() {

    return new Cache() {

      @Override
      public Board readThrough(UUID boardId, Function<UUID, Board> f) {
        return f.apply(boardId);
      }

      @Override
      public Note read(UUID boardId, Column column, UUID noteId) {
        return new Note().setId(noteId);
      }

      @Override
      public void write(UUID boardId, Column column, Note note) {
      }

      @Override
      public void delete(UUID boardId, Column column, UUID noteId) {
      }
    };
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
  public void testExportBoard() throws IOException {

    RestService restService = new RestService(service);
    Response response = restService.exportBoard(BOARD_ID);

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    StreamingOutput stream = (StreamingOutput) response.getEntity();
    stream.write(output);

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(
      "START\r\n\r\nStart\r\n\r\n"
      + "STOP\r\n\r\nStop\r\n\r\n"
      + "CONTINUE\r\n\r\nContinue",
      new String(output.toByteArray()));
  }

  @Test
  public void testCreateNote() {

    RestService restService = new RestService(service);
    Response response = restService.createNote(
      BOARD_ID, Column.START, new Note().setText("Start"));

    ID id = (ID) response.getEntity();

    assertNotNull(id);
    assertTrue(id.getId() instanceof UUID);
  }

  @Test
  public void testUpdateNote() {

    RestService restService = new RestService(service);
    Response response = restService.updateNote(
      BOARD_ID, Column.START, new Note().setId(NOTE_ID_1).setText("Update"));

    ID id = (ID) response.getEntity();

    assertNotNull(id);
    assertEquals(NOTE_ID_1, id.getId());
  }

  @Test
  public void testDeleteNote() {

    RestService restService = new RestService(service);
    Response response = restService.deleteNote(BOARD_ID, Column.START, NOTE_ID_1);

    ID id = (ID) response.getEntity();

    assertNotNull(id);
    assertEquals(NOTE_ID_1, id.getId());
  }
}
