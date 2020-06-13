package org.adrianwalker.startstopcontinue.web;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.adrianwalker.startstopcontinue.cache.NonCachingCache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.service.Service;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

public final class WebServletTest {

  private static final int THREADS = 1;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private DataAccess dataAccess;

  private ExecutorService executorService;

  @Before
  public void setUp() throws Exception {

    MockitoAnnotations.initMocks(this);
    executorService = Executors.newFixedThreadPool(THREADS);
  }

  @Test
  public void testDoGetNewBoard() throws Exception {

    Service service = new Service(
      dataAccess,
      new NonCachingCache(boardId -> dataAccess.readBoard(boardId)),
      executorService, 0);
    WebServlet servlet = new WebServlet(service, 0);
    servlet.doGet(request, response);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).sendRedirect(stringCaptor.capture());

    assertTrue(stringCaptor.getValue().matches("\\?boardid=\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}"));
  }
}
