package org.adrianwalker.startstopcontinue.configuration;

public final class HttpConfiguration {

  /*
  Environment variable names
   */
  private static final String HTTP_PORT = "STARTSTOPCONTINUE_HTTP_PORT";

  /*
  Configuration defaults
   */
  public static final String DEFAULT_HTTP_PORT = "8080";

  public int getHttpPort() {

    return Integer.valueOf(System.getenv().getOrDefault(HTTP_PORT, DEFAULT_HTTP_PORT));
  }
}
