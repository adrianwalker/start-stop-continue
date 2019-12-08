package org.adrianwalker.startstopcontinue.websocket;

import javax.websocket.server.ServerEndpointConfig;

public final class EventSocketConfigurator extends ServerEndpointConfig.Configurator {

  public EventSocketConfigurator() {
    super();
  }

  @Override
  public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {
    return (T) new EventSocket();
  }
}
