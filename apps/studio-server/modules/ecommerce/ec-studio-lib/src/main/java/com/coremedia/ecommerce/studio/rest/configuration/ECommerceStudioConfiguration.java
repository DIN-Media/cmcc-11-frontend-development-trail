package com.coremedia.ecommerce.studio.rest.configuration;

import com.coremedia.blueprint.base.livecontext.augmentation.config.AugmentationPageGridServiceConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.ecommerce.studio.preview.CommerceAppPreviewProvider;
import com.coremedia.ecommerce.studio.preview.CommerceHeadlessPreviewProvider;
import com.coremedia.ecommerce.studio.rest.CommerceBeanDefaultPictureResolver;
import com.coremedia.ecommerce.studio.rest.CommerceContentTypesPictureResolver;
import com.coremedia.ecommerce.studio.rest.filter.EcStudioFilters;
import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.service.previewurl.PreviewProvider;
import com.coremedia.service.previewurl.impl.PreviewUrlServiceConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AugmentationPageGridServiceConfiguration.class,
        EcStudioFilters.class,
        CapRestServiceConfiguration.class,
        BaseCommerceServicesConfiguration.class,
})
@EnableConfigurationProperties({
        ECommerceStudioConfigurationProperties.class,
})
@ComponentScan(basePackages = "com.coremedia.ecommerce.studio.rest")
public class ECommerceStudioConfiguration {

  @Bean
  public PreviewProvider commerceHeadlessPreviewProvider(PreviewUrlServiceConfigurationProperties previewUrlServiceConfigurationProperties) {
    return new CommerceHeadlessPreviewProvider(previewUrlServiceConfigurationProperties);
  }

  @Bean
  public PreviewProvider commerceAppPreviewProvider(PreviewUrlServiceConfigurationProperties previewUrlServiceConfigurationProperties,
                                                    SitesService sitesService) {
    return new CommerceAppPreviewProvider(sitesService);
  }

  @Bean
  public CommerceBeanDefaultPictureResolver commerceBeanDefaultPictureResolver(ContentRepository contentRepository) {
    return new CommerceBeanDefaultPictureResolver(contentRepository);
  }

  @Bean
  public CommerceContentTypesPictureResolver commerceContentTypeCategoryDefaultPictureResolver(CommerceConnectionSupplier commerceConnectionSupplier,
                                                                                               ContentRepository contentRepository) {
    return new CommerceContentTypesPictureResolver(commerceConnectionSupplier, contentRepository, CommerceContentTypesPictureResolver.CM_EXTERNAL_CHANNEL);
  }

  @Bean
  public CommerceContentTypesPictureResolver commerceContentTypeProductDefaultPictureResolver(CommerceConnectionSupplier commerceConnectionSupplier,
                                                                                              ContentRepository contentRepository) {
    return new CommerceContentTypesPictureResolver(commerceConnectionSupplier, contentRepository, CommerceContentTypesPictureResolver.CM_EXTERNAL_PRODUCT);
  }

  @Bean
  public CommerceContentTypesPictureResolver commerceContentTypeProductTeaserDefaultPictureResolver(CommerceConnectionSupplier commerceConnectionSupplier,
                                                                                                    ContentRepository contentRepository) {
    return new CommerceContentTypesPictureResolver(commerceConnectionSupplier, contentRepository, CommerceContentTypesPictureResolver.CM_PRODUCT_TEASER);
  }
}
