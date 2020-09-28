package org.adrianwalker.startstopcontinue.pubsub;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import java.util.UUID;
import java.util.function.BiConsumer;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.fromJson;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.toJson;

public final class RedisEventPubSub implements EventPubSub {

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

    subscribeConnection.async().subscribe(boardId.toString());
  }

  @Override
  public void unsubscribe(final UUID boardId) {

    subscribeConnection.async().unsubscribe(boardId.toString());
  }

  @Override
  public void publish(final UUID boardId, final Event event) {

    publishConnection.async().publish(boardId.toString(), toJson(event));
  }

  @Override
  public void addConsumer(final BiConsumer<UUID, Event> consumer) {

    subscribeConnection.addListener(new RedisPubSubAdapter<>() {

      @Override
      public void message(final String channel, final String message) {

        consumer.accept(UUID.fromString(channel), fromJson(message, Event.class));
      }
    });
  }
}
