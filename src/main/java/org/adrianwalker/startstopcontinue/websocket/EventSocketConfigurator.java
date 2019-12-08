package org.adrianwalker.startstopcontinue.websocket;

import javax.websocket.server.ServerEndpointConfig;

public final class EventSocketConfigurator extends ServerEndpointConfig.Configurator {

  private final SessionCache sessionCache;

  public EventSocketConfigurator(final SessionCache sessionCache) {

    super();

    this.sessionCache = sessionCache;
  }

  @Override
  public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {

    return (T) new EventSocket(sessionCache);
  }
}
