package org.adrianwalker.startstopcontinue.cache.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class RedisEventPubSub implements EventPubSub {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final StatefulRedisPubSubConnection<String, String> publishConnection;
  private final StatefulRedisPubSubConnection<String, String> subscribeConnection;

  public RedisEventPubSub(
    final StatefulRedisPubSubConnection<String, String> publishConnection,
    final StatefulRedisPubSubConnection<String, String> subscribeConnection) {

    this.publishConnection = publishConnection;
    this.subscribeConnection = subscribeConnection;
  }

  @Override
  public void subscribe(final UUID boardId) {

    this.subscribeConnection.async().subscribe(boardId.toString());
  }

  @Override
  public void unsubscribe(final UUID boardId) {

    this.subscribeConnection.async().unsubscribe(boardId.toString());
  }

  @Override
  public void publish(final UUID boardId, final Event event) {

    try {
      publishConnection.async().publish(boardId.toString(), OBJECT_MAPPER.writeValueAsString(event));
    } catch (final JsonProcessingException jpe) {
      throw new RuntimeException(jpe);
    }
  }

  @Override
  public void addConsumer(final BiConsumer<UUID, Event> consumer) {

    subscribeConnection.addListener(new RedisPubSubAdapter<>() {

      @Override
      public void message(final String channel, final String message) {

        try {
          consumer.accept(UUID.fromString(channel), OBJECT_MAPPER.readValue(message, Event.class));
        } catch (final IOException ioe) {
          throw new RuntimeException(ioe);
        }
      }
    });
  }
}
