package org.adrianwalker.startstopcontinue.websocket;

import java.io.IOException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("")
public final class EventSocket {

  private final SessionCache sessionCache;
  
  public EventSocket(final SessionCache sessionCache) {
    
    this.sessionCache = sessionCache;
  }

  @OnOpen
  public void onOpen(final Session session) {

    sessionCache.add(session);
  }

  @OnMessage
  public void onMessage(final Session session, final String message) {

    broadcast(session, message);
  }

  @OnClose
  public void onClose(final Session session) {

    sessionCache.remove(session);
  }

  @OnError
  public void onError(final Throwable cause) {

    throw new RuntimeException(cause);
  }

  private void broadcast(final Session senderSession, final String json) {

    sessionCache.get(senderSession).stream()
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
