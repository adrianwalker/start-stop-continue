package org.adrianwalker.startstopcontinue.cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import java.util.UUID;
import java.util.function.Function;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.configuration.CacheConfiguration;

public final class CacheFactory {

  private final CacheConfiguration config;
  private final Function<UUID, Board> readThroughFunction;

  public CacheFactory(
    final CacheConfiguration config,
    final Function<UUID, Board> readThroughFunction) {

    this.config = config;
    this.readThroughFunction = readThroughFunction;
  }

  public Cache create() {

    Cache cache;

    if (!config.getCacheHostname().isEmpty() && config.getCachePort() > 0) {

      cache = new RedisCache(createRedisConnection(config), readThroughFunction);

    } else {

      cache = new LinkedHashMapLRUCache(config.getCacheSize(), readThroughFunction);
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
