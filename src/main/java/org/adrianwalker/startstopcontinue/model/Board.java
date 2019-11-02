package org.adrianwalker.startstopcontinue.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

public final class Board implements Serializable {

  private UUID id;
  private List<Note> starts;
  private List<Note> stops;
  private List<Note> continues;

  public UUID getId() {
    return id;
  }

  public Board setId(final UUID id) {
    this.id = id;
    return this;
  }

  public List<Note> getStarts() {
    return starts;
  }

  public Board setStarts(final List<Note> starts) {
    this.starts = starts;
    return this;
  }

  public List<Note> getStops() {
    return stops;
  }

  public Board setStops(final List<Note> stops) {
    this.stops = stops;
    return this;
  }

  public List<Note> getContinues() {
    return continues;
  }

  public Board setContinues(final List<Note> continues) {
    this.continues = continues;
    return this;
  }

  @Override
  public int hashCode() {

    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.id);
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

    Board other = (Board) obj;
    return Objects.equals(this.id, other.id);
  }
}
