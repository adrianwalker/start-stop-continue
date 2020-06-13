package org.adrianwalker.startstopcontinue.rest;

import org.adrianwalker.startstopcontinue.service.Service;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public final class RestServlet extends ResourceConfig {

  public RestServlet(final Service service) {

    register(JacksonFeature.class);
    registerInstances(new RestService(service));
  }
}
