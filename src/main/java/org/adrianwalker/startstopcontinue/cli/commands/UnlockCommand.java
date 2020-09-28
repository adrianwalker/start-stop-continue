package org.adrianwalker.startstopcontinue.cli.commands;

import static java.lang.String.format;
import static java.util.Map.of;
import java.util.UUID;
import org.adrianwalker.startstopcontinue.pubsub.Event;
import org.adrianwalker.startstopcontinue.service.Service;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.toJson;

public final class UnlockCommand implements Command {

  private final Service service;

  public UnlockCommand(final Service service) {

    this.service = service;
  }

  @Override
  public String name() {
    return "unlock";
  }

  @Override
  public String eval(final String[] command) {

    if (command.length < 2) {
      return "usage: unlock <BOARD ID>";
    }

    UUID boardId = UUID.fromString(command[1]);

    service.unlockBoard(boardId);
    boolean locked = service.readBoard(boardId).isLocked();

    service.publishEvent(boardId, new Event()
      .setSessionId("")
      .setData(toJson(of("boardId", boardId.toString(), "locked", locked))));

    return format("Board %s is %s", boardId, locked ? "locked" : "unlocked");
  }
}
