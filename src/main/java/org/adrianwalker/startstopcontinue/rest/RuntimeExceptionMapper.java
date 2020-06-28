package org.adrianwalker.startstopcontinue.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public final class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(final RuntimeException re) {

    return Response.status(500).entity(re.getMessage()).build();
  }
}
