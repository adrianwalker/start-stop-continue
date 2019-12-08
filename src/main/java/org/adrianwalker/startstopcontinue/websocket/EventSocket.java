package org.adrianwalker.startstopcontinue.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("")
public final class EventSocket {

  private static final String BOARD_ID = "boardId";
  private static final Map<UUID, Set<Session>> BOARD_ID_SESSIONS = new HashMap<>();

  public EventSocket() {
  }

  @OnOpen
  public void onOpen(final Session session) {

    addSession(session, getBoardId(session));
  }

  private static UUID getBoardId(final Session session) {
    
    return UUID.fromString(session.getPathParameters().get(BOARD_ID));
  }

  @OnMessage
  public void onMessage(final Session session, final String message) {

    broadcast(session, message);
  }

  @OnClose
  public void onClose(final Session session) {

    removeSession(session);
  }

  @OnError
  public void onError(final Throwable cause) {

    throw new RuntimeException(cause);
  }

  private void addSession(final Session session, final UUID boardId) {

    BOARD_ID_SESSIONS.computeIfAbsent(boardId, m -> new HashSet<>()).add(session);
  }

  private void removeSession(final Session session) {

    UUID boardId = getBoardId(session);
    Set<Session> sessions = BOARD_ID_SESSIONS.get(boardId);

    sessions.remove(session);

    if (sessions.isEmpty()) {
      BOARD_ID_SESSIONS.remove(boardId);
    }
  }

  private void broadcast(final Session senderSession, final String json) {

    BOARD_ID_SESSIONS.get(getBoardId(senderSession)).stream()
      .filter(session -> session.isOpen())
      .filter(openSession -> !openSession.equals(senderSession))
      .forEach(peerSession -> {
        try {
          peerSession.getBasicRemote().sendText(json);
        } catch (final IOException ioe) {
          throw new RuntimeException(ioe);
        }
      });
  }
}
