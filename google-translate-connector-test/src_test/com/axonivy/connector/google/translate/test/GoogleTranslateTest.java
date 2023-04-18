package com.axonivy.connector.google.translate.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.connector.googletranslate.mock.GoogleTranslateServiceMock;

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
import ch.ivyteam.ivy.scripting.objects.CompositeObject;
import google.translate.Language;

@IvyProcessTest
public class GoogleTranslateTest{

  private static final BpmProcess TRANSLATE = BpmProcess.path("google/translate");

  private interface Start {
    BpmElement GOOGLE_TRANSLATE = TRANSLATE.elementName("getTranslatedText(String,String,String)");
    BpmElement GOOGLE_LANGUAGES = TRANSLATE.elementName("getLanguages(String)");
  }

  @BeforeEach
  void setup(AppFixture fixture, IApplication app) {
    fixture.config("RestClients.googleTranslate.Url", GoogleTranslateServiceMock.URI);
    RestClients clients = RestClients.of(app);
    RestClient googleTranslate = clients.find("googleTranslate");
    var testClient = googleTranslate.toBuilder()
      .feature(CsrfHeaderFeature.class.getName())
      .toRestClient();
    clients.set(testClient);
  }

  @Test
  public void translate1(BpmClient bpmClient){
    ExecutionResult result = bpmClient.start()
      .subProcess(Start.GOOGLE_TRANSLATE)
      .withParam("text", "Hello World")
      .withParam("targetLanguage", "de")
      .withParam("sourceLanguate", "en")
      .execute();
    CompositeObject data = result.data().last();
    try {
		assertThat(data.get("translatedText")).isEqualTo("Hallo Welt");
	} catch (NoSuchFieldException e) {
		assertTrue(false);
	}
  }
  
  @Test
  public void translate2(BpmClient bpmClient){
    ExecutionResult result = bpmClient.start()
      .subProcess(Start.GOOGLE_TRANSLATE)
      .withParam("text", "Hello World")
      .withParam("targetLanguage", "en")
      .withParam("sourceLanguate", "en")
      .execute();
    CompositeObject data = result.data().last();
    try {
		assertThat(data.get("translatedText")).isEqualTo("Hello World");
	} catch (NoSuchFieldException e) {
		assertTrue(false);
	}
  }
  
  @Test
  public void translate3(BpmClient bpmClient){
    ExecutionResult result = bpmClient.start()
      .subProcess(Start.GOOGLE_TRANSLATE)
      .withParam("text", "Hello World")
      .withParam("targetLanguage", "en")
      .withParam("sourceLanguate", "")
      .execute();
    CompositeObject data = result.data().last();
    try {
		assertThat(data.get("translatedText")).isEqualTo("Hallo Welt");
	} catch (NoSuchFieldException e) {
		assertTrue(false);
	}
  }

  @Test
  public void languages1(BpmClient bpmClient){
    ExecutionResult result = bpmClient.start()
      .subProcess(Start.GOOGLE_LANGUAGES)
      .withParam("targetLanguage", "en")
      .execute();
    CompositeObject data = result.data().last();
    try {
    	if (data.get("languages") != null) {
    		var languages = (List<Language>) data.get("languages");
    		assertTrue(languages.size() > 100);
    	} else {
    		assertTrue(false);
    	}
	} catch (NoSuchFieldException e) {
		assertTrue(false);
	}
  }
  
  @Test
  public void languages2(BpmClient bpmClient){
    ExecutionResult result = bpmClient.start()
      .subProcess(Start.GOOGLE_LANGUAGES)
      .withParam("targetLanguage", "")
      .execute();
    CompositeObject data = result.data().last();
    try {
    	if (data.get("languages") != null) {
    		var languages = (List<Language>) data.get("languages");
    		assertTrue(languages.size() > 100);
    	} else {
    		assertTrue(false);
    	}
	} catch (NoSuchFieldException e) {
		assertTrue(false);
	}
  }
}
