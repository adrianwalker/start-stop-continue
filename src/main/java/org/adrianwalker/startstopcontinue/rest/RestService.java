package org.adrianwalker.startstopcontinue.rest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.service.Service;
import org.adrianwalker.startstopcontinue.model.Note;

@Path("")
public class RestService {

  private final Service service;
  private final Cache<UUID, Board> cache;
  private final ExecutorService executor;

  public RestService(final Service service, final Cache<UUID, Board> cache, final int threads) {

    this.service = service;
    this.cache = cache;
    this.executor = Executors.newFixedThreadPool(threads);
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}")
  public Response readBoard(
    @PathParam("boardId")
    final UUID boardId) {

    return Response
      .ok(cache.readThrough(boardId, f -> service.readBoard(boardId)))
      .build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/column/{column}/note")
  public Response createNote(
    @PathParam("boardId")
    final UUID boardId,
    @PathParam("column")
    final Column column,
    final Note note) {

    escapeTags(note);

    executor.execute(() -> service.createNote(boardId, column, note));
    cacheAdd(boardId, column, note);

    return Response.ok(note).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/column/{column}/note")
  public Response updateNote(
    @PathParam("boardId")
    final UUID boardId,
    @PathParam("column")
    final Column column,
    final Note note) {

    escapeTags(note);

    executor.execute(() -> service.updateNote(boardId, column, note));
    cacheUpdate(boardId, column, note);

    return Response.accepted().build();
  }

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/column/{column}/note/{noteId}")
  public Response deleteNote(
    @PathParam("boardId")
    final UUID boardId,
    @PathParam("column")
    final Column column,
    @PathParam("noteId")
    final UUID noteId) {

    executor.execute(() -> service.deleteNote(boardId, column, noteId));
    cacheDelete(boardId, column, noteId);

    return Response.ok(new Note().setId(noteId)).build();
  }

  private void cacheAdd(final UUID boardId, final Column column, final Note note) {

    Board board = cache.readThrough(boardId, f -> service.readBoard(boardId));
    List<Note> notes = notes(board, column);
    notes.add(note);
  }

  private void cacheUpdate(final UUID boardId, final Column column, final Note note) {

    Board board = cache.readThrough(boardId, f -> service.readBoard(boardId));
    List<Note> notes = notes(board, column);

    int index = notes.indexOf(note);
    notes.remove(note);
    notes.add(index, note);
  }

  private void cacheDelete(final UUID boardId, final Column column, final UUID noteId) {

    Board board = cache.readThrough(boardId, f -> service.readBoard(boardId));
    List<Note> notes = notes(board, column);
    notes.remove(new Note().setId(noteId));
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

  private static Note escapeTags(final Note note) {
    return note.setText(note.getText().replaceAll("<", "").replaceAll(">", "&gt;"));
  }
}
