package org.adrianwalker.startstopcontinue.configuration;

public final class CommandLineInterfaceConfiguration {

  /*
  Environment variable names
   */
  private static final String CLI_PORT = "STARTSTOPCONTINUE_CLI_PORT";

  /*
  Configuration defaults
   */
  public static final String DEFAULT_CLI_PORT = "9090";

  public int getHttpPort() {

    return Integer.valueOf(System.getenv().getOrDefault(CLI_PORT, DEFAULT_CLI_PORT));
  }
}
