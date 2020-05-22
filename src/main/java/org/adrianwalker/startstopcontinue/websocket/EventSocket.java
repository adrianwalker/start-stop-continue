package org.adrianwalker.startstopcontinue.websocket;

import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.adrianwalker.startstopcontinue.cache.SessionsCache;
import org.adrianwalker.startstopcontinue.pubsub.Event;

@ServerEndpoint("")
public final class EventSocket {

  private static final String PING_MESSAGE = "ping";
  private static final String BOARD_ID = "boardId";
  private static final Map<UUID, Set<Session>> SESSIONS = new HashMap<>();
  private final EventPubSub eventPubSub;
  private final SessionsCache sessionsCache;

  public static final BiConsumer<UUID, Event> BROADCAST_CONSUMER = (boardId, event) -> broadcast(boardId, event);

  public EventSocket(final EventPubSub eventPubSub, final SessionsCache sessionsCache) {

    this.eventPubSub = eventPubSub;
    this.sessionsCache = sessionsCache;
  }

  @OnOpen
  public void onOpen(final Session session) {

    UUID boardId = getBoardId(session);

    if (!SESSIONS.containsKey(boardId)) {
      SESSIONS.put(boardId, new HashSet<>());
      eventPubSub.subscribe(boardId);
    }

    SESSIONS.get(boardId).add(session);
    sessionsCache.incrementSessions(boardId);
  }

  @OnMessage
  public void onMessage(final Session session, final String message) {

    if (message.isEmpty()) {
      return;
    }

    if (PING_MESSAGE.equals(message)) {
      return;
    }

    eventPubSub.publish(getBoardId(session), new Event().setId(session.getId()).setData(message));
  }

  @OnClose
  public void onClose(final Session session) {

    UUID boardId = getBoardId(session);
    SESSIONS.get(boardId).remove(session);
    sessionsCache.decrementSessions(boardId);
    
    if (SESSIONS.get(boardId).isEmpty()) {
      SESSIONS.remove(boardId);
    }

    if (!SESSIONS.containsKey(boardId)) {
      eventPubSub.unsubscribe(boardId);
    }
  }

  @OnError
  public void onError(final Session session, final Throwable t) {

    onClose(session);

    throw new RuntimeException(t);
  }

  private static void broadcast(final UUID boardId, final Event event) {

    SESSIONS.get(boardId).stream()
      .filter(session -> session.isOpen())
      .filter(openSession -> !openSession.getId().equals(event.getId()))
      .forEach(peerSession -> {
        try {
          peerSession.getBasicRemote().sendText(event.getData());
        } catch (final IOException ioe) {
          throw new RuntimeException(ioe);
        }
      });
  }

  private UUID getBoardId(final Session session) {

    return UUID.fromString(session.getPathParameters().get(BOARD_ID));
  }
}
