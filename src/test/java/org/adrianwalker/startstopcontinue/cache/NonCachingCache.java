package org.adrianwalker.startstopcontinue.cache;

import java.util.UUID;
import java.util.function.Function;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public final class NonCachingCache implements Cache {

  private final Function<UUID, Board> readThroughFunction;

  public NonCachingCache(final Function<UUID, Board> readThroughFunction) {
    this.readThroughFunction = readThroughFunction;
  }

  @Override
  public Board read(final UUID boardId) {
    return readThroughFunction.apply(boardId);
  }

  @Override
  public Note read(final UUID boardId, final Column column, final UUID noteId) {
    return new Note().setId(noteId);
  }

  @Override
  public void write(final UUID boardId, final Column column, final Note note) {
  }

  @Override
  public void delete(final UUID boardId, final Column column, final UUID noteId) {
  }

  @Override
  public void delete(final UUID boardId) {
  }
}
