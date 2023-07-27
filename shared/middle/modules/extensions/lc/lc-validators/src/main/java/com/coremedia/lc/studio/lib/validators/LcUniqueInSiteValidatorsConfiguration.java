package com.coremedia.lc.studio.lib.validators;

import com.coremedia.blueprint.base.multisite.BlueprintMultisiteConfiguration;
import com.coremedia.blueprint.base.rest.validators.UniqueInSiteStringValidator;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.observe.ObservedPropertyService;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
@Import({
        BlueprintMultisiteConfiguration.class,
})
class LcUniqueInSiteValidatorsConfiguration {

  private static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";
  private static final String CM_EXTERNAL_PAGE = "CMExternalPage";
  private static final String EXTERNAL_ID = "externalId";

  @Bean
  UniqueInSiteStringValidator externalPageUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                    SitesService sitesService) {
    CapPropertyDescriptor capPropertyDescriptor = lookupDescriptor(contentRepository, CM_EXTERNAL_PAGE);
    ObservedPropertyService observedPropertyService = contentRepository.getObservedPropertyService();
    Function<String, Set<Content>> lookupFunction =
            (String value) -> observedPropertyService.getContentsWithValue(value, capPropertyDescriptor);
    return new UniqueInSiteStringValidator(type(contentRepository, CM_EXTERNAL_PAGE), false, EXTERNAL_ID, lookupFunction, sitesService);
  }

  @Bean
  UniqueInSiteStringValidator externalChannelUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                       SitesService sitesService) {
    CapPropertyDescriptor capPropertyDescriptor = lookupDescriptor(contentRepository, CM_EXTERNAL_CHANNEL);
    ObservedPropertyService observedPropertyService = contentRepository.getObservedPropertyService();
    Function<String, Set<Content>> lookupFunction =
            (String value) -> observedPropertyService.getContentsWithValue(value, capPropertyDescriptor);
    return new UniqueInSiteStringValidator(type(contentRepository, CM_EXTERNAL_CHANNEL), false, EXTERNAL_ID, lookupFunction, sitesService);
  }

  private static CapPropertyDescriptor lookupDescriptor(ContentRepository contentRepository, String documentTypeName) {
    ContentType contentType = contentRepository.getContentType(documentTypeName);
    if (contentType == null) {
      throw new IllegalStateException("Required content type " + documentTypeName + " not found.");
    }
    CapPropertyDescriptor descriptor = contentType.getDescriptor(EXTERNAL_ID);
    if (descriptor == null) {
      throw new IllegalStateException("Required property decriptor " + EXTERNAL_ID + " not found on type " + documentTypeName + ".");
    }
    return descriptor;
  }

  @NonNull
  private static ContentType type(@NonNull ContentRepository repository, @NonNull String typeStr) {
    return Objects.requireNonNull(repository.getContentType(typeStr));
  }
}
