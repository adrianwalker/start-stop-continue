package org.adrianwalker.startstopcontinue.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  private final static Logger LOGGER = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

  @Override
  public Response toResponse(final RuntimeException re) {

    LOGGER.error(re.getMessage(), re);

    return Response.status(500).entity(re.getMessage()).build();
  }
}
