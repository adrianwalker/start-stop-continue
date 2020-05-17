package org.adrianwalker.startstopcontinue.configuration;

public final class CacheConfiguration {

  /*
  Environment variable names
   */
  private static final String CACHE_HOSTNAME = "STARTSTOPCONTINUE_CACHE_HOSTNAME";
  private static final String CACHE_PORT = "STARTSTOPCONTINUE_CACHE_PORT";
  private static final String CACHE_PASSWORD = "STARTSTOPCONTINUE_CACHE_PASSWORD";
  private static final String CACHE_SIZE = "STARTSTOPCONTINUE_CACHE_SIZE";

  /*
  Configuration defaults
   */
  public static final String DEFAULT_CACHE_HOSTNAME = "";
  public static final String DEFAULT_CACHE_PORT = "0";
  public static final String DEFAULT_CACHE_PASSWORD = "";
  public static final String DEFAULT_CACHE_SIZE = "32";
  
  public String getCacheHostname() {

    return System.getenv().getOrDefault(CACHE_HOSTNAME, DEFAULT_CACHE_HOSTNAME);
  }

  public int getCachePort() {

    return Integer.valueOf(System.getenv().getOrDefault(CACHE_PORT, DEFAULT_CACHE_PORT));
  }

  public String getCachePassword() {

    return System.getenv().getOrDefault(CACHE_PASSWORD, DEFAULT_CACHE_PASSWORD);
  }

  public int getCacheSize() {

    return Integer.valueOf(System.getenv().getOrDefault(CACHE_SIZE, DEFAULT_CACHE_SIZE));
  }
}
