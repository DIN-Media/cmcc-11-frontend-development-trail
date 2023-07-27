package com.coremedia.blueprint.cae.configuration;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.base.tree.TreeRelationServicesConfiguration;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.cache.Cache;
import com.coremedia.cache.config.CacheConfiguration;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cms.delivery.configuration.DeliveryConfigurationProperties;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration(proxyBeanMethods = false)
@Import({
        CacheConfiguration.class,
        TreeRelationServicesConfiguration.class,
})
@ImportResource(value = {
        "classpath:/com/coremedia/cap/multisite/multisite-services.xml",
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@EnableConfigurationProperties({
        BlueprintPageCaeContentBeansConfigurationProperties.class
})
public class BlueprintPageCaeContentBeansConfiguration {

  private final DeliveryConfigurationProperties deliveryProperties;
  private final BlueprintPageCaeContentBeansConfigurationProperties bpCaeProperties;

  private final TreeRelation<Content> navigationTreeRelation;
  private final ContentBeanFactory contentBeanFactory;
  private final DataViewFactory dataViewFactory;
  private final SitesService sitesService;
  private final Cache cache;

  BlueprintPageCaeContentBeansConfiguration(DeliveryConfigurationProperties deliveryProperties,
                                            BlueprintPageCaeContentBeansConfigurationProperties bpCaeProperties,
                                            TreeRelation<Content> navigationTreeRelation,
                                            ContentBeanFactory contentBeanFactory,
                                            DataViewFactory dataViewFactory,
                                            SitesService sitesService,
                                            Cache cache) {
    this.deliveryProperties = deliveryProperties;
    this.bpCaeProperties = bpCaeProperties;
    this.navigationTreeRelation = navigationTreeRelation;
    this.contentBeanFactory = contentBeanFactory;
    this.dataViewFactory = dataViewFactory;
    this.sitesService = sitesService;
    this.cache = cache;
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  //Create the cmPage prototype bean. Do not define any arguments here to avoid repeated spring dependency lookup.
  public PageImpl cmPage() {
    PageImpl page = new PageImpl(
            deliveryProperties.isDeveloperMode(),
            sitesService,
            cache,
            navigationTreeRelation,
            contentBeanFactory,
            dataViewFactory);
    page.setMergeCodeResources(bpCaeProperties.isMergeCodeResources());
    return page;
  }
}
