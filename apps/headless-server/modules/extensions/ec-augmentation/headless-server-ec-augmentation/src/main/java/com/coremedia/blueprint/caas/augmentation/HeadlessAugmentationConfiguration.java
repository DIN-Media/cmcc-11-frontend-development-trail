package com.coremedia.blueprint.caas.augmentation;

import com.coremedia.blueprint.base.links.RuleProvider;
import com.coremedia.blueprint.base.links.impl.AbsoluteUrlPrefixRuleProvider;
import com.coremedia.blueprint.base.links.impl.ApplicationPropertyReplacerFormatter;
import com.coremedia.blueprint.base.links.impl.RuleUrlPrefixResolver;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.caas.augmentation.adapter.AugmentationPageGridAdapterFactory;
import com.coremedia.blueprint.caas.augmentation.adapter.AugmentationPageGridAdapterFactoryCmsOnly;
import com.coremedia.blueprint.caas.augmentation.adapter.AugmentationPageGridAdapterFactoryDispatcher;
import com.coremedia.blueprint.caas.augmentation.adapter.CommerceRefAdapter;
import com.coremedia.blueprint.caas.augmentation.adapter.CommerceRefAdapterCmsOnly;
import com.coremedia.blueprint.caas.augmentation.adapter.CommerceRefAdapterDispatcher;
import com.coremedia.blueprint.caas.augmentation.adapter.CommerceSearchFacade;
import com.coremedia.blueprint.caas.augmentation.adapter.ProductListAdapterFactory;
import com.coremedia.blueprint.caas.augmentation.model.Augmentation;
import com.coremedia.blueprint.caas.augmentation.model.AugmentationContext;
import com.coremedia.blueprint.caas.augmentation.model.CategoryAugmentation;
import com.coremedia.blueprint.caas.augmentation.model.CategoryAugmentationCmsOnly;
import com.coremedia.blueprint.caas.augmentation.model.CommerceRef;
import com.coremedia.blueprint.caas.augmentation.model.ProductAugmentation;
import com.coremedia.blueprint.caas.augmentation.model.ProductAugmentationCmsOnly;
import com.coremedia.caas.config.CaasSearchConfigurationProperties;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.caas.search.solr.SolrQueryBuilder;
import com.coremedia.caas.search.solr.SolrSearchResultFactory;
import com.coremedia.caas.wiring.ProvidesTypeNameResolver;
import com.coremedia.caas.wiring.TypeNameResolver;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.id.IdScheme;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        CaasAssetSearchServiceConfigProperties.class,
})
@ComponentScan({
        "com.coremedia.livecontext.asset.impl",
})
@Import({
        HeadlessAugmentationCommerceConfiguration.class,
        HeadlessAugmentationCmsOnlyConfiguration.class,
})
public class HeadlessAugmentationConfiguration {

  public static final String CATEGORY_REF = "CategoryRef";
  public static final String PRODUCT_REF = "ProductRef";

  private static final Set<String> AUGMENTATION_GQL_INTERFACES = Set.of(
          Augmentation.class.getSimpleName(),
          CategoryAugmentation.class.getSimpleName(),
          CategoryAugmentationCmsOnly.class.getSimpleName(),
          ProductAugmentation.class.getSimpleName(),
          ProductAugmentationCmsOnly.class.getSimpleName(),
          CommerceRef.class.getSimpleName()
  );


  private static final Map<Class, String> AUGMENTATION_TYPE_RESOLVE_MAP = Map.of(
          ProductAugmentation.class, "ProductAugmentationImpl",
          ProductAugmentationCmsOnly.class, "ProductAugmentationImpl",
          CategoryAugmentationCmsOnly.class, "CategoryAugmentationImpl",
          CategoryAugmentation.class, "CategoryAugmentationImpl"
  );

  @Value("${link.urlPrefixType:''}")
  private String urlPrefixType;

  // Indicate for which interface we provide a type resolver
  @Bean
  public ProvidesTypeNameResolver providesAugmentationTypeNameResolver() {
    return typeName -> AUGMENTATION_GQL_INTERFACES.contains(typeName)
            ? Optional.of(true)
            : Optional.empty();
  }

  @Bean("query-root:commerce")
  @Qualifier("queryRoot")
  public Object commerceAugmentation() {
    return new Object();
  }

  @Bean
  public TypeNameResolver<Augmentation> augmentationTypeNameResolver() {
    return augmentation -> Optional.ofNullable(AUGMENTATION_TYPE_RESOLVE_MAP.get(augmentation.getClass()));
  }

  @Bean
  public TypeNameResolver<CommerceRef> commerceRefTypeNameResolver() {
    return commerceRef -> {
      CommerceBeanType type = commerceRef.getType();
      if (type.equals(BaseCommerceBeanType.CATEGORY)) {
        return Optional.of(CATEGORY_REF);
      } else if (type.equals(BaseCommerceBeanType.PRODUCT) || type.equals(BaseCommerceBeanType.SKU)) {
        return Optional.of(PRODUCT_REF);
      }
      return Optional.of(CommerceRef.class.getSimpleName());
    };
  }

  @Bean
  public SolrSearchResultFactory caasAssetSearchServiceSearchResultFactory(@Qualifier("solrClient") SolrClient solrClient,
                                                                           ContentRepository contentRepository,
                                                                           CaasSearchConfigurationProperties caasSearchConfigurationProperties,
                                                                           CaasAssetSearchServiceConfigProperties caasAssetSearchServiceConfigProperties) {
    SolrSearchResultFactory solrSearchResultFactory = new SolrSearchResultFactory(contentRepository, solrClient, caasSearchConfigurationProperties.getSolr().getCollection());
    solrSearchResultFactory.setCacheForSeconds(caasAssetSearchServiceConfigProperties.getCacheSeconds());
    return solrSearchResultFactory;
  }

  @Bean
  public CaasAssetSearchService caasAssetSearchService(CaasAssetSearchServiceConfigProperties caasAssetSearchServiceConfigProperties,
                                                       @Qualifier("caasAssetSearchServiceSearchResultFactory") SolrSearchResultFactory searchResultFactory,
                                                       ContentRepository contentRepository,
                                                       List<IdScheme> idSchemes,
                                                       @Qualifier("dynamicContentSolrQueryBuilder") SolrQueryBuilder solrQueryBuilder) {
    return new CaasAssetSearchService(caasAssetSearchServiceConfigProperties, searchResultFactory, contentRepository, idSchemes, solrQueryBuilder);
  }

  @Bean
  public CommerceSearchFacade commerceSearchHelper(CommerceConnectionHelper commerceConnectionHelper) {
    return new CommerceSearchFacade(commerceConnectionHelper);
  }

  @Bean
  public RuleUrlPrefixResolver ruleUrlPrefixResolver(List<RuleProvider> ruleProviders) {
    RuleUrlPrefixResolver ruleUrlPrefixResolver = new RuleUrlPrefixResolver();
    ruleUrlPrefixResolver.setRuleProviders(ruleProviders);
    return ruleUrlPrefixResolver;
  }

  @Bean
  public ApplicationPropertyReplacerFormatter applicationPropertyReplacerFormatter() {
    return new ApplicationPropertyReplacerFormatter();
  }

  @Bean
  public AbsoluteUrlPrefixRuleProvider absoluteUrlPrefixRuleProvider(SitesService sitesService,
                                                                     SettingsService settingsService,
                                                                     Cache cache,
                                                                     ApplicationPropertyReplacerFormatter urlPrefixProcessor) {
    AbsoluteUrlPrefixRuleProvider ruleProvider = new AbsoluteUrlPrefixRuleProvider();
    ruleProvider.setSitesService(sitesService);
    ruleProvider.setSettingsService(settingsService);
    ruleProvider.setCache(cache);
    ruleProvider.setUrlPrefixProcessor(urlPrefixProcessor);
    ruleProvider.setUrlPrefixType(urlPrefixType);
    return ruleProvider;
  }

  @Bean
  public ProductListAdapterFactory productListAdapter(@Qualifier("settingsService") SettingsService settingsService,
                                                      @Qualifier("sitesService") SitesService sitesService,
                                                      @Qualifier("collectionExtendedItemsAdapter") ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory,
                                                      CommerceConnectionHelper commerceConnectionHelper,
                                                      CommerceSearchFacade commerceSearchFacade) {
    return new ProductListAdapterFactory(settingsService, sitesService, extendedLinkListAdapterFactory, commerceConnectionHelper, commerceSearchFacade);
  }

  @Bean
  public AugmentationPageGridAdapterFactoryDispatcher productPageGridAdapter(AugmentationPageGridAdapterFactory productPageGridAdapterDelegate,
                                                                             AugmentationPageGridAdapterFactoryCmsOnly productPageGridAdapterDelegateCmsOnly) {
    return new AugmentationPageGridAdapterFactoryDispatcher(productPageGridAdapterDelegate, productPageGridAdapterDelegateCmsOnly);
  }

  @Bean
  public AugmentationPageGridAdapterFactoryDispatcher categoryPageGridAdapter(AugmentationPageGridAdapterFactory categoryPageGridAdapterDelegate,
                                                                              AugmentationPageGridAdapterFactoryCmsOnly categoryPageGridAdapterDelegateCmsOnly) {
    return new AugmentationPageGridAdapterFactoryDispatcher(categoryPageGridAdapterDelegate, categoryPageGridAdapterDelegateCmsOnly);
  }

  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  AugmentationContext augmentationContext() {
    return new AugmentationContext();
  }

  @Bean
  public CommerceRefAdapterDispatcher commerceRefAdapter(ObjectProvider<AugmentationContext> augmentationContext,
                                                         CommerceRefAdapter commerceRefAdapterDelegate,
                                                         CommerceRefAdapterCmsOnly commerceRefAdapterDelegateCmsOnly) {
    return new CommerceRefAdapterDispatcher(augmentationContext, commerceRefAdapterDelegate, commerceRefAdapterDelegateCmsOnly);
  }

}
