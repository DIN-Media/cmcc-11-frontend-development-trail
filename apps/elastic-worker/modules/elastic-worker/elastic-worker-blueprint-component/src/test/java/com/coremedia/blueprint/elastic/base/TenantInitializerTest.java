package com.coremedia.blueprint.elastic.base;

import com.coremedia.blueprint.base.settings.impl.BlueprintSettingsServiceConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.elastic.core.api.tenant.TenantServiceListener;
import com.coremedia.elastic.core.api.tenant.TenantServiceListenerBase;
import com.coremedia.elastic.core.impl.tenant.TenantConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({
        TenantInitializerTest.LocalConfig.class,
        TenantConfiguration.class,
        TenantInitializerConfiguration.class,
        XmlRepoConfiguration.class
})
class TenantInitializerTest {

  @Autowired
  private MyTenantServiceListenerBase myTenantServiceListenerBase;

  @Test
  void tenantsRegistered() throws InterruptedException {
    final String tenant = "tenant";
    final String testTenant = "testTenant";
    for (int i = 0; i < 10; i++) {
      synchronized (myTenantServiceListenerBase.monitor) {
        if (!(containsTenant(tenant) && containsTenant(testTenant))) {
          myTenantServiceListenerBase.monitor.wait(1000);
        }
      }
    }
    assertThat(containsTenant(tenant)).as(tenant).isTrue();
    assertThat(containsTenant(testTenant)).as(testTenant).isTrue();
  }

  private boolean containsTenant(String tenant) {
    return myTenantServiceListenerBase.tenants.contains(tenant);
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

    @Bean
    public TenantServiceListener tenantServiceListener() {
      return new MyTenantServiceListenerBase();
    }

  }

  private static class MyTenantServiceListenerBase extends TenantServiceListenerBase {

    final Object monitor = new Object();
    Collection<String> tenants = new ArrayList<>();

    @Override
    public void onTenantRegistered(String tenant) {
      synchronized (monitor) {
        tenants.add(tenant);
        monitor.notifyAll();
      }
    }

  }
}
