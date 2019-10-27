package org.adrianwalker.startstopcontinue.web;

import java.io.IOException;
import static java.lang.String.format;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.service.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityViewServlet;

public final class WebServlet extends VelocityViewServlet {

  private static final String BOARD_ID = "boardid";
  private static final String REDIRECT_URL = "%s?" + BOARD_ID + "=%s";

  private final Service service;

  public WebServlet(final Service service) {

    super();
    this.service = service;
  }

  @Override
  protected Template handleRequest(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Context context) {

    String boardId = request.getParameter(BOARD_ID);

    if (null == boardId) {

      Board board = new Board();
      service.create(board);

      String url = format(REDIRECT_URL, request.getRequestURL().toString(), board.getId().toString());
      try {
        response.sendRedirect(url);
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
    
    context.put("boardId", boardId);

    return getTemplate("index.html");
  }
}
