package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.livecontext.augmentation.tree.ExternalChannelContentTreeRelation;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.settings.SettingsFinder;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.customizer.Customize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

@ImportResource(reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class, value = {
        "classpath:/com/coremedia/cae/uapi-services.xml",
        "classpath:/com/coremedia/cap/multisite/multisite-services.xml",
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/framework/spring/blueprint-services.xml",
        "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml"})
@Configuration(proxyBeanMethods = false)
@Import({
        com.coremedia.blueprint.base.livecontext.augmentation.config.AugmentationTreeServicesConfiguration.class,
})
public class LiveContextNavigationConfiguration {
  @Bean
  LiveContextNavigationFactory liveContextNavigationFactory(LiveContextNavigationTreeRelation liveContextNavigationTreeRelation,
                                                            ContentBeanFactory contentBeanFactory,
                                                            SitesService sitesService,
                                                            AugmentationService categoryAugmentationService,
                                                            ValidationService<LiveContextNavigation> validationService,
                                                            CommerceConnectionSupplier commerceConnectionSupplier) {
    LiveContextNavigationFactory liveContextNavigationFactory = new LiveContextNavigationFactory();
    liveContextNavigationFactory.setContentBeanFactory(contentBeanFactory);
    liveContextNavigationFactory.setSitesService(sitesService);
    liveContextNavigationFactory.setTreeRelation(liveContextNavigationTreeRelation);
    liveContextNavigationFactory.setAugmentationService(categoryAugmentationService);
    liveContextNavigationFactory.setValidationService(validationService);
    liveContextNavigationFactory.setCommerceConnectionSupplier(commerceConnectionSupplier);
    return liveContextNavigationFactory;
  }

  @Bean
  LiveContextNavigationTreeRelation liveContextNavigationTreeRelation(@Lazy LiveContextNavigationFactory navigationFactory,
                                                                      ContentBeanFactory contentBeanFactory,
                                                                      SitesService sitesService,
                                                                      ExternalChannelContentTreeRelation externalChannelContentTreeRelation) {
    LiveContextNavigationTreeRelation liveContextNavigationTreeRelation = new LiveContextNavigationTreeRelation();
    liveContextNavigationTreeRelation.setNavigationFactory(navigationFactory);
    liveContextNavigationTreeRelation.setContentBeanFactory(contentBeanFactory);
    liveContextNavigationTreeRelation.setSitesService(sitesService);
    liveContextNavigationTreeRelation.setDelegate(externalChannelContentTreeRelation);
    return liveContextNavigationTreeRelation;
  }

  @Customize("contentSettingsFinderHierarchies")
  @Bean(autowireCandidate = false)
  Map<String, TreeRelation<?>> liveContextNavigationSettingsFinderCustomizer(TreeRelation<Content> externalChannelContentTreeRelation) {
    return Map.of("CMExternalChannel", externalChannelContentTreeRelation);
  }

  @Bean
  LiveContextCategoryNavigationSettingsFinder liveContextCategoryNavigationSettingsFinder() {
    return new LiveContextCategoryNavigationSettingsFinder();
  }

  @Customize("settingsFinders")
  @Bean(autowireCandidate = false)
  Map<Class<?>, SettingsFinder> liveContextCategorySettingsServiceConfigurer(LiveContextCategoryNavigationSettingsFinder liveContextCategoryNavigationSettingsFinder) {
    return Map.of(LiveContextCategoryNavigation.class, liveContextCategoryNavigationSettingsFinder);
  }
}
