package org.adrianwalker.startstopcontinue.cache;

import org.junit.Test;
import static org.junit.Assert.*;

public final class LinkedHashMapCacheTest {

  @Test
  public void testZeroSizeReadThrough() {

    for (boolean sync : new boolean[]{true, false}) {

      Cache<String, String> cache = new LinkedHashMapCache<>(0, sync);

      String value = cache.readThrough("abc", t -> "def");
      assertEquals("def", value);

      value = cache.readThrough("abc", t -> "ghi");
      assertEquals("ghi", value);
    }
  }

  @Test
  public void testNonZeroSizeReadThrough() {

    for (boolean sync : new boolean[]{true, false}) {

      Cache<String, String> cache = new LinkedHashMapCache<>(1, sync);

      String value = cache.readThrough("abc", t -> "def");
      assertEquals("def", value);

      value = cache.readThrough("abc", t -> "ghi");
      assertEquals("def", value);
    }
  }

  @Test
  public void testCacheEviction() {

    int cacheSize = 10;
    Cache<Integer, Integer> cache = new LinkedHashMapCache<>(cacheSize, false);

    for (int i = 0; i < cacheSize + 1; i++) {
      int j = i, k = i;
      cache.readThrough(j, t -> k);
    }

    assertEquals(null, cache.readThrough(0, t -> null));
    assertEquals(10, cache.readThrough(10, t -> null).intValue());
  }
}
