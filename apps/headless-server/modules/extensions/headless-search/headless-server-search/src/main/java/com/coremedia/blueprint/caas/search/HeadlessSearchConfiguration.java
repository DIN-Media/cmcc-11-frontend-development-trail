package com.coremedia.blueprint.caas.search;

import com.coremedia.blueprint.base.caas.model.adapter.NavigationAdapterFactory;
import com.coremedia.blueprint.base.caas.model.adapter.QueryListAdapterFactory;
import com.coremedia.blueprint.base.caas.model.adapter.SearchServiceAdapterFactory;
import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.caas.config.CaasSearchConfigurationProperties;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.caas.search.id.CaasContentBeanIdScheme;
import com.coremedia.caas.search.solr.SolrCaeQueryBuilder;
import com.coremedia.caas.search.solr.SolrQueryBuilder;
import com.coremedia.caas.search.solr.SolrSearchResultFactory;
import com.coremedia.caas.web.CaasServiceConfigurationProperties;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.id.IdScheme;
import com.coremedia.search.solr.client.SolrClientConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        CaasSearchConfigurationProperties.class,
        CaasServiceConfigurationProperties.class,
})
@Import({
        SolrClientConfiguration.class,
})
@ImportResource(value = {
        "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
        "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
        "classpath:/com/coremedia/blueprint/base/navigation/context/bpbase-default-contextstrategy.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class HeadlessSearchConfiguration {

  private final CaasSearchConfigurationProperties caasSearchConfigurationProperties;
  private final CaasServiceConfigurationProperties caasServiceConfigurationProperties;

  public HeadlessSearchConfiguration(CaasSearchConfigurationProperties caasSearchConfigurationProperties,
                                     CaasServiceConfigurationProperties caasServiceConfigurationProperties) {
    this.caasSearchConfigurationProperties = caasSearchConfigurationProperties;
    this.caasServiceConfigurationProperties = caasServiceConfigurationProperties;
  }

  @Bean
  public SearchServiceAdapterFactory searchServiceAdapter(@Qualifier("searchResultFactory") SolrSearchResultFactory searchResultFactory,
                                                          ContentRepository contentRepository,
                                                          @Qualifier("settingsService") SettingsService settingsService,
                                                          SitesService sitesService,
                                                          List<IdScheme> idSchemes,
                                                          @Qualifier("caeSolrQueryBuilder") SolrQueryBuilder solrQueryBuilder) {
    return new SearchServiceAdapterFactory(searchResultFactory, contentRepository, settingsService, sitesService, idSchemes, solrQueryBuilder);
  }

  @Bean
  public SolrQueryBuilder caeSolrQueryBuilder() {
    return new SolrCaeQueryBuilder("/cmdismax");
  }

  @Bean
  public SolrQueryBuilder dynamicContentSolrQueryBuilder() {
    return new SolrCaeQueryBuilder("/select");
  }

  @Bean
  @SuppressWarnings("squid:S00107")
  public QueryListAdapterFactory queryListAdapter(@Qualifier("queryListSearchResultFactory") SolrSearchResultFactory searchResultFactory,
                                                  ContentRepository contentRepository,
                                                  @Qualifier("settingsService") SettingsService settingsService,
                                                  SitesService sitesService,
                                                  List<IdScheme> idSchemes,
                                                  @Qualifier("dynamicContentSolrQueryBuilder") SolrQueryBuilder solrQueryBuilder,
                                                  @Qualifier("collectionExtendedItemsAdapter") ExtendedLinkListAdapterFactory collectionExtendedItemsAdapter,
                                                  @Qualifier("navigationAdapter") NavigationAdapterFactory navigationAdapterFactory) {
    return new QueryListAdapterFactory(searchResultFactory, contentRepository, settingsService, sitesService, idSchemes, solrQueryBuilder, collectionExtendedItemsAdapter, navigationAdapterFactory);
  }

  @Bean
  public SolrSearchResultFactory queryListSearchResultFactory(@Qualifier("solrClient") SolrClient solrClient,
                                                              ContentRepository contentRepository) {
    SolrSearchResultFactory solrSearchResultFactory = new SolrSearchResultFactory(contentRepository, solrClient, caasServiceConfigurationProperties.getSolr().getCollection());
    if (!caasServiceConfigurationProperties.isPreview()) {
      solrSearchResultFactory.setCacheForSeconds(this.caasServiceConfigurationProperties.getQuerylistSearchCacheForSeconds());
    }
    return solrSearchResultFactory;
  }

  @Bean
  public SolrSearchResultFactory searchResultFactory(@Qualifier("solrClient") SolrClient solrClient,
                                                     ContentRepository contentRepository) {
    SolrSearchResultFactory solrSearchResultFactory = new SolrSearchResultFactory(contentRepository, solrClient, caasServiceConfigurationProperties.getSolr().getCollection());
    if (!caasServiceConfigurationProperties.isPreview()) {
      solrSearchResultFactory.setCacheForSeconds(caasSearchConfigurationProperties.getSeconds());
    }
    return solrSearchResultFactory;
  }

  @Bean
  public ExtendedLinkListAdapterFactory collectionExtendedItemsAdapter() {
    return new ExtendedLinkListAdapterFactory("extendedItems", "links", "items", "CMLinkable", "target");
  }

  @Bean
  public NavigationAdapterFactory navigationAdapter(@Qualifier("contentContextStrategy") ContextStrategy<Content, Content> contextStrategy, Map<String, TreeRelation<Content>> treeRelations) {
    return new NavigationAdapterFactory(contextStrategy, treeRelations);
  }

  @Bean
  public IdScheme caasContentBeanIdScheme(ContentRepository contentRepository) {
    return new CaasContentBeanIdScheme(contentRepository);
  }

  @Bean
  public List<IdScheme> idSchemes(IdScheme caasContentBeanIdScheme) {
    return Collections.singletonList(caasContentBeanIdScheme);
  }
}