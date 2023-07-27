package com.coremedia.lc.studio.lib;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.lc.studio.lib.interceptor.ChangeCategoryWriteInterceptor;
import com.coremedia.lc.studio.lib.interceptor.ChangeProductWriteInterceptor;
import com.coremedia.lc.studio.lib.validators.LcStudioValidatorsConfiguration;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ComponentScan
@Import({LcStudioValidatorsConfiguration.class, LcStudioPlacementsConfiguration.class})
public class LcStudioLibComponentAutoConfiguration {

  @Bean
  ChangeCategoryWriteInterceptor changeCategoryWriteInterceptor(@Value("CMExternalChannel") ContentType contentType,
                                                                CommerceConnectionSupplier commerceConnectionSupplier,
                                                                AugmentationService categoryAugmentationService,
                                                                SitesService sitesService,
                                                                ContentRepository contentRepository) {

    var externalChannelInterceptor = new ChangeCategoryWriteInterceptor(contentType,
            commerceConnectionSupplier, categoryAugmentationService,
            sitesService, contentRepository);
    externalChannelInterceptor.setType(contentType);
    externalChannelInterceptor.setInterceptingSubtypes(true);
    return externalChannelInterceptor;
  }

  @Bean
  ChangeProductWriteInterceptor changeProductWriteInterceptor(@Value("CMExternalProduct") ContentType contentType,
                                                               CommerceConnectionSupplier commerceConnectionSupplier,
                                                               AugmentationService categoryAugmentationService,
                                                               SitesService sitesService,
                                                               ContentRepository contentRepository) {

    var externalProductInterceptor = new ChangeProductWriteInterceptor(contentType,
            commerceConnectionSupplier, categoryAugmentationService,
            sitesService, contentRepository);
    externalProductInterceptor.setType(contentType);
    externalProductInterceptor.setInterceptingSubtypes(true);
    return externalProductInterceptor;
  }
}
