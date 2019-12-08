package org.adrianwalker.startstopcontinue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.websocket.server.ServerEndpointConfig;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.cache.LinkedHashMapCache;
import org.adrianwalker.startstopcontinue.cache.RedisCache;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.dataaccess.JsonFileSystemDataAccess;
import org.adrianwalker.startstopcontinue.dataaccess.MinioDataAccess;
import org.adrianwalker.startstopcontinue.rest.RestServlet;
import org.adrianwalker.startstopcontinue.service.Service;
import org.adrianwalker.startstopcontinue.web.WebServlet;
import org.adrianwalker.startstopcontinue.websocket.EventSocket;
import org.adrianwalker.startstopcontinue.websocket.EventSocketConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.ServletContainer;

public final class Launcher {

  private static final String CONTEXT_PATH = "/startstopcontinue";
  private static final String REST_SERVLET_PATH = "/api/*";
  private static final String WEB_SERVLET_PATH = "/index.html";
  private static final String DEFAULT_SERVLET_PATH = "/";
  private static final String WEB_SOCKET_SERVLET_PATH = "/events/{boardId}";
  private static final String[] WELCOME_FILES = {"index.html"};
  private static final String BASE_RESOURCE = "static/";
  private static final int IDLE_TIMEOUT = 30 * 60 * 1000;

  public static void main(final String[] args) throws Exception {

    Configuration config = new Configuration();

    DataAccess dataAccess = DataAccessFactory.create(config);
    Cache cache = CacheFactory.create(config);
    ExecutorService executorService = Executors.newFixedThreadPool(config.getDataThreads());
    Service service = new Service(dataAccess, cache, executorService, config.getDataSize());

    Server server = createServer(config.getHttpPort());
    ServletContextHandler context = createContext(CONTEXT_PATH, BASE_RESOURCE, WELCOME_FILES);

    context.addServlet(
      new ServletHolder(
        new ServletContainer(
          new RestServlet(service))),
      REST_SERVLET_PATH);

    context.addServlet(
      new ServletHolder(new WebServlet(service)),
      WEB_SERVLET_PATH);

    WebSocketServerContainerInitializer
      .configureContext(context)
      .addEndpoint(ServerEndpointConfig.Builder
        .create(EventSocket.class, WEB_SOCKET_SERVLET_PATH)
        .configurator(new EventSocketConfigurator())
        .build());

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
    connector.setIdleTimeout(IDLE_TIMEOUT);
    server.setConnectors(new Connector[]{connector});

    return server;
  }

  private static final class CacheFactory {

    public static Cache create(final Configuration config) {

      Cache cache;

      if (!config.getCacheHostname().isEmpty() && config.getCachePort() > 0) {

        cache = new RedisCache(
          config.getCacheHostname(), config.getCachePort(),
          config.getCachePassword());

      } else {

        cache = new LinkedHashMapCache(config.getCacheSize());
      }

      return cache;
    }
  }

  private static final class DataAccessFactory {

    public static DataAccess create(final Configuration config) {

      DataAccess dataAccess;

      if (!config.getDataEndpoint().isEmpty() && config.getDataPort() > 0) {

        dataAccess = new MinioDataAccess(
          config.getDataEndpoint(), config.getDataPort(),
          config.getDataAccessKey(), config.getDataSecretKey());

      } else {

        dataAccess = new JsonFileSystemDataAccess(config.getDataPath());
      }

      return dataAccess;
    }
  }
}
