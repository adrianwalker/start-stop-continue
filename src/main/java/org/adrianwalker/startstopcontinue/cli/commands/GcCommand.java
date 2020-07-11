package org.adrianwalker.startstopcontinue.cli.commands;

public final class GcCommand implements Command {

  @Override
  public String name() {
    return "gc";
  }

  @Override
  public String eval(final String[] args) {

    System.gc();

    return "Garbage collection requested";
  }
}
