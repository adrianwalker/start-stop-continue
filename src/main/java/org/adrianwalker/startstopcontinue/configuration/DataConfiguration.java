package org.adrianwalker.startstopcontinue.configuration;

import java.nio.file.Path;

public final class DataConfiguration {

  /*
  Environment variable names
   */
  private static final String DATA_ENDPOINT = "STARTSTOPCONTINUE_DATA_ENDPOINT";
  private static final String DATA_PORT = "STARTSTOPCONTINUE_DATA_PORT";
  private static final String DATA_ACCESS_KEY = "STARTSTOPCONTINUE_DATA_ACCESS_KEY";
  private static final String DATA_SECRET_KEY = "STARTSTOPCONTINUE_DATA_SECRET_KEY";
  private static final String DATA_SECURE = "STARTSTOPCONTINUE_DATA_SECURE";
  private static final String DATA_REGION = "STARTSTOPCONTINUE_DATA_REGION";
  private static final String DATA_BUCKET = "STARTSTOPCONTINUE_DATA_BUCKET";
  private static final String DATA_PATH = "STARTSTOPCONTINUE_DATA_PATH";
  private static final String DATA_THREADS = "STARTSTOPCONTINUE_DATA_THREADS";
  private static final String DATA_SIZE = "STARTSTOPCONTINUE_DATA_SIZE";

  /*
  Configuration defaults
   */
  public static final String DEFAULT_DATA_ENDPOINT = "";
  public static final String DEFAULT_DATA_PORT = "0";
  public static final String DEFAULT_DATA_ACCESS_KEY = "";
  public static final String DEFAULT_DATA_SECRET_KEY = "";
  public static final String DEFAULT_DATA_SECURE = "false";
  public static final String DEFAULT_DATA_REGION = "";
  public static final String DEFAULT_DATA_BUCKET = "";
  public static final String DEFAULT_DATA_PATH = "/var/tmp/startstopcontinue";
  public static final String DEFAULT_DATA_THREADS = "8";
  public static final String DEFAULT_DATA_SIZE = "1024";

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

  public boolean getDataSecure() {

    return Boolean.valueOf(System.getenv().getOrDefault(DATA_SECURE, DEFAULT_DATA_SECURE));
  }

  public String getDataRegion() {

    return System.getenv().getOrDefault(DATA_REGION, DEFAULT_DATA_REGION);
  }

  public String getDataBucket() {

    return System.getenv().getOrDefault(DATA_BUCKET, DEFAULT_DATA_BUCKET);
  }

  public Path getDataPath() {

    return Path.of(System.getenv().getOrDefault(DATA_PATH, DEFAULT_DATA_PATH));
  }

  public int getDataThreads() {

    return Integer.valueOf(System.getenv().getOrDefault(DATA_THREADS, DEFAULT_DATA_THREADS));
  }

  public int getDataSize() {

    return Integer.valueOf(System.getenv().getOrDefault(DATA_SIZE, DEFAULT_DATA_SIZE));
  }
}
