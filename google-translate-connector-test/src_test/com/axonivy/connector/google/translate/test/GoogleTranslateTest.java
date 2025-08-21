package com.axonivy.connector.google.translate.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.axonivy.utils.e2etest.enums.E2EEnvironment.REAL_SERVER;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.axonivy.connector.google.translate.test.constants.GoogleTranslateTestConstants;
import com.axonivy.connector.googletranslate.mock.GoogleTranslateServiceMock;
import com.axonivy.utils.e2etest.context.MultiEnvironmentContextProvider;
import com.axonivy.utils.e2etest.utils.E2ETestUtils;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.engine.client.ExecutionResult;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmElement;
import ch.ivyteam.ivy.bpm.engine.client.element.BpmProcess;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.rest.client.RestClient;
import ch.ivyteam.ivy.rest.client.RestClients;
import ch.ivyteam.ivy.rest.client.security.CsrfHeaderFeature;
import google.translate.Data;

@IvyProcessTest(enableWebServer = true)
@ExtendWith(MultiEnvironmentContextProvider.class)
public class GoogleTranslateTest {

  private static final BpmProcess TRANSLATE = BpmProcess.path("google/translate");

  private interface Start {
    BpmElement GOOGLE_TRANSLATE = TRANSLATE.elementName("getTranslatedText(String,String,String)");
    BpmElement GOOGLE_LANGUAGES = TRANSLATE.elementName("getLanguages(String)");
  }

  @BeforeEach
  void beforeEach(ExtensionContext context, AppFixture fixture, IApplication app) {
    E2ETestUtils.determineConfigForContext(context.getDisplayName(), runRealEnv(fixture), runMockEnv(fixture, app));
  }

  private Runnable runRealEnv(AppFixture fixture) {
    return () -> {
      String apiKey = System.getProperty(GoogleTranslateTestConstants.API_KEY);
      fixture.var("googleTranslateConnector.apiKey", apiKey);
    };
  }

  private Runnable runMockEnv(AppFixture fixture, IApplication app) {
    return () -> {
      fixture.config("RestClients.googleTranslate.Url", GoogleTranslateServiceMock.URI);
      RestClients clients = RestClients.of(app);
      RestClient googleTranslate = clients.find("googleTranslate");
      var testClient = googleTranslate.toBuilder().feature(CsrfHeaderFeature.class.getName()).toRestClient();
      clients.set(testClient);
    };
  }

  @AfterEach
  void afterEach(AppFixture fixture, IApplication app) {
    RestClients clients = RestClients.of(app);
    clients.remove("googleTranslate");
  }

  /**
   * Test case: translate "Hello World" from English to German Expect: the translated is "Hallo Welt"
   */
  @TestTemplate
  public void translate_deToEn(ExtensionContext context, BpmClient bpmClient) {
    ExecutionResult result = bpmClient.start().subProcess(Start.GOOGLE_TRANSLATE).withParam("text", "Hello World")
        .withParam("targetLanguage", "de").withParam("sourceLanguate", "en").execute();
    Data data = result.data().last();
    assertThat(data.getTranslatedText()).isEqualTo("Hallo Welt");
  }

  /**
   * Test case: translate "Hello World" from English to English Expect: the translated is "Hello World"
   */
  @TestTemplate
  public void translate_enToEn(ExtensionContext context, BpmClient bpmClient) {
    ExecutionResult result = bpmClient.start().subProcess(Start.GOOGLE_TRANSLATE).withParam("text", "Hello World")
        .withParam("targetLanguage", "en").withParam("sourceLanguate", "en").execute();
    Data data = result.data().last();
    assertThat(data.getTranslatedText()).isEqualTo("Hello World");
  }

  /**
   * Test case: translate a text from a blank to English Expect: the translated is "Hallo Welt"(default as German)
   */
  @TestTemplate
  public void translate_blankToEn(ExtensionContext context, BpmClient bpmClient) {
    ExecutionResult result = bpmClient.start().subProcess(Start.GOOGLE_TRANSLATE).withParam("text", "Hello World")
        .withParam("targetLanguage", "en").withParam("sourceLanguate", "").execute();
    Data data = result.data().last();
    if (context.getDisplayName().equals(REAL_SERVER.getDisplayName())) {
      assertTrue(!data.getTranslatedText().isEmpty());
    } else {
      assertThat(data.getTranslatedText()).isEqualTo("Hallo Welt");
    }
  }

  /**
   * Test case: get a list languages name by English Expect: the list has more 100 languages by English
   */
  @TestTemplate
  public void languages_listOfEn(ExtensionContext context, BpmClient bpmClient) {
    ExecutionResult result =
        bpmClient.start().subProcess(Start.GOOGLE_LANGUAGES).withParam("targetLanguage", "en").execute();
    Data data = result.data().last();
    var languages = data.getLanguages();
    assertTrue(languages.size() > 100);
  }

  /**
   * Test case: get a list languages name by a blank value Expect: the list has more 100 languages by default as English
   */
  @TestTemplate
  public void languages2_listOfBlank(ExtensionContext context, BpmClient bpmClient) {
    ExecutionResult result =
        bpmClient.start().subProcess(Start.GOOGLE_LANGUAGES).withParam("targetLanguage", "").execute();
    Data data = result.data().last();
    var languages = data.getLanguages();
    assertTrue(languages.size() > 100);
  }
}
