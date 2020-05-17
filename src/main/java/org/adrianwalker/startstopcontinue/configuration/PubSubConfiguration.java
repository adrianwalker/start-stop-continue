package org.adrianwalker.startstopcontinue.configuration;

public final class PubSubConfiguration {

  /*
  Environment variable names
   */
  private static final String PUBSUB_HOSTNAME = "STARTSTOPCONTINUE_PUBSUB_HOSTNAME";
  private static final String PUBSUB_PORT = "STARTSTOPCONTINUE_PUBSUB_PORT";
  private static final String PUBSUB_PASSWORD = "STARTSTOPCONTINUE_PUBSUB_PASSWORD";

  /*
  Configuration defaults
   */
  public static final String DEFAULT_PUBSUB_HOSTNAME = "";
  public static final String DEFAULT_PUBSUB_PORT = "0";
  public static final String DEFAULT_PUBSUB_PASSWORD = "";
  
  public String getPubSubHostname() {

    return System.getenv().getOrDefault(PUBSUB_HOSTNAME, DEFAULT_PUBSUB_HOSTNAME);
  }

  public int getPubSubPort() {

    return Integer.valueOf(System.getenv().getOrDefault(PUBSUB_PORT, DEFAULT_PUBSUB_PORT));
  }

  public String getPubSubPassword() {

    return System.getenv().getOrDefault(PUBSUB_PASSWORD, DEFAULT_PUBSUB_PASSWORD);
  }
}
