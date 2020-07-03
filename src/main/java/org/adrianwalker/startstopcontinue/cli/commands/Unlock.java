package org.adrianwalker.startstopcontinue.cli.commands;

import static java.lang.String.format;
import static java.util.Map.of;
import java.util.UUID;
import static org.adrianwalker.startstopcontinue.cli.commands.CommandUtil.toJson;
import org.adrianwalker.startstopcontinue.pubsub.Event;
import org.adrianwalker.startstopcontinue.pubsub.EventPubSub;
import org.adrianwalker.startstopcontinue.service.Service;

public final class Unlock implements Command {

  private final Service service;
  private final EventPubSub eventPubSub;

  public Unlock(final Service service, final EventPubSub eventPubSub) {

    this.service = service;
    this.eventPubSub = eventPubSub;
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

    eventPubSub.publish(boardId, new Event()
      .setId(boardId.toString())
      .setData(toJson(of("id", boardId.toString(), "locked", locked))));

    return format("board %s is %s", boardId, locked ? "locked" : "unlocked");
  }
}
