package org.adrianwalker.startstopcontinue.rest;

import java.util.UUID;
import org.adrianwalker.startstopcontinue.cache.Cache;
import org.adrianwalker.startstopcontinue.model.Board;
import org.adrianwalker.startstopcontinue.service.Service;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public final class RestServlet extends ResourceConfig {

  public RestServlet(final Service service, final Cache<UUID, Board> cache) {

    register(JacksonFeature.class);
    registerInstances(
      new RestService(service, cache));
    
  }
}
