package org.adrianwalker.startstopcontinue.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.String.format;
import java.util.LinkedHashMap;
import java.util.Map;
import static java.util.Map.of;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.adrianwalker.startstopcontinue.service.Service;

public final class WebServlet extends HttpServlet {

  private static final String BOARD_ID_PARAM = "boardid";
  private static final String BOARD_ID_TOKEN = "$" + BOARD_ID_PARAM;
  private static final String REDIRECT_URL = "?" + BOARD_ID_PARAM + "=%s";
  private static final String TEMPLATE = "/static/index.html";
  private static final float LOAD_FACTOR = 0.75f;
  private static final boolean ACCESS_ORDER = true;
  private static final Map<Pattern, String> HTML_MINIFY = of(
    Pattern.compile(">\\s+<"), "><"
  );

  private final Service service;
  private final Map<String, byte[]> cache;

  public WebServlet(final Service service, final int cacheSize) {

    super();

    this.service = service;

    this.cache = new LinkedHashMap(cacheSize + 1, LOAD_FACTOR, ACCESS_ORDER) {

      @Override
      public boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > cacheSize;
      }
    };
  }

  private byte[] readTemplate(final String boardId) throws IOException {

    String html;
    try (InputStream is = getClass().getResourceAsStream(TEMPLATE)) {

      html = new String(is.readAllBytes());
    }

    return minify(substitute(html, boardId)).getBytes();
  }

  public String substitute(final String html, final String boardId) {

    return html.replace(BOARD_ID_TOKEN, boardId);
  }

  public String minify(final String html) {

    String minifiedHtml = html;
    for (Map.Entry<Pattern, String> minify : HTML_MINIFY.entrySet()) {
      minifiedHtml = minify.getKey().matcher(minifiedHtml).replaceAll(minify.getValue());
    }

    return minifiedHtml;
  }

  @Override
  public void doGet(
    final HttpServletRequest request,
    final HttpServletResponse response) throws ServletException, IOException {

    if (null == request.getParameter(BOARD_ID_PARAM)) {

      doNewBoard(response);

    } else {

      doExistingBoard(request, response);
    }
  }

  public void doExistingBoard(
    final HttpServletRequest request,
    final HttpServletResponse response) throws IOException {

    String boardId = request.getParameter(BOARD_ID_PARAM);

    if (!cache.containsKey(boardId)) {
      cache.put(boardId, readTemplate(boardId));
    }

    byte[] html = cache.get(boardId);

    response.setContentType(MediaType.TEXT_HTML);
    response.setContentLength(html.length);
    response.flushBuffer();

    try (OutputStream os = response.getOutputStream()) {

      os.write(html);
      os.flush();
    }
  }

  public void doNewBoard(final HttpServletResponse response) throws IOException {

    UUID boardId = service.createBoard();
    String url = format(REDIRECT_URL, boardId.toString());

    response.sendRedirect(url);
  }
}
