package org.adrianwalker.startstopcontinue.rest;

import java.util.Set;
import java.util.UUID;
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
import org.adrianwalker.startstopcontinue.service.Service;
import org.adrianwalker.startstopcontinue.model.Note;

@Path("")
public class RestService {

  private final Service service;
  private final Cache<UUID, Board> cache;

  public RestService(final Service service, final Cache<UUID, Board> cache) {

    this.service = service;
    this.cache = cache;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/note")
  public Response create(
    @PathParam("boardId")
    final UUID boardId,
    final Note note) {

    service.create(boardId, note);
    cacheUpdate(boardId, note, false, true);

    return Response.ok(note).build();
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}")
  public Response read(
    @PathParam("boardId")
    final UUID boardId) {

    return Response
      .ok(cache.readThrough(boardId, f -> service.read(boardId)))
      .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/note")
  public Response update(
    @PathParam("boardId")
    final UUID boardId,
    final Note note) {

    service.update(boardId, note);
    cacheUpdate(boardId, note, true, true);

    return Response.ok(note).build();
  }

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/note")
  public Response delete(
    @PathParam("boardId")
    final UUID boardId,
    final Note note) {

    service.delete(boardId, note);
    cacheUpdate(boardId, note, true, false);

    return Response.ok(note).build();
  }

  private void cacheUpdate(final UUID boardId, final Note note, final boolean remove, final boolean add) {

    Board board = cache.readThrough(boardId, f -> service.read(boardId));

    Set<Note> notes = null;
    switch (note.getType()) {
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

    if (null == notes) {
      return;
    }

    if (remove) {
      notes.remove(note);
    }

    if (add) {
      notes.add(note);
    }
  }
}
