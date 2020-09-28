package org.adrianwalker.startstopcontinue.pubsub;

public final class Event {

  private String sessionId;
  private String data;

  public String getSessionId() {
    return sessionId;
  }

  public Event setSessionId(final String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public String getData() {
    return data;
  }

  public Event setData(final String data) {
    this.data = data;
    return this;
  }
}
