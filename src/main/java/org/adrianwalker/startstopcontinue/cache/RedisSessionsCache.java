package org.adrianwalker.startstopcontinue.cache;

import io.lettuce.core.api.StatefulRedisConnection;
import java.util.UUID;
import java.util.function.Consumer;

public class RedisSessionsCache implements SessionsCache {

  private static final String FIELD_SEPERATOR = "/";
  private static final String SESSIONS = "sessions";

  private final long expirySeconds;
  private final StatefulRedisConnection<String, String> redisConnection;
  private final Consumer<UUID> deleteConsumer;

  public RedisSessionsCache(
    final long expirySeconds,
    final StatefulRedisConnection<String, String> redisConnection,
    final Consumer<UUID> deleteConsumer) {

    this.expirySeconds = expirySeconds;
    this.redisConnection = redisConnection;
    this.deleteConsumer = deleteConsumer;
  }

  @Override
  public long incrementSessions(final UUID boardId) {

    long sessions = incrby(key(boardId), 1);
    expire(boardId.toString(), expirySeconds);

    return sessions;
  }

  @Override
  public long decrementSessions(final UUID boardId) {

    long sessions = incrby(key(boardId), -1);
    expire(boardId.toString(), expirySeconds);

    if (sessions == 0) {
      del(key(boardId));
      deleteConsumer.accept(boardId);
    }

    return sessions;
  }

  private String key(final UUID boardId) {

    return boardId.toString() + FIELD_SEPERATOR + SESSIONS;
  }

  private Long incrby(final String key, final long incrby) {

    return redisConnection.sync().incrby(key, incrby);
  }

  private void del(final String... keys) {

    redisConnection.async().del(keys);
  }

  private void expire(final String key, final long seconds) {

    redisConnection.async().expire(key, seconds);
  }
}
