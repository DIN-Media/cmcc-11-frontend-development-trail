package com.coremedia.blueprint.assets.server;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cms.server.plugins.PublishRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"SpringJavaAutowiringInspection", "DuplicateStringLiteralInspection"})
@SpringJUnitConfig(AssetPublishInterceptorTest.LocalConfig.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles(AssetPublishInterceptorTest.LocalConfig.PROFILE)
class AssetPublishInterceptorTest {

  @Inject
  private ContentRepository repository;

  @Mock
  private PublishRequest publishRequest;

  @Inject
  private AssetPublishInterceptor testling;

  private void setUp(String contentPath) {
    Content testContent = repository.getChild(contentPath);
    Map<String, Object> properties = new HashMap<>(testContent.getProperties());
    Mockito.when(publishRequest.getVersion()).thenReturn(testContent.getCheckedInVersion());
    Mockito.when(publishRequest.getProperties()).thenReturn(properties);
  }

  @Test
  void testMarkedPublishable() {
    setUp("AssetAllTrue");

    testling.intercept(publishRequest);
    assertPublished("web");
  }

  @Test
  void testMarkedNotPublishable() {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  void testMarkedNotPublishableThumbnailOverride() {
    setUp("AssetAllFalse");

    testling.intercept(publishRequest);
    assertPublished("thumbnail");
  }

  @Test
  void testNoSuchRendition() {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("original");
  }

  @Test
  @DirtiesContext
  void testNoSuchRenditionDefaultFalse() {
    setUp("Asset");
    testling.setRemoveDefault(false);

    testling.intercept(publishRequest);
    assertPublished("original");
  }

  @Test
  void testNotMarked() {
    setUp("Asset");

    testling.intercept(publishRequest);
    assertNotPublished("print");
  }

  @Test
  @DirtiesContext
  void testNotMarkedDefaultFalse() {
    setUp("Asset");
    testling.setRemoveDefault(false);

    testling.intercept(publishRequest);
    assertPublished("print");
  }

  @Test
  void testNoRenditions() {
    setUp("AssetWithoutRenditions");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  void testNoRenditionsThumbnailOverride() {
    setUp("AssetWithoutRenditions");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertThat(properties.get("thumbnail")).isNotNull();
  }

  @Test
  void testNoMetadata() {
    setUp("AssetWithoutMetadata");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  void testNoMetadataThumbnailOverride() {
    setUp("AssetWithoutMetadata");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertThat(properties.get("thumbnail")).isNotNull();
  }

  @Test
  void testBadMetadata() {
    setUp("AssetWithBadRenditions");

    testling.intercept(publishRequest);
    assertNotPublished("web");
  }

  @Test
  void testBadMetadataThumbnailOverride() {
    setUp("AssetWithBadRenditions");

    testling.intercept(publishRequest);
    Map<String, Object> properties = publishRequest.getProperties();
    assertThat(properties.get("thumbnail")).isNotNull();
  }

  private void assertPublished(String rendition) {
    Map<String, Object> properties = publishRequest.getProperties();
    assertThat(properties.get(rendition)).isEqualTo(publishRequest.getVersion().get(rendition));
  }

  private void assertNotPublished(String rendition) {
    Map<String, Object> properties = publishRequest.getProperties();
    assertThat(properties.get(rendition)).isNull();
  }

  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  @EnableAutoConfiguration
  @Profile(LocalConfig.PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "AssetPublishInterceptorTest";

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/assets/server/AssetPublishInterceptorTest-content.xml");
    }
  }
}
