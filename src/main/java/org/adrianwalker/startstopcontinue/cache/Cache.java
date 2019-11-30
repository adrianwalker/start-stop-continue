package org.adrianwalker.startstopcontinue.cache;

import java.util.UUID;
import java.util.function.Function;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public interface Cache {

  Board readThrough(final UUID boardId, final Function<UUID, Board> f);

  Note read(final UUID boardId, final Column column, final UUID noteId);

  void write(final UUID boardId, final Column column, final Note note);

  void delete(final UUID boardId, final Column column, final UUID noteId);
}
