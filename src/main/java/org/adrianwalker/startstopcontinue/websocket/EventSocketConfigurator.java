package org.adrianwalker.startstopcontinue.websocket;

import java.util.HashMap;
import java.util.Map;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import javax.websocket.server.ServerEndpointConfig;

public final class EventSocketConfigurator extends ServerEndpointConfig.Configurator {
  
  private final Map<Class, Object> endPointInstances = new HashMap<>(); 

  public EventSocketConfigurator(final EventPubSub eventPubSub) {

    super();

    endPointInstances.put(EventSocket.class, new EventSocket(eventPubSub));
  }

  @Override
  public <T> T getEndpointInstance(final Class<T> clazz) throws InstantiationException {

    return (T) endPointInstances.get(clazz);
  }
}
