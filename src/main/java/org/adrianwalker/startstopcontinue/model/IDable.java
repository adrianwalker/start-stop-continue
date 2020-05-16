package org.adrianwalker.startstopcontinue.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class IDable<T extends IDable> implements Serializable {

  private UUID id;

  public UUID getId() {
    return id;
  }

  public T setId(final UUID id) {
    this.id = id;
    return (T) this;
  }

  @Override
  public int hashCode() {

    return id.hashCode();
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

    IDable other = (IDable) obj;
    return Objects.equals(this.id, other.id);
  }
}
