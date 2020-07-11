package org.adrianwalker.startstopcontinue.cli.commands;

import static java.util.stream.Collectors.joining;
import org.adrianwalker.startstopcontinue.cli.CommandLineInterface;

public final class HelpCommand implements Command {

  private final CommandLineInterface cli;

  public HelpCommand(final CommandLineInterface cli) {

    this.cli = cli;
  }

  @Override
  public String name() {
    return "help";
  }

  @Override
  public String eval(final String[] command) {

    return "Available commands:\n"
      + cli.getCommands()
        .stream()
        .map(c -> c.name())
        .sorted()
        .collect(joining("\n"));
  }
}
