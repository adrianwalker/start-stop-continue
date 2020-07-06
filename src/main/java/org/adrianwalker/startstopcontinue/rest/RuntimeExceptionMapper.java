package org.adrianwalker.startstopcontinue.rest;

import java.nio.file.NoSuchFileException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);
  private static final String BOARD_NOT_FOUND = "Board Not Found";

  @Override
  public Response toResponse(final RuntimeException re) {

    LOGGER.error(re.getMessage(), re);

    Throwable cause = getRootCause(re);

    if (cause instanceof NoSuchFileException) {
      return Response.status(404).entity(BOARD_NOT_FOUND).build();
    }

    return Response.status(500).entity(cause.getMessage()).build();
  }

  public Throwable getRootCause(final RuntimeException re) {

    Throwable t = re;

    Throwable cause;
    while ((cause = t.getCause()) != null) {
      t = cause;
    }

    return t;
  }
}
