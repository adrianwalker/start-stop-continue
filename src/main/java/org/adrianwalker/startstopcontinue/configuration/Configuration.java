package org.adrianwalker.startstopcontinue.configuration;

public final class Configuration {
  
  private final HttpConfiguration httpConfiguration;
  private final CacheConfiguration cacheConfiguration;
  private final DataConfiguration dataConfiguration;
  private final PubSubConfiguration pubSubConfiguration;
  
  public Configuration() {
    
    httpConfiguration = new HttpConfiguration();
    cacheConfiguration = new CacheConfiguration();
    dataConfiguration = new DataConfiguration();
    pubSubConfiguration = new PubSubConfiguration();
  }

  public HttpConfiguration getHttpConfiguration() {
    return httpConfiguration;
  }

  public CacheConfiguration getCacheConfiguration() {
    return cacheConfiguration;
  }

  public DataConfiguration getDataConfiguration() {
    return dataConfiguration;
  }

  public PubSubConfiguration getPubSubConfiguration() {
    return pubSubConfiguration;
  }
}
