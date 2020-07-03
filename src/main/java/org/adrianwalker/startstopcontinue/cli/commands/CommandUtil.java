package org.adrianwalker.startstopcontinue.cli.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public final class CommandUtil {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private CommandUtil() {
  }

  public static String toJson(final Map<String, Object> data) {

    try {
      return OBJECT_MAPPER.writeValueAsString(data);
    } catch (final JsonProcessingException jpe) {
      throw new RuntimeException(jpe);
    }
  }
}
