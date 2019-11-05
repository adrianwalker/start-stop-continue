package org.adrianwalker.startstopcontinue.web;

import java.io.IOException;
import static java.lang.String.format;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.service.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityViewServlet;

public final class WebServlet extends VelocityViewServlet {

  private static final String BOARD_ID = "boardid";
  private static final String REDIRECT_URL = "?" + BOARD_ID + "=%s";
  private static final String TEMPLATE = "index.html";

  private final Service service;

  public WebServlet(final Service service) {

    super();
    this.service = service;
  }

  @Override
  public void doGet(
    final HttpServletRequest request,
    final HttpServletResponse response) throws ServletException, IOException {

    if (null == request.getParameter(BOARD_ID)) {

      Board board = service.createBoard();

      String url = format(REDIRECT_URL, board.getId().toString());
      response.sendRedirect(url);

    } else {
      super.doGet(request, response);
    }
  }

  @Override
  protected Template handleRequest(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Context context) {

    context.put(BOARD_ID, request.getParameter(BOARD_ID));

    return getTemplate(TEMPLATE);
  }
}
