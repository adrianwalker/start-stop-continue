package org.adrianwalker.startstopcontinue.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import org.adrianwalker.startstopcontinue.cli.commands.Command;

public final class CommandLineInterface {

  private static final String EMPTY = " ";
  public static final String PROMPT = "> ";

  private final int port;
  private final Map<String, Command> commandInstances;

  public CommandLineInterface(final int port) {

    this.port = port;

    commandInstances = new HashMap<>();
  }

  public CommandLineInterface addCommand(final Command command) {

    commandInstances.put(command.name(), command);

    return this;
  }

  public void start() {

    new Thread(() -> {
      ServerSocket serverSocket;
      try {
        serverSocket = new ServerSocket(port);
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      }

      while (true) {
        try {
          accept(serverSocket.accept());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  private void accept(final Socket socket) throws InterruptedException, ExecutionException {

    new Thread(() -> {
      try ( OutputStream os = socket.getOutputStream();  InputStream is = socket.getInputStream();) {

        Scanner scanner = new Scanner(is);
        PrintWriter writer = new PrintWriter(os);

        while (prompt(writer, scanner)) {
          print(writer, eval(read(scanner)));
        }
      } catch (final IOException ioe) {
        throw new RuntimeException(ioe);
      } finally {
        try {
          socket.close();
        } catch (final IOException ioe) {
          throw new RuntimeException(ioe);
        }
      }
    }).start();
  }

  public boolean prompt(final PrintWriter writer, final Scanner scanner) {

    writer.write(PROMPT);
    writer.flush();

    return scanner.hasNextLine();
  }

  private String read(final Scanner scanner) {

    return scanner.nextLine().strip();
  }

  private String eval(final String read) {

    if (read.isEmpty()) {
      return null;
    }

    String[] command = read.split(EMPTY);

    if (command.length == 0) {
      return null;
    }

    if (commandInstances.containsKey(command[0])) {
      try {
        return commandInstances.get(command[0]).eval(command);
      } catch (final Exception e) {
        return e.getMessage();
      }
    }

    return command[0] + ": command not found";
  }

  private void print(final PrintWriter writer, final String eval) {

    if (null == eval) {
      return;
    }

    writer.write(eval);
    writer.write("\n");
    writer.flush();
  }
}
