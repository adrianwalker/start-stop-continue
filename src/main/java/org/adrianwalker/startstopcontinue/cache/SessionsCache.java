package org.adrianwalker.startstopcontinue.cache;

import java.util.UUID;

public interface SessionsCache {

  long incrementSessions(UUID boardId);

  long decrementSessions(UUID boardId);
}
