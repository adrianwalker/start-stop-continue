package org.adrianwalker.startstopcontinue.websocket;

import java.util.Set;
import javax.websocket.Session;

public interface SessionCache {

  Set<Session> get(Session session);

  void add(Session session);

  void remove(Session session);
}
