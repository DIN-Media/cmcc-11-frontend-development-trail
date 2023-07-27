package com.coremedia.blueprint.assets.server;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import java.util.Map;

@AutoConfiguration
@ImportResource(value = "classpath:/framework/spring/blobstore/am/blobstore.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class AMServerAutoConfiguration {

  /**
   * Override removal behavior for a given rendition. Setting this to false might be used if the given
   * rendition is required on the download portal and must not be excluded upon publication.
   */
  @Bean
  public Map<String, Boolean> removeOverride() {
    return Map.of("thumbnail", false);
  }

  /**
   * A PublishInterceptor that makes sure to remove blobs on publication
   * unless the blobs are needed for the download portal.
   */
  @Bean
  public AssetPublishInterceptor assetPublishInterceptor(@Qualifier("removeOverride") Map<String, Boolean> removeOverride) {
    AssetPublishInterceptor assetPublishInterceptor = new AssetPublishInterceptor();
    assetPublishInterceptor.setType("AMAssets");
    assetPublishInterceptor.setInterceptingSubtypes(true);
    assetPublishInterceptor.setAssetMetadataProperty("metadata");
    assetPublishInterceptor.setRemoveDefault(true);
    assetPublishInterceptor.setRemoveOverride(removeOverride);

    return assetPublishInterceptor;
  }
}
