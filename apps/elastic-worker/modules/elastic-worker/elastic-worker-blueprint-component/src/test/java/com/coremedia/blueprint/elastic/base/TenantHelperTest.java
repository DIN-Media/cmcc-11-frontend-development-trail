package com.coremedia.blueprint.elastic.base;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.settings.impl.BlueprintSettingsServiceConfiguration;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.elastic.core.impl.tenant.TenantConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({
        TenantHelperTest.LocalConfig.class,
        TenantConfiguration.class,
        XmlRepoConfiguration.class
})
class TenantHelperTest {

  @Autowired
  private SettingsService settingsService;

  @Autowired
  private SitesService sitesService;

  @Test
  void testReadTenantsFromContent() {
    final TenantHelper tenantHelper = new TenantHelper(sitesService, settingsService);
    final Collection<String> strings = tenantHelper.readTenantsFromContent();
    assertThat(strings).containsExactlyInAnyOrder("tenant", "testTenant");
  }

  @Configuration(proxyBeanMethods = false)
  @Import(BlueprintSettingsServiceConfiguration.class)
  @ImportResource(value = "classpath:META-INF/coremedia/elastic-worker.xml",
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/testing/contenttest.xml");
    }
  }
}
