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

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    ReadThroughCache cache = new LinkedHashMapCache(0, boardId -> new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));
    
    Board board = cache.read(id1);
    assertEquals(id1, board.getId());

    board = cache.read(id2);
    assertEquals(id2, board.getId());
  }

  @Test
  public void testNonZeroSizeReadThrough() {

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    ReadThroughCache cache = new LinkedHashMapCache(1, boardId -> new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));

    Board board = cache.read(id1);
    assertEquals(id1, board.getId());

    board = cache.read(id2);
    assertEquals(id2, board.getId());
  }

  @Test
  public void testCacheEviction() {

    UUID[] boardIds = {
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
    };

    int cacheSize = 1;

    ReadThroughCache cache = new LinkedHashMapCache(cacheSize, boardId -> new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));

    for (UUID boardId : boardIds) {     
      assertEquals(boardId, cache.read(boardId).getId());
    }
    
    cache.read(boardIds[0], Column.CONTINUE, UUID.randomUUID());
    cache.write(boardIds[1], Column.START, new Note().setId(UUID.randomUUID()));
    cache.delete(boardIds[2], Column.START, UUID.randomUUID());
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

    ReadThroughCache cache = new LinkedHashMapCache(10, boardId -> new Board().setId(boardId)
      .setStarts(new ArrayList<>())
      .setStops(new ArrayList<>())
      .setContinues(new ArrayList<>()));

    cache.write(id4, Column.START, note1);
    cache.write(id4, Column.STOP, note2);
    cache.write(id4, Column.CONTINUE, note3);

    assertEquals(id1, cache.read(id4, Column.START, id1).getId());
    assertEquals(id2, cache.read(id4, Column.STOP, id2).getId());
    assertEquals(id3, cache.read(id4, Column.CONTINUE, id3).getId());
   
    cache.delete(id4, Column.START, id1);
    cache.delete(id4, Column.STOP, id2);
    cache.delete(id4, Column.CONTINUE, id3);

    assertEquals(0, cache.read(id4).getStarts().size());
    assertEquals(0, cache.read(id4).getStops().size());
    assertEquals(0, cache.read(id4).getContinues().size());
  }
}
