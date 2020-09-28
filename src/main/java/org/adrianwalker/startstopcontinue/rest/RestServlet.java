package org.adrianwalker.startstopcontinue.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.ext.ContextResolver;
import org.adrianwalker.startstopcontinue.service.Service;
import static org.adrianwalker.startstopcontinue.util.JsonUtil.OBJECT_MAPPER;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public final class RestServlet extends ResourceConfig {

  public RestServlet(final Service service) {

    register(new JacksonObjectMapperProvider());
    register(JacksonFeature.class);
    register(RuntimeExceptionMapper.class);
    registerInstances(new RestService(service));
  }

  private static final class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(final Class<?> type) {
      return OBJECT_MAPPER;
    }
  }
}
