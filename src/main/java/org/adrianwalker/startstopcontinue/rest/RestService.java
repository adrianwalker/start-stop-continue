package org.adrianwalker.startstopcontinue.rest;

import java.util.HashMap;
import java.util.Map;
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
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.ID;
import org.adrianwalker.startstopcontinue.model.Note;
import org.adrianwalker.startstopcontinue.service.Service;

@Path("")
public class RestService {

  private static final Map<String, String> HTML_ESCAPE = new HashMap<>();

  static {
    HTML_ESCAPE.put("<", "&lt;");
    HTML_ESCAPE.put(">", "&gt;");
  }

  private final Service service;

  public RestService(final Service service) {

    this.service = service;
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}")
  public Response readBoard(
    @PathParam("boardId")
    final UUID boardId) {

    return Response
      .ok(service.readBoard(boardId))
      .build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("board/{boardId}/lock")
  public Response lockBoard(
    @PathParam("boardId")
    final UUID boardId) {

    service.lockBoard(boardId);

    return Response.ok(new ID().setId(boardId)).build();
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

    service.createNote(boardId, column, escapeHtml(note));

    return Response.ok(new ID().setId(note.getId())).build();
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

    service.updateNote(boardId, column, escapeHtml(note));

    return Response.ok(new ID().setId(note.getId())).build();
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

    service.deleteNote(boardId, column, noteId);

    return Response.ok(new ID().setId(noteId)).build();
  }

  private static Note escapeHtml(final Note note) {

    for (Map.Entry<String, String> escape : HTML_ESCAPE.entrySet()) {
      note.setText(note.getText().replace(escape.getKey(), escape.getValue()));
    }

    return note;
  }
}
