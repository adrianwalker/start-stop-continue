package org.adrianwalker.startstopcontinue.cli.commands;

import static java.lang.String.format;
import org.adrianwalker.startstopcontinue.service.Service;

public final class CacheCommand implements Command {

  private final Service service;

  public CacheCommand(final Service service) {

    this.service = service;
  }

  @Override
  public String name() {
    return "cache";
  }

  @Override
  public String eval(final String[] command) {

    if (command.length < 2) {
      return "usage: cache [size|purge]";
    }

    if ("size".equals(command[1])) {
      return size();
    } else if ("purge".equals(command[1])) {
      return purge();
    } else {
      return "usage: cache [size|purge]";
    }
  }

  private String size() {

    long cacheSize = service.cacheSize();
    return format("cacheSize = %s", cacheSize);
  }

  private String purge() {

    service.cachePurge();
    return format("Cache purged");
  }
}
