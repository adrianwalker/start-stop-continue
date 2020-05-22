package org.adrianwalker.startstopcontinue.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import java.util.UUID;
import java.util.function.Consumer;
import org.adrianwalker.startstopcontinue.configuration.CacheConfiguration;

public final class SessionsCacheFactory {

  private final CacheConfiguration config;
  private final Consumer<UUID> deleteConsumer;

  public SessionsCacheFactory(final CacheConfiguration config, final Consumer<UUID> deleteConsumer) {

    this.config = config;
    this.deleteConsumer = deleteConsumer;
  }

  public SessionsCache create() {

    SessionsCache cache;

    if (!config.getCacheHostname().isEmpty() && config.getCachePort() > 0) {

      cache = new RedisSessionsCache(
        config.getCacheExpirySeconds(),
        createRedisConnection(config),
        deleteConsumer);

    } else {

      cache = new HashMapSessionsCache(deleteConsumer);
    }

    return cache;
  }

  private static StatefulRedisConnection<String, String> createRedisConnection(final CacheConfiguration config) {

    return RedisClient.create(RedisURI.Builder
      .redis(config.getCacheHostname())
      .withPort(config.getCachePort())
      .withPassword(config.getCachePassword())
      .build())
      .connect();
  }
}
