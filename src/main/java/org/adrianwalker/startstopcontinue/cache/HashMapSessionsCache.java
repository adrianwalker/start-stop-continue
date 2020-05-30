package org.adrianwalker.startstopcontinue.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HashMapSessionsCache implements SessionsCache {

  private final static Logger LOGGER = LoggerFactory.getLogger(HashMapSessionsCache.class);

  private final Map<UUID, Long> cache;
  private final Consumer<UUID> deleteConsumer;

  public HashMapSessionsCache(final Consumer<UUID> deleteConsumer) {

    this.cache = new HashMap<>();
    this.deleteConsumer = deleteConsumer;
  }

  @Override
  public long incrementSessions(final UUID boardId) {

    long sessions = cache.compute(boardId, (k, v) -> (v == null) ? 1 : v + 1);

    LOGGER.info("boardId = {}, sessions = {}", boardId, sessions);

    return sessions;
  }

  @Override
  public long decrementSessions(final UUID boardId) {

    long sessions = cache.compute(boardId, (k, v) -> (v == null) ? 0 : v - 1);

    if (sessions == 0) {
      cache.remove(boardId);
      deleteConsumer.accept(boardId);
    }

    LOGGER.info("boardId = {}, sessions = {}", boardId, sessions);

    return sessions;
  }
}
