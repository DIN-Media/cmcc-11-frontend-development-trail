package com.coremedia.blueprint.studio.topicpages.rest;

import com.coremedia.blueprint.base.config.ConfigurationService;
import com.coremedia.blueprint.base.rest.BlueprintBaseStudioRestConfiguration;
import com.coremedia.blueprint.base.taxonomies.TaxonomyLocalizationStrategy;
import com.coremedia.blueprint.localization.configuration.TaxonomyLocalizationStrategyConfiguration;
import com.coremedia.blueprint.taxonomies.TaxonomyConfiguration;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.spring.ContentConfigurationProperties;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/common/uapi-services.xml",
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@Import({
        BlueprintBaseStudioRestConfiguration.class,
        TaxonomyLocalizationStrategyConfiguration.class,
        TaxonomyConfiguration.class,
})
@EnableConfigurationProperties(ContentConfigurationProperties.class)
public class CustomTopicPagesConfiguration {

  @Bean
  TopicPagesResource topicPagesResource(CapConnection connection,
                                        ConfigurationService configurationService,
                                        SitesService sitesService,
                                        TaxonomyResolver strategyResolver,
                                        TaxonomyLocalizationStrategy taxonomyLocalizationStrategy,
                                        ContentConfigurationProperties contentConfigurationProperties) {
    TopicPagesResource topicPagesResource = new TopicPagesResource(connection,
            configurationService,
            strategyResolver,
            taxonomyLocalizationStrategy,
            sitesService,
            contentConfigurationProperties.getSiteConfigurationPath(),
            contentConfigurationProperties.getGlobalConfigurationPath());
    topicPagesResource.setIgnoredTaxonomies("Asset Download Portal");

    return topicPagesResource;
  }
}
