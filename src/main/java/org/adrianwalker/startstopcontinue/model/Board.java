package org.adrianwalker.startstopcontinue.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Board implements Serializable {

  private UUID id;
  private Set<Note> starts;
  private Set<Note> stops;
  private Set<Note> continues;

  public Board() {
    id = UUID.randomUUID();
    starts = new HashSet<>();
    stops = new HashSet<>();
    continues = new HashSet<>();
  }

  public UUID getId() {
    return id;
  }

  public Board setId(final UUID id) {
    this.id = id;
    return this;
  }

  public Set<Note> getStarts() {
    return starts;
  }

  public Board setStarts(final Set<Note> starts) {
    this.starts = starts;
    return this;
  }

  public Set<Note> getStops() {
    return stops;
  }

  public Board setStops(final Set<Note> stops) {
    this.stops = stops;
    return this;
  }

  public Set<Note> getContinues() {
    return continues;
  }

  public Board setContinues(final Set<Note> continues) {
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
