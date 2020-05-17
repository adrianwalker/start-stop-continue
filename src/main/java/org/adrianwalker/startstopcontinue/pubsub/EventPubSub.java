package org.adrianwalker.startstopcontinue.pubsub;

import java.util.UUID;
import java.util.function.BiConsumer;

public interface EventPubSub {

  void subscribe(UUID boardId);

  void unsubscribe(UUID boardId);

  void publish(UUID boardId, Event event);

  void addConsumer(BiConsumer<UUID, Event> consumer);
}
