package org.adrianwalker.startstopcontinue.cache;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.model.Column;
import org.adrianwalker.startstopcontinue.model.Note;

public interface Cache {

  Board read(final UUID boardId);

  Note read(final UUID boardId, final Column column, final UUID noteId);

  void write(final UUID boardId, final Column column, final Note note);

  void delete(final UUID boardId, final Column column, final UUID noteId);
}
