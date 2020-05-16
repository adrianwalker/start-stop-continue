package org.adrianwalker.startstopcontinue.model;

import java.util.List;

public final class Board extends IDable<Board> {

  private List<Note> starts;
  private List<Note> stops;
  private List<Note> continues;

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
}
