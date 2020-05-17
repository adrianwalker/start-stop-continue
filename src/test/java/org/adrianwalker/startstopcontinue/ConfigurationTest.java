package org.adrianwalker.startstopcontinue;

import org.adrianwalker.startstopcontinue.configuration.Configuration;
import static org.adrianwalker.startstopcontinue.configuration.CacheConfiguration.DEFAULT_CACHE_HOSTNAME;
import static org.adrianwalker.startstopcontinue.configuration.CacheConfiguration.DEFAULT_CACHE_PASSWORD;
import static org.adrianwalker.startstopcontinue.configuration.CacheConfiguration.DEFAULT_CACHE_PORT;
import static org.adrianwalker.startstopcontinue.configuration.CacheConfiguration.DEFAULT_CACHE_SIZE;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_ACCESS_KEY;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_BUCKET;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_ENDPOINT;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_PATH;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_PORT;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_REGION;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_SECRET_KEY;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_SECURE;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_SIZE;
import static org.adrianwalker.startstopcontinue.configuration.DataConfiguration.DEFAULT_DATA_THREADS;
import static org.adrianwalker.startstopcontinue.configuration.HttpConfiguration.DEFAULT_HTTP_PORT;
import static org.adrianwalker.startstopcontinue.configuration.PubSubConfiguration.DEFAULT_PUBSUB_HOSTNAME;
import static org.adrianwalker.startstopcontinue.configuration.PubSubConfiguration.DEFAULT_PUBSUB_PASSWORD;
import static org.adrianwalker.startstopcontinue.configuration.PubSubConfiguration.DEFAULT_PUBSUB_PORT;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public final class ConfigurationTest {

  private Configuration configuration;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
  }

  @Test
  public void testDefaults() {

    assertEqualsStringValueOf(DEFAULT_HTTP_PORT, configuration.getHttpConfiguration().getHttpPort());

    assertEqualsStringValueOf(DEFAULT_CACHE_HOSTNAME, configuration.getCacheConfiguration().getCacheHostname());
    assertEqualsStringValueOf(DEFAULT_CACHE_PORT, configuration.getCacheConfiguration().getCachePort());
    assertEqualsStringValueOf(DEFAULT_CACHE_PASSWORD, configuration.getCacheConfiguration().getCachePassword());
    assertEqualsStringValueOf(DEFAULT_CACHE_SIZE, configuration.getCacheConfiguration().getCacheSize());

    assertEqualsStringValueOf(DEFAULT_DATA_ENDPOINT, configuration.getDataConfiguration().getDataEndpoint());
    assertEqualsStringValueOf(DEFAULT_DATA_PORT, configuration.getDataConfiguration().getDataPort());
    assertEqualsStringValueOf(DEFAULT_DATA_ACCESS_KEY, configuration.getDataConfiguration().getDataAccessKey());
    assertEqualsStringValueOf(DEFAULT_DATA_SECRET_KEY, configuration.getDataConfiguration().getDataSecretKey());
    assertEqualsStringValueOf(DEFAULT_DATA_SECURE, configuration.getDataConfiguration().getDataSecure());
    assertEqualsStringValueOf(DEFAULT_DATA_REGION, configuration.getDataConfiguration().getDataRegion());
    assertEqualsStringValueOf(DEFAULT_DATA_BUCKET, configuration.getDataConfiguration().getDataBucket());
    assertEqualsStringValueOf(DEFAULT_DATA_PATH, configuration.getDataConfiguration().getDataPath());
    assertEqualsStringValueOf(DEFAULT_DATA_THREADS, configuration.getDataConfiguration().getDataThreads());
    assertEqualsStringValueOf(DEFAULT_DATA_SIZE, configuration.getDataConfiguration().getDataSize());

    assertEqualsStringValueOf(DEFAULT_PUBSUB_HOSTNAME, configuration.getPubSubConfiguration().getPubSubHostname());
    assertEqualsStringValueOf(DEFAULT_PUBSUB_PORT, configuration.getPubSubConfiguration().getPubSubPort());
    assertEqualsStringValueOf(DEFAULT_PUBSUB_PASSWORD, configuration.getPubSubConfiguration().getPubSubPassword());
  }

  private void assertEqualsStringValueOf(final String expected, final Object actual) {

    assertEquals(expected, String.valueOf(actual));
  }
}
