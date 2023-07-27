package com.coremedia.blueprint.caas.augmentation;

import com.coremedia.blueprint.base.livecontext.augmentation.tree.ExternalChannelContentTreeRelation;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceSiteFinder;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.caas.augmentation.adapter.AugmentationPageGridAdapterFactory;
import com.coremedia.blueprint.caas.augmentation.adapter.CommerceRefAdapter;
import com.coremedia.blueprint.caas.augmentation.model.AssetFacade;
import com.coremedia.blueprint.caas.augmentation.model.AugmentationFacade;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.asset.AssetService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import static com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
import static com.coremedia.blueprint.caas.augmentation.adapter.AugmentationPageGridAdapterFactory.PDP_PAGEGRID_PROPERTY_NAME;

@Configuration(proxyBeanMethods = false)
@ImportResource(value = {
        "classpath:com/coremedia/blueprint/base/pagegrid/impl/bpbase-pagegrid-services.xml"},
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@PropertySource("classpath:/META-INF/coremedia/headless-server-ec-augmentation-defaults.properties")
public class HeadlessAugmentationCommerceConfiguration {

  @Bean
  public AssetFacade assetFacade(AssetService assetService, CommerceRefHelper commerceRefHelper) {
    return new AssetFacade(assetService, commerceRefHelper);
  }

  @Bean
  public AugmentationFacade augmentationFacade(AugmentationService categoryAugmentationService,
                                               AugmentationService productAugmentationService,
                                               SitesService sitesService,
                                               CommerceConnectionHelper commerceConnectionHelper,
                                               CatalogAliasTranslationService catalogAliasTranslationService,
                                               CommerceSiteFinder commerceSiteFinder) {
    return new AugmentationFacade(categoryAugmentationService, productAugmentationService, sitesService,
            commerceConnectionHelper, catalogAliasTranslationService, commerceSiteFinder);
  }

  @Bean
  public CommerceConnectionHelper commerceConnectionHelper(CommerceConnectionSupplier CommerceConnectionSupplier) {
    return new CommerceConnectionHelper(CommerceConnectionSupplier);
  }

  @Bean
  public AugmentationPageGridAdapterFactory categoryPageGridAdapterDelegate(
          AugmentationService categoryAugmentationService,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
          ContentBackedPageGridService categoryContentBackedPageGridService,
          SitesService sitesService, CommerceConnectionHelper commerceConnectionHelper) {
    return new AugmentationPageGridAdapterFactory(
            PAGE_GRID_STRUCT_PROPERTY,
            categoryAugmentationService,
            externalChannelContentTreeRelation,
            categoryContentBackedPageGridService,
            sitesService, commerceConnectionHelper);
  }

  @Bean
  public AugmentationPageGridAdapterFactory productPageGridAdapterDelegate(
          AugmentationService productAugmentationService,
          ExternalChannelContentTreeRelation externalChannelContentTreeRelation,
          ContentBackedPageGridService productContentBackedPageGridService,
          SitesService sitesService, CommerceConnectionHelper commerceConnectionHelper) {
    return new AugmentationPageGridAdapterFactory(
            PDP_PAGEGRID_PROPERTY_NAME,
            productAugmentationService,
            externalChannelContentTreeRelation,
            productContentBackedPageGridService,
            sitesService, commerceConnectionHelper);
  }

  @Bean
  public CommerceRefAdapter commerceRefAdapterDelegate(SitesService sitesService, CommerceConnectionHelper commerceConnectionHelper, CatalogAliasTranslationService catalogAliasTranslationService){
    return new CommerceRefAdapter(sitesService, commerceConnectionHelper, catalogAliasTranslationService);
  }
}
