package org.adrianwalker.startstopcontinue;

import java.nio.file.Path;

public final class Configuration {

  private static final String HTTP_PORT = "startstopcontinue.http.port";
  private static final String CACHE_SIZE = "startstopcontinue.cache.size";
  private static final String CACHE_SYNC = "startstopcontinue.cache.sync";
  private static final String DATA_THREADS = "startstopcontinue.data.threads";
  private static final String DATA_PATH = "startstopcontinue.data.path";
  private static final String DATA_SIZE = "startstopcontinue.data.size";

  private static final String DEFAULT_HTTP_PORT = "8080";
  private static final String DEFAULT_CACHE_SIZE = "32";
  private static final String DEFAULT_CACHE_SYNC = "False";
  private static final String DEFAULT_DATA_THREADS = "8";
  private static final String DEFAULT_DATA_PATH = "/var/tmp/startstopcontinue";
  private static final String DEFAULT_DATA_SIZE = "1024";

  public int getHttpPort() {

    return Integer.valueOf(System.getenv().getOrDefault(HTTP_PORT, DEFAULT_HTTP_PORT));
  }

  public int getCacheSize() {

    return Integer.valueOf(System.getenv().getOrDefault(CACHE_SIZE, DEFAULT_CACHE_SIZE));
  }

  public boolean getCacheSync() {

    return Boolean.valueOf(System.getenv().getOrDefault(CACHE_SYNC, DEFAULT_CACHE_SYNC));
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
