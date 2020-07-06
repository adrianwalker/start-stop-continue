package org.adrianwalker.startstopcontinue;

import com.sun.management.UnixOperatingSystemMXBean;
import static java.lang.String.format;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;
import static java.util.Map.of;
import static java.util.stream.Collectors.joining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Monitoring {

  private final static Logger LOGGER = LoggerFactory.getLogger(Monitoring.class);
  private static final double BYTES_IN_MEGABYTE = 1 * 1024 * 1024;

  private Monitoring() {
  }

  public static Map<String, Double> memoryUsage() {

    Runtime runtime = Runtime.getRuntime();
    double usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_IN_MEGABYTE;
    double freeMemory = runtime.freeMemory() / BYTES_IN_MEGABYTE;
    double totalMemory = runtime.totalMemory() / BYTES_IN_MEGABYTE;
    double maxMemory = runtime.maxMemory() / BYTES_IN_MEGABYTE;

    return of(
      "usedMemory", usedMemory,
      "freeMemory", freeMemory,
      "totalMemory", totalMemory,
      "maxMemory", maxMemory);
  }

  public static Map<String, Long> fileDescriptors() {

    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

    long openFileDescriptors = -1;
    long maxFileDescriptors = -1;

    if (os instanceof UnixOperatingSystemMXBean) {
      UnixOperatingSystemMXBean uos = ((UnixOperatingSystemMXBean) os);
      openFileDescriptors = uos.getOpenFileDescriptorCount();
      maxFileDescriptors = uos.getMaxFileDescriptorCount();
    }

    return of(
      "openFileDescriptors", openFileDescriptors,
      "maxFileDescriptors", maxFileDescriptors);
  }

  public static void logMemoryUsage() {

    LOGGER.info(memoryUsage().entrySet().stream()
      .map(e -> format("%s = %.2f", e.getKey(), e.getValue()))
      .collect(joining(", ")));
  }

  public static void logFileDescriptors() {

    LOGGER.info(fileDescriptors().entrySet().stream()
      .map(e -> format("%s = %s", e.getKey(), e.getValue()))
      .collect(joining(", ")));
  }
}
