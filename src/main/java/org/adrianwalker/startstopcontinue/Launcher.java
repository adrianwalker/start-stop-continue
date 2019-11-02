package org.adrianwalker.startstopcontinue;

import java.nio.file.Path;
import java.util.UUID;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.dataaccess.JsonFileSystemDataAccess;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.rest.RestServlet;
import org.adrianwalker.startstopcontinue.service.Service;
import org.adrianwalker.startstopcontinue.web.WebServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

public final class Launcher {

  private static final int BOARD_CACHE_SIZE = 32;
  private static final int PERSISTENCE_THREADS = 4;
  private static final Path FILE_PATH = Path.of("/var/tmp/startstopcontinue");
  private static final int PORT = 8080;
  private static final String CONTEXT_PATH = "/startstopcontinue";
  private static final String REST_SERVLET_PATH = "/api/*";
  private static final String WEB_SERVLET_PATH = "/index.html";
  private static final String DEFAULT_SERVLET_PATH = "/";
  private static final String[] WELCOME_FILES = {"index.html"};
  private static final String BASE_RESOURCE = "static/";
  private static final int MAX_NOTE_LENGTH = 1024;

  public static void main(final String[] args) throws Exception {

    JsonFileSystemDataAccess dataAccess = new JsonFileSystemDataAccess(FILE_PATH);
    Cache<UUID, Board> cache = new Cache<>(BOARD_CACHE_SIZE);
    Service service = new Service(dataAccess, cache, PERSISTENCE_THREADS, MAX_NOTE_LENGTH);

    Server server = createServer(PORT);
    ServletContextHandler context = createContext(CONTEXT_PATH, BASE_RESOURCE, WELCOME_FILES);

    context.addServlet(
      new ServletHolder(
        new ServletContainer(
          new RestServlet(service))),
      REST_SERVLET_PATH);

    context.addServlet(
      new ServletHolder(new WebServlet(service)),
      WEB_SERVLET_PATH);

    context.addServlet(DefaultServlet.class, DEFAULT_SERVLET_PATH);

    server.setHandler(context);
    server.start();
  }

  private static ServletContextHandler createContext(
    final String contextPath, final String baseResourcePath, final String... welcomeFiles) {

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath(contextPath);
    ClassLoader cl = Launcher.class.getClassLoader();
    context.setBaseResource(Resource.newResource(cl.getResource(baseResourcePath)));
    context.setWelcomeFiles(welcomeFiles);

    return context;
  }

  private static Server createServer(final int port) {

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});

    return server;
  }
}
