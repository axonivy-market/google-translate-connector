package com.axonivy.connector.googletranslate.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.rest.client.config.IvyDefaultJaxRsTemplates;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * A simple REST backend for a third-party service used in tests:<br/>
 * - provides static results and therefore simplifies our tests <br/>
 * - does not require authentication or real-world secrets <br/>
 * - built with ivy standard tools: easy to maintain for everyone <br/>
 */
@Path(GoogleTranslateServiceMock.PATH_SUFFIX)
@PermitAll // allow unauthenticated calls
@Hidden // do not show me on swagger-ui or openapi3 resources.
@SuppressWarnings("all")
public class GoogleTranslateServiceMock {

  static final String PATH_SUFFIX = "googleTranslateMock";

  // URI where this mock can be reached: to be referenced in tests that use it!
  public static final String URI = "{"+IvyDefaultJaxRsTemplates.APP_URL+"}/api/"+PATH_SUFFIX;

  private static int checked = 0;

  /**
   * The mock data of translating result API
   * @return
   */
  @GET
  @Path("/language/translate/v2")
  @Produces(MediaType.APPLICATION_JSON)
  public Response translate() {
    return Response.ok()
      .entity(load("translate.json"))
      .build();
  }
  
  /**
   * 
   * @return
   */
  @GET
  @Path("/language/translate/v2/languages")
  @Produces(MediaType.APPLICATION_JSON)
  public Response languages() {
    return Response.ok()
      .entity(load("languages.json"))
      .build();
  }

  /**
   * Load JSON file from json folder
   * @param json
   * @return
   */
  private static String load(String json) {
    try (var is = GoogleTranslateServiceMock.class.getResourceAsStream("json/"+json)) {
      if (is == null) {
        throw new RuntimeException("The json file '"+json+"' does not exist.");
      }
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read json "+json, ex);
    }
  }
}
