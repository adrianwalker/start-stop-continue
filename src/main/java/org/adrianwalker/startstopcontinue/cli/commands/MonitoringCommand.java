package org.adrianwalker.startstopcontinue.cli.commands;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import static org.adrianwalker.startstopcontinue.Monitoring.memoryUsage;
import static org.adrianwalker.startstopcontinue.Monitoring.fileDescriptors;

public final class MonitoringCommand implements Command {

  @Override
  public String name() {
    return "monitoring";
  }

  @Override
  public String eval(final String[] command) {

    String memoryUsage = memoryUsage().entrySet().stream()
      .map(e -> format("%s = %.2f", e.getKey(), e.getValue()))
      .collect(joining("\n"));

    String fileDescriptors = fileDescriptors().entrySet().stream()
      .map(e -> format("%s = %s", e.getKey(), e.getValue()))
      .collect(joining("\n"));

    return Stream.of(memoryUsage, fileDescriptors).collect(joining("\n"));
  }
}
