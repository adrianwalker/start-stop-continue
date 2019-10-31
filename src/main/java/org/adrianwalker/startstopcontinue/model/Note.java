package org.adrianwalker.startstopcontinue.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class Note implements Serializable {

  private UUID id;
  private String color;
  private String text;

  public Note() {
    id = UUID.randomUUID();
  }

  public UUID getId() {
    return id;
  }

  public Note setId(final UUID id) {
    this.id = id;
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

  @Override
  public int hashCode() {

    int hash = 5;
    hash = 79 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    Note other = (Note) obj;
    return Objects.equals(this.id, other.id);
  }

}
