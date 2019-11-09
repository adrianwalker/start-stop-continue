package org.adrianwalker.startstopcontinue.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class Cache<T, R> {

  private static final float LOAD_FACTOR = 0.75f;
  private static final boolean ACCESS_ORDER = true;
  private final Map<T, R> cache;

  public Cache(final int cacheSize, final boolean sync) {

    LinkedHashMap map = new LinkedHashMap(cacheSize + 1, LOAD_FACTOR, ACCESS_ORDER) {

      @Override
      public boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > cacheSize;
      }
    };

    if (sync) {
      this.cache = Collections.synchronizedMap(map);
    } else {
      this.cache = map;
    }
  }

  public R readThrough(final T key, final Function<T, R> f) {

    R value;
    if (cache.containsKey(key)) {
      value = cache.get(key);
    } else {
      value = f.apply(key);
      cache.put(key, value);
    }

    return value;
  }
}
