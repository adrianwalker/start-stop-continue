package org.adrianwalker.startstopcontinue.pubsub;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class HashSetEventPubSub implements EventPubSub {

  private final Set<UUID> subscriptions;
  private final Set<BiConsumer<UUID, Event>> consumers;

  public HashSetEventPubSub() {

    subscriptions = new HashSet<>();
    consumers = new HashSet<>();
  }

  @Override
  public void subscribe(final UUID boardId) {

    subscriptions.add(boardId);
  }

  @Override
  public void unsubscribe(final UUID boardId) {

    subscriptions.remove(boardId);
  }

  @Override
  public void publish(final UUID boardId, final Event event) {

    if (!subscriptions.contains(boardId)) {
      return;
    }

    consumers.forEach(consumer -> consumer.accept(boardId, event));
  }

  @Override
  public void addConsumer(final BiConsumer<UUID, Event> consumer) {

    consumers.add(consumer);
  }
}
