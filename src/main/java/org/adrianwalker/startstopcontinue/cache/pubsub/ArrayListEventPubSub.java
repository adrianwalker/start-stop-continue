package org.adrianwalker.startstopcontinue.cache.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class ArrayListEventPubSub implements EventPubSub {

  private final List<UUID> subscriptions;
  private final List<BiConsumer<UUID, Event>> consumers;

  public ArrayListEventPubSub() {

    subscriptions = new ArrayList<>();
    consumers = new ArrayList<>();
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
