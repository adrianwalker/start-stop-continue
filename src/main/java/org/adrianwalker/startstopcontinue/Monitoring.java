package org.adrianwalker.startstopcontinue;

import com.sun.management.UnixOperatingSystemMXBean;
import static java.lang.String.format;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Monitoring {

  private final static Logger LOGGER = LoggerFactory.getLogger(Monitoring.class);
  private static final double BYTES_IN_MEGABYTE = 1 * 1024 * 1024;

  private Monitoring() {
  }

  public static void logMemoryUsage() {

    Runtime runtime = Runtime.getRuntime();
    double usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_IN_MEGABYTE;
    double freeMemory = runtime.freeMemory() / BYTES_IN_MEGABYTE;
    double totalMemory = runtime.totalMemory() / BYTES_IN_MEGABYTE;
    double maxMemory = runtime.maxMemory() / BYTES_IN_MEGABYTE;

    LOGGER.info(
      "usedMemory = {}, freeMemory = {}, totalMemory = {}, maxMemory = {}",
      format("%.2f", usedMemory),
      format("%.2f", freeMemory),
      format("%.2f", totalMemory),
      format("%.2f", maxMemory));
  }

  public static void logOpenFileHandles() {

    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    if (os instanceof UnixOperatingSystemMXBean) {
      UnixOperatingSystemMXBean uos = ((UnixOperatingSystemMXBean) os);
      long openFileDescriptors = uos.getOpenFileDescriptorCount();

      LOGGER.info("openFileDescriptors = {}", openFileDescriptors);
    }
  }
}
