package org.adrianwalker.startstopcontinue.websocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.websocket.Session;

public final class HashMapSessionCache implements SessionCache {

  private static final String BOARD_ID = "boardId";

  private static Map<UUID, Set<Session>> cache;

  public HashMapSessionCache() {
    cache = new HashMap<>();
  }

  @Override
  public Set<Session> get(final Session session) {

    return cache.get(getBoardId(session));
  }

  @Override
  public void add(final Session session) {

    cache.computeIfAbsent(getBoardId(session), m -> new HashSet<>()).add(session);
  }

  @Override
  public void remove(final Session session) {

    UUID boardId = getBoardId(session);
    Set<Session> sessions = cache.get(boardId);

    sessions.remove(session);

    if (sessions.isEmpty()) {
      cache.remove(boardId);
    }
  }

  private static UUID getBoardId(final Session session) {

    return UUID.fromString(session.getPathParameters().get(BOARD_ID));
  }
}
