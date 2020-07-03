package org.adrianwalker.startstopcontinue.cli.commands;

public interface Command {

  String name();

  String eval(String[] args);
}
