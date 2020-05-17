package org.adrianwalker.startstopcontinue.pubsub;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.adrianwalker.startstopcontinue.websocket.EventSocket;
import org.adrianwalker.startstopcontinue.configuration.PubSubConfiguration;

public final class EventPubSubFactory {

  private final PubSubConfiguration config;

  public EventPubSubFactory(final PubSubConfiguration config) {

    this.config = config;
  }

  public EventPubSub create() {

    EventPubSub eventPubSub;

    if (!config.getPubSubHostname().isEmpty() && config.getPubSubPort() > 0) {

      eventPubSub = new RedisEventPubSub(
        createRedisPubSubconnection(config),
        createRedisPubSubconnection(config));

    } else {

      eventPubSub = new HashSetEventPubSub();
    }

    eventPubSub.addConsumer(EventSocket.BROADCAST_CONSUMER);

    return eventPubSub;
  }

  private static StatefulRedisPubSubConnection<String, String> createRedisPubSubconnection(
    final PubSubConfiguration config) {

    return RedisClient.create(RedisURI.Builder
      .redis(config.getPubSubHostname())
      .withPort(config.getPubSubPort())
      .withPassword(config.getPubSubPassword())
      .build())
      .connectPubSub();
  }
}
