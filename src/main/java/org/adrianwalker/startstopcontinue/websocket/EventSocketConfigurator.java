package org.adrianwalker.startstopcontinue.websocket;

import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import javax.websocket.server.ServerEndpointConfig;
import org.adrianwalker.startstopcontinue.cache.SessionsCache;

public final class EventSocketConfigurator extends ServerEndpointConfig.Configurator {

  private final EventPubSub eventPubSub;
  private final SessionsCache sessionsCache;

  public EventSocketConfigurator(final EventPubSub eventPubSub, final SessionsCache sessionsCache) {

    super();

    this.eventPubSub = eventPubSub;
    this.sessionsCache = sessionsCache;
  }

  @Override
  public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {

    return (T) new EventSocket(eventPubSub, sessionsCache);
  }
}
