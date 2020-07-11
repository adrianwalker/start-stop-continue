package org.adrianwalker.startstopcontinue;

import org.adrianwalker.startstopcontinue.cli.CommandLineInterface;
import java.util.concurrent.ExecutorService;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSubFactory;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccessFactory;
import org.adrianwalker.startstopcontinue.cache.CacheFactory;
import java.util.concurrent.Executors;
import javax.websocket.server.ServerEndpointConfig;
import org.adrianwalker.startstopcontinue.dataaccess.DataAccess;
import org.adrianwalker.startstopcontinue.rest.RestServlet;
import org.adrianwalker.startstopcontinue.service.Service;
import org.adrianwalker.startstopcontinue.web.WebServlet;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import org.adrianwalker.startstopcontinue.websocket.EventSocket;
import org.adrianwalker.startstopcontinue.websocket.EventSocketConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.ServletContainer;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.cli.commands.CacheCommand;
import org.adrianwalker.startstopcontinue.cli.commands.GcCommand;
import org.adrianwalker.startstopcontinue.cli.commands.HelpCommand;
import org.adrianwalker.startstopcontinue.cli.commands.MonitoringCommand;
import org.adrianwalker.startstopcontinue.cli.commands.UnlockCommand;
import org.adrianwalker.startstopcontinue.configuration.Configuration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

public final class Launcher {

  private static final String CONTEXT_PATH = "/startstopcontinue";
  private static final String REST_SERVLET_PATH = "/api/*";
  private static final String WEB_SERVLET_PATH = "/index.html";
  private static final String DEFAULT_SERVLET_PATH = "/";
  private static final String WEB_SOCKET_SERVLET_PATH = "/events/{boardId}";
  private static final String[] WELCOME_FILES = {"index.html"};
  private static final String BASE_RESOURCE = "static/";
  private static final int IDLE_TIMEOUT = 30 * 60 * 1000;
  private static final int COMPRESSION_LEVEL = 3;
  private static final int TEMPLATE_CACHE_SIZE = 32;

  public static void main(final String[] args) throws Exception {

    Configuration config = new Configuration();

    DataAccess dataAccess = createDataAccess(config);
    Cache cache = createCache(config, dataAccess);
    EventPubSub eventPubSub = createPubSub(config);
    Service service = createService(config, dataAccess, cache);
    Server server = createServer(config);
    CommandLineInterface cli = createCli(config, service, eventPubSub);

    ServletContextHandler context = createContext(CONTEXT_PATH, BASE_RESOURCE, WELCOME_FILES);

    addRestServlet(context, service);
    addWebServlet(context, service);
    addWebSocketServlet(context, eventPubSub);
    addDefaultServlet(context);

    server.setHandler(enableCompression(context));
    server.start();

    cli.start();
  }

  private static EventPubSub createPubSub(final Configuration config) {

    return new EventPubSubFactory(config.getPubSubConfiguration()).create();
  }

  private static Cache createCache(final Configuration config, final DataAccess dataAccess) {

    return new CacheFactory(
      config.getCacheConfiguration(),
      boardId -> dataAccess.readBoard(boardId)).create();
  }

  private static DataAccess createDataAccess(final Configuration config) {

    return new DataAccessFactory(config.getDataConfiguration()).create();
  }

  private static Service createService(
    final Configuration config, final DataAccess dataAccess, final Cache cache) {

    ExecutorService executorService = Executors.newFixedThreadPool(
      config.getDataConfiguration().getDataThreads());

    return new Service(
      dataAccess,
      cache,
      executorService,
      config.getDataConfiguration().getDataSize());
  }

  private static Server createServer(final Configuration config) {

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(config.getHttpConfiguration().getHttpPort());
    server.setConnectors(new Connector[]{connector});

    return server;
  }

  private static CommandLineInterface createCli(
    final Configuration config, final Service service, final EventPubSub eventPubSub) {

    int port = config.getCommandLineInterfaceConfiguration().getHttpPort();
    CommandLineInterface cli = new CommandLineInterface(port);

    return cli
      .addCommand(new HelpCommand(cli))
      .addCommand(new MonitoringCommand())
      .addCommand(new UnlockCommand(service, eventPubSub))
      .addCommand(new CacheCommand(service))
      .addCommand(new GcCommand());
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

  private static Handler enableCompression(final Handler handler) {

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setHandler(handler);
    gzipHandler.setCompressionLevel(COMPRESSION_LEVEL);

    return gzipHandler;
  }

  private static void addDefaultServlet(final ServletContextHandler context) {

    context.addServlet(DefaultServlet.class, DEFAULT_SERVLET_PATH);
  }

  private static void addWebSocketServlet(
    final ServletContextHandler context, final EventPubSub eventPubSub) throws Exception {

    ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
    container.setDefaultMaxSessionIdleTimeout(IDLE_TIMEOUT);
    container.addEndpoint(ServerEndpointConfig.Builder
      .create(EventSocket.class, WEB_SOCKET_SERVLET_PATH)
      .configurator(new EventSocketConfigurator(eventPubSub))
      .build());
  }

  private static void addWebServlet(final ServletContextHandler context, final Service service) {

    context.addServlet(
      new ServletHolder(new WebServlet(service, TEMPLATE_CACHE_SIZE)),
      WEB_SERVLET_PATH);
  }

  private static void addRestServlet(final ServletContextHandler context, final Service service) {

    context.addServlet(
      new ServletHolder(
        new ServletContainer(
          new RestServlet(service))),
      REST_SERVLET_PATH);
  }
}
