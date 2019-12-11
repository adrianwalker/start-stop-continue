package org.adrianwalker.startstopcontinue.cache.pubsub;

public final class Event {

  private String id;
  private String data;

  public String getId() {
    return id;
  }

  public Event setId(final String id) {
    this.id = id;
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
