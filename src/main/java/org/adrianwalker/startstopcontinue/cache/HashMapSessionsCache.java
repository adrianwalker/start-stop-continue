package org.adrianwalker.startstopcontinue.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class HashMapSessionsCache implements SessionsCache {

  private final Map<UUID, Long> cache;
  private final Consumer<UUID> deleteConsumer;

  public HashMapSessionsCache(final Consumer<UUID> deleteConsumer) {

    this.cache = new HashMap<>();
    this.deleteConsumer = deleteConsumer;
  }

  @Override
  public long incrementSessions(final UUID boardId) {

    return cache.compute(boardId, (k, v) -> (v == null) ? 1 : v + 1);
  }

  @Override
  public long decrementSessions(final UUID boardId) {

    long sessions = cache.compute(boardId, (k, v) -> (v == null) ? 0 : v - 1);

    if (sessions == 0) {
      cache.remove(boardId);
      deleteConsumer.accept(boardId);
    }
    
    return sessions;
  }
}
