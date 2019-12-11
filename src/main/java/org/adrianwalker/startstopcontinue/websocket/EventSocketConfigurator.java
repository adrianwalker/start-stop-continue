package org.adrianwalker.startstopcontinue.websocket;

import org.adrianwalker.startstopcontinue.cache.pubsub.EventPubSub;
import javax.websocket.server.ServerEndpointConfig;

public final class EventSocketConfigurator extends ServerEndpointConfig.Configurator {

  private final EventPubSub eventPubSub;

  public EventSocketConfigurator(final EventPubSub eventPubSub) {

    super();

    this.eventPubSub = eventPubSub;
  }

  @Override
  public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {

    return (T) new EventSocket(eventPubSub);
  }
}
