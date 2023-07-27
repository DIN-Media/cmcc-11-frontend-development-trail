package com.coremedia.blueprint.lc.test.services;

import com.coremedia.blueprint.base.livecontext.client.common.GenericCommerceConnection;
import com.coremedia.blueprint.base.livecontext.client.common.GenericCommerceConnectionFactory;
import com.coremedia.blueprint.base.livecontext.client.config.CommerceAdapterClientAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.client.data.DataClient;
import com.coremedia.blueprint.base.livecontext.client.data.DataClientFactory;
import com.coremedia.blueprint.base.livecontext.client.settings.CommerceSettings;
import com.coremedia.blueprint.base.livecontext.client.settings.CommerceSettingsProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
}, classes = {
        PropertyPlaceholderAutoConfiguration.class,
        OverrideGenericCommerceConnectionTest.LocalConfig.class,
})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OverrideGenericCommerceConnectionTest {

  @Configuration(proxyBeanMethods = false)
  @Import({
          BaseCommerceServicesAutoConfiguration.class,
          CommerceAdapterClientAutoConfiguration.class
  })
  static class LocalConfig {

    @Bean
    @Scope("prototype")
    TestOverrideGenericCommerceConnection myOverrideConnection() {
      return spy(TestOverrideGenericCommerceConnection.class);
    }

    @Bean
    @Scope("prototype")
    TestOverrideGenericCommerceConnection brokenConnection() {
      TestOverrideGenericCommerceConnection spy = spy(TestOverrideGenericCommerceConnection.class);
      // return a custom store context provider here
      doReturn(mock(StoreContextProvider.class)).when(spy).getStoreContextProvider();
      return spy;
    }
  }

  @SpyBean
  private SettingsService settingsService;

  @MockBean
  private DataClientFactory dataClientFactory;

  @MockBean
  private CatalogAliasTranslationService catalogAliasTranslationService;

  @Autowired
  private GenericCommerceConnectionFactory connectionFactory;

  @Mock
  private CommerceSettingsProvider commerceSettingsProvider;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CommerceSettings commerceSettings;

  @Mock
  private Site site;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataClient dataClient;

  private GenericCommerceConnection connection;

  @BeforeEach
  void setup() {
    when(settingsService.createProxy(CommerceSettingsProvider.class, site)).thenReturn(commerceSettingsProvider);
    when(commerceSettingsProvider.getCommerce()).thenReturn(commerceSettings);

    when(commerceSettings.getEndpoint()).thenReturn("testEndpoint");
    when(dataClientFactory.createDataClient("testEndpoint")).thenReturn(Optional.of(dataClient));

    when(dataClient.getMetadata().getVendor()).thenReturn(Vendor.of("test"));
  }

  @Test()
  void broken() {
    when(commerceSettings.getConnectionQualifier()).thenReturn("brokenConnection");
    assertThatThrownBy(() -> connectionFactory.createConnection(site)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testOverride() {
    when(site.getId()).thenReturn("siteId");
    when(site.getLocale()).thenReturn(Locale.CANADA_FRENCH);
    when(commerceSettings.getCatalogConfig().getId()).thenReturn("testCatalogId");
    when(commerceSettings.getConnectionQualifier()).thenReturn("myOverrideConnection");

    connection = connectionFactory.createConnection(site).orElseThrow(IllegalStateException::new);
    assertThat(connection).isInstanceOf(TestOverrideGenericCommerceConnection.class);
    assertThat(connection.getInitialStoreContext())
            .isNotNull()
            // the store context must reference the custom connection instance
            .returns(connection, StoreContext::getConnection);

    // connection instances are not cached by the factory
    var secondConnection = connectionFactory.createConnection(site).orElseThrow(IllegalStateException::new);
    assertThat(connection).isNotSameAs(secondConnection);
  }

}
