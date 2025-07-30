package com.axonivy.connector.google.translate.test.utils;

import com.axonivy.connector.google.translate.test.constants.GoogleTranslateTestConstants;
import com.axonivy.connector.googletranslate.mock.GoogleTranslateServiceMock;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature;

public class GoogleTranslateTestUtils {

  public static void setUpConfigForContext(String contextName, AppFixture fixture, IApplication app) {
    switch (contextName) {
      case GoogleTranslateTestConstants.REAL_CALL_CONTEXT_DISPLAY_NAME:
        setUpConfigForApiTest(fixture);
        break;
      case GoogleTranslateTestConstants.MOCK_SERVER_CONTEXT_DISPLAY_NAME:
        setUpConfigForMockServer(fixture, app);
        break;
      default:
        break;
    }
  }

  private static void setUpConfigForApiTest(AppFixture fixture) {
    String apiKey = System.getProperty(GoogleTranslateTestConstants.API_KEY);
    fixture.var("googleTranslateConnector.apiKey", apiKey);
  }

  private static void setUpConfigForMockServer(AppFixture fixture, IApplication app) {
    fixture.config("RestClients.googleTranslate.Url", GoogleTranslateServiceMock.URI);
    RestClients clients = RestClients.of(app);
    RestClient googleTranslate = clients.find("googleTranslate");
    var testClient = googleTranslate.toBuilder().feature(CsrfHeaderFeature.class.getName()).toRestClient();
    clients.set(testClient);
  }
}
