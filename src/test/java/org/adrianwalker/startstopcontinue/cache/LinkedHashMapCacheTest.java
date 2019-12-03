package org.adrianwalker.startstopcontinue.cache;

import java.util.ArrayList;
import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;
import org.junit.Test;
import static org.junit.Assert.*;

public final class LinkedHashMapCacheTest {

  @Test
  public void testZeroSizeReadThrough() {

    Cache cache = new LinkedHashMapCache(0);
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Board board = cache.readThrough(id1, t -> new Board().setId(id1)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    assertEquals(id1, board.getId());

    board = cache.readThrough(id1, t -> new Board().setId(id2)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    assertEquals(id2, board.getId());
  }

  @Test
  public void testNonZeroSizeReadThrough() {

    Cache cache = new LinkedHashMapCache(1);
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Board board = cache.readThrough(id1, t -> new Board().setId(id1)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    assertEquals(id1, board.getId());

    board = cache.readThrough(id1, t -> new Board().setId(id2)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    assertEquals(id1, board.getId());
  }

  @Test
  public void testCacheEviction() {

    UUID[] ids = {
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
    };

    int cacheSize = ids.length - 1;
    Cache cache = new LinkedHashMapCache(cacheSize);

    for (UUID id : ids) {
      cache.readThrough(id, t -> new Board().setId(id)
        .setStarts(new ArrayList<>())
        .setStops(new ArrayList<>())
        .setContinues(new ArrayList<>()));
    }

    try {
      cache.readThrough(ids[0], t -> null);
      fail();
    } catch (NullPointerException npe) {
    }

    assertEquals(ids[ids.length - 1], cache.readThrough(ids[ids.length - 1], t -> null).getId());
  }

  @Test
  public void testWriteReadDelete() {

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    UUID id4 = UUID.randomUUID();

    Note note1 = new Note().setId(id1);
    Note note2 = new Note().setId(id2);
    Note note3 = new Note().setId(id3);

    Cache cache = new LinkedHashMapCache(10);
    cache.readThrough(id4, f -> new Board().setId(id4)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    
    cache.write(id4, Column.START, note1);
    cache.write(id4, Column.STOP, note2);
    cache.write(id4, Column.CONTINUE, note3);

    assertEquals(id1, cache.read(id4, Column.START, id1).getId());
    assertEquals(id2, cache.read(id4, Column.STOP, id2).getId());
    assertEquals(id3, cache.read(id4, Column.CONTINUE, id3).getId());

    assertEquals(id1, cache.readThrough(id4, f -> null).getStarts().get(0).getId());
    assertEquals(id2, cache.readThrough(id4, f -> null).getStops().get(0).getId());
    assertEquals(id3, cache.readThrough(id4, f -> null).getContinues().get(0).getId());

    cache.delete(id4, Column.START, id1);
    cache.delete(id4, Column.STOP, id2);
    cache.delete(id4, Column.CONTINUE, id3);

    assertEquals(0, cache.readThrough(id4, f -> null).getStarts().size());
    assertEquals(0, cache.readThrough(id4, f -> null).getStops().size());
    assertEquals(0, cache.readThrough(id4, f -> null).getContinues().size());
  }
}
