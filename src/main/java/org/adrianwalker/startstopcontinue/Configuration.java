package org.adrianwalker.startstopcontinue;

import java.nio.file.Path;

public final class Configuration {

  /*
  Environment variable names
   */
  private static final String HTTP_PORT = "STARTSTOPCONTINUE_HTTP_PORT";

  private static final String CACHE_HOSTNAME = "STARTSTOPCONTINUE_CACHE_HOSTNAME";
  private static final String CACHE_PORT = "STARTSTOPCONTINUE_CACHE_PORT";
  private static final String CACHE_PASSWORD = "STARTSTOPCONTINUE_CACHE_PASSWORD";
  private static final String CACHE_SIZE = "STARTSTOPCONTINUE_CACHE_SIZE";

  private static final String DATA_ENDPOINT = "STARTSTOPCONTINUE_DATA_ENDPOINT";
  private static final String DATA_PORT = "STARTSTOPCONTINUE_DATA_PORT";
  private static final String DATA_ACCESS_KEY = "STARTSTOPCONTINUE_DATA_ACCESS_KEY";
  private static final String DATA_SECRET_KEY = "STARTSTOPCONTINUE_DATA_SECRET_KEY";
  private static final String DATA_THREADS = "STARTSTOPCONTINUE_DATA_THREADS";
  private static final String DATA_PATH = "STARTSTOPCONTINUE_DATA_PATH";
  private static final String DATA_SIZE = "STARTSTOPCONTINUE_DATA_SIZE";

  /*
  Configuration defaults
   */
  private static final String DEFAULT_HTTP_PORT = "8080";

  private static final String DEFAULT_CACHE_HOSTNAME = "";
  private static final String DEFAULT_CACHE_PORT = "0";
  private static final String DEFAULT_CACHE_PASSWORD = "";
  private static final String DEFAULT_CACHE_SIZE = "32";

  private static final String DEFAULT_DATA_ENDPOINT = "";
  private static final String DEFAULT_DATA_PORT = "0";
  private static final String DEFAULT_DATA_ACCESS_KEY = "";
  private static final String DEFAULT_DATA_SECRET_KEY = "";
  private static final String DEFAULT_DATA_THREADS = "8";
  private static final String DEFAULT_DATA_PATH = "/var/tmp/startstopcontinue";
  private static final String DEFAULT_DATA_SIZE = "1024";

  public int getHttpPort() {

    return Integer.valueOf(System.getenv().getOrDefault(HTTP_PORT, DEFAULT_HTTP_PORT));
  }

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

  public String getDataEndpoint() {

    return System.getenv().getOrDefault(DATA_ENDPOINT, DEFAULT_DATA_ENDPOINT);
  }

  public int getDataPort() {

    return Integer.valueOf(System.getenv().getOrDefault(DATA_PORT, DEFAULT_DATA_PORT));
  }

  public String getDataAccessKey() {

    return System.getenv().getOrDefault(DATA_ACCESS_KEY, DEFAULT_DATA_ACCESS_KEY);
  }

  public String getDataSecretKey() {

    return System.getenv().getOrDefault(DATA_SECRET_KEY, DEFAULT_DATA_SECRET_KEY);
  }

  public int getDataThreads() {

    return Integer.valueOf(System.getenv().getOrDefault(DATA_THREADS, DEFAULT_DATA_THREADS));
  }

  public Path getDataPath() {

    return Path.of(System.getenv().getOrDefault(DATA_PATH, DEFAULT_DATA_PATH));
  }

  public int getDataSize() {

    return Integer.valueOf(System.getenv().getOrDefault(DATA_SIZE, DEFAULT_DATA_SIZE));
  }
}
