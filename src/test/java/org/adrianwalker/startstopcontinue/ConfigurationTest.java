package org.adrianwalker.startstopcontinue;

import java.nio.file.Path;
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
  public void testGetHttpPort() {

    assertEquals(8080, configuration.getHttpPort());
  }

  @Test
  public void testGetCacheHostname() {

    assertEquals("", configuration.getCacheHostname());
  }

  @Test
  public void testGetCachePort() {

    assertEquals(0, configuration.getCachePort());
  }

  @Test
  public void testGetCacheSize() {

    assertEquals(32, configuration.getCacheSize());
  }

  @Test
  public void testGetDataThreads() {

    assertEquals(8, configuration.getDataThreads());
  }

  @Test
  public void testGetDataPath() {

    assertEquals(Path.of("/var/tmp/startstopcontinue"), configuration.getDataPath());
  }

  @Test
  public void testGetDataSize() {

    assertEquals(1024, configuration.getDataSize());
  }
}
