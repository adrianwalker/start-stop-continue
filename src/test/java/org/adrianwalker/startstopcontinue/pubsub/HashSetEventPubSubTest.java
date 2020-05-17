package org.adrianwalker.startstopcontinue.pubsub;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.junit.Test;
import static org.junit.Assert.*;

public final class HashSetEventPubSubTest {

  @Test
  public void testSubscribePublishUnsubscribe() {

    UUID boardId = UUID.randomUUID();
    String eventId = "test event ID";
    String eventData = "test data";

    Event event = new Event()
      .setId(eventId)
      .setData(eventData);

    BiConsumer<UUID, Event> consumer = (b, e) -> {
      assertEquals(boardId, b);
      assertEquals(eventId, e.getId());
      assertEquals(eventData, e.getData());
    };

    EventPubSub pubSub = new HashSetEventPubSub();
    pubSub.subscribe(boardId);
    pubSub.addConsumer(consumer);
    pubSub.publish(boardId, event);
    pubSub.unsubscribe(boardId);
  }

  @Test
  public void testDuplicateConsumers() {

    UUID boardId = UUID.randomUUID();

    AtomicInteger consumerCallCount = new AtomicInteger(0);
    BiConsumer<UUID, Event> consumer = (b, e) -> {
      consumerCallCount.incrementAndGet();
    };

    EventPubSub pubSub = new HashSetEventPubSub();
    pubSub.subscribe(boardId);
    pubSub.addConsumer(consumer);
    pubSub.addConsumer(consumer);
    pubSub.addConsumer(consumer);
    pubSub.publish(boardId, new Event());
    pubSub.unsubscribe(boardId);

    assertEquals(1, consumerCallCount.get());
  }

  @Test
  public void testNoSubscribers() {

    UUID boardId = UUID.randomUUID();

    BiConsumer<UUID, Event> consumer = (bi, e) -> {
      fail();
    };

    EventPubSub pubSub = new HashSetEventPubSub();
    pubSub.subscribe(boardId);
    pubSub.addConsumer(consumer);
    pubSub.unsubscribe(boardId);
    pubSub.publish(boardId, new Event());
  }
}
