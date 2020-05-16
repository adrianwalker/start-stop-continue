package org.adrianwalker.startstopcontinue.model;

import java.util.Date;

public final class Note extends IDable<Note> {

  private Date created;
  private String color;
  private String text;

  public Date getCreated() {
    return created;
  }

  public Note setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public String getColor() {
    return color;
  }

  public Note setColor(final String color) {
    this.color = color;
    return this;
  }

  public String getText() {
    return text;
  }

  public Note setText(final String text) {
    this.text = text;
    return this;
  }
}
