package org.adrianwalker.startstopcontinue.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class JsonUtil {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
  }

  public static <T extends Object> T fromJson(final String json, final Class<T> type) {

    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static <T extends Object> T fromJson(final File file, final Class<T> type) {

    try {
      return OBJECT_MAPPER.readValue(file, type);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static <T extends Object> T fromJson(final InputStream is, Class<T> type) throws IOException, JsonParseException, JsonMappingException {

    try {
      return OBJECT_MAPPER.readValue(is, type);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static String toJson(final Object obj) {

    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (final JsonProcessingException jpe) {
      throw new RuntimeException(jpe);
    }
  }

  public static void toJson(final File file, Object obj) {

    try {
      OBJECT_MAPPER.writeValue(file, obj);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static void toJson(final OutputStream os, Object obj) {

    try {
      OBJECT_MAPPER.writeValue(os, obj);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
