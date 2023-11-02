package com.coremedia.blueprint.caas.augmentation;

import com.coremedia.blueprint.base.caas.search.HeadlessSearchAutoConfiguration;
import com.coremedia.blueprint.base.caas.web.BlueprintBaseMediaConfig;
import com.coremedia.blueprint.base.livecontext.augmentation.AugmentationAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesConfiguration;
import com.coremedia.blueprint.base.multisite.BlueprintMultisiteConfiguration;
import com.coremedia.blueprint.base.tree.TreeRelationServicesConfiguration;
import com.coremedia.blueprint.coderesources.ThemeServiceConfiguration;
import com.coremedia.blueprint.headlessserver.CaasConfig;
import com.coremedia.caas.media.TransformationServiceConfiguration;
import com.coremedia.caas.richtext.RichtextTransformerRegistry;
import com.coremedia.caas.web.controller.graphql.GraphQLController;
import com.coremedia.caas.wrapper.UrlPathFormater;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.asset.AssetSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.dataloader.CacheMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.ARTICLE_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.AUGMENTED_PAGE_EXTERNAL_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.AUGMENTED_PAGE_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.CATALOG_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.CATEGORY_EXTERNAL_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.CATEGORY_REFERENCE;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.DOWNLOAD_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.GRID_NAME;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.MASTER_SITE_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.PDPPAGEGRID_CSS_CLASS_NAME;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.PICTURE_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.PRODUCT_EXTERNAL_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.PRODUCT_LIST_ID;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.ROOT_CHANNEL_SEGMENT;
import static com.coremedia.blueprint.caas.augmentation.TestRepoConstants.VISUAL_ID;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        HeadlessAugmentationConfiguration.class,
        AugmentationAutoConfiguration.class,
        BaseCommerceServicesConfiguration.class,
        HeadlessSearchAutoConfiguration.class,
        CaasConfig.class,
        BlueprintBaseMediaConfig.class,
        BlueprintMultisiteConfiguration.class,
        GraphQLController.class,
        TransformationServiceConfiguration.class,
        ThemeServiceConfiguration.class,
        TreeRelationServicesConfiguration.class,
        CommerceSchemaGraphQLTest.LocalTestConfiguration.class,
}, properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/content/contentrepository.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml",
        "cache.capacities.java.lang.Object=20",
})
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SuppressWarnings("java:S2699") // assertions from spring-graphql are not detected by sonar
class CommerceSchemaGraphQLTest {

  @MockBean
  UrlPathFormater urlPathFormater;
  @MockBean
  ObjectMapper objectMapper;
  @MockBean
  CacheMap remoteLinkCacheMap;
  @MockBean(name = "solrClient")
  SolrClient solrClient;
  @MockBean
  RichtextTransformerRegistry richtextTransformerRegistry;
  @MockBean
  AssetSearchService assetSearchService;

  @Autowired
  ContentRepository contentRepository;
  @Autowired
  SitesService sitesService;
  @Autowired
  private WebTestClient webTestClient;
  private WebGraphQlTester webGraphQlTester;
  private Site site;

  @BeforeAll
  void setUp() {
    webGraphQlTester = HttpGraphQlTester.builder(
                    webTestClient
                            .mutate()
                            .baseUrl("/graphql")
            )
            .header("Content-Type", "application/json")
            .build();
    site = Objects.requireNonNull(sitesService.getSite(MASTER_SITE_ID));
  }

  @BeforeEach
  void setupMocks() {
    when(assetSearchService.searchAssets("CMPicture", PRODUCT_EXTERNAL_ID, site))
            .thenReturn(List.of(contentRepository.getContent(PICTURE_ID)));
    when(assetSearchService.searchAssets("CMDownload", PRODUCT_EXTERNAL_ID, site))
            .thenReturn(List.of(contentRepository.getContent(DOWNLOAD_ID)));
    when(assetSearchService.searchAssets("CMVisual", PRODUCT_EXTERNAL_ID, site))
            .thenReturn(List.of(contentRepository.getContent(VISUAL_ID)));
  }

  @ParameterizedTest
  @CsvSource(value = {
          "path               | contentId | values",
          "root-en            | 111112    | c1,c2",
          "root-en/subpage-en | 1111112   | ",
          "root-de            | 11144     | ",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testGetPageByPath(String path, String contentId, String values) {
    var externalIds = values != null ? values.split(",") : new String[0];
    webGraphQlTester.documentName("pageByPath")
            .variable("path", path)
            .execute()
            .path("content.pageByPath.id").entity(String.class).isEqualTo(contentId)
            .path("content.pageByPath.children").hasValue()
            .path("content.pageByPath.children[*].categoryRef.externalId")
            .entityList(String.class).containsExactly(externalIds);
  }

  @Test
  void testAugmentationForCommerceIdBySite() {
    webGraphQlTester.documentName("commerce/augmentationForCommerceIdBySite")
            .variable("commerceId", CATEGORY_REFERENCE)
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("commerce.augmentationForCommerceIdBySite").hasValue()
            .path("commerce.augmentationForCommerceIdBySite.id").hasValue()
            .path("commerce.augmentationForCommerceIdBySite.grid").hasValue()
            .path("commerce.augmentationForCommerceIdBySite.commerceRef").hasValue()
            .path("commerce.augmentationForCommerceIdBySite.commerceRef.externalId")
            .entity(String.class).isEqualTo(CATEGORY_EXTERNAL_ID);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-stuff                           | cool-stuff",
          "mock:///catalog/category/cool-stuff  | cool-stuff",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testCategoryAugmentationBySite(String input, String expected) {
    webGraphQlTester.documentName("commerce/categoryAugmentationBySite")
            .variable("externalId", input)
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("commerce.categoryAugmentationBySite").hasValue()
            .path("commerce.categoryAugmentationBySite.id").hasValue()
            .path("commerce.categoryAugmentationBySite.grid").hasValue()
            .path("commerce.categoryAugmentationBySite.grid.name")
            .entity(String.class).isEqualTo(GRID_NAME)
            .path("commerce.categoryAugmentationBySite.grid.rows[*]").entityList(Object.class).hasSize(1)
            .path("commerce.categoryAugmentationBySite.grid.placements[*]").entityList(Object.class).hasSize(1)
            .path("commerce.categoryAugmentationBySite.grid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("commerce.categoryAugmentationBySite.commerceRef").hasValue()
            .path("commerce.categoryAugmentationBySite.commerceRef.catalogId")
            .entity(String.class).isEqualTo(CATALOG_ID)
            .path("commerce.categoryAugmentationBySite.commerceRef.externalId").entity(String.class).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-stuff                           | cool-stuff",
          "mock:///catalog/category/cool-stuff  | cool-stuff",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testCategoryAugmentationByStore(String input, String expected) {
    webGraphQlTester.documentName("commerce/categoryAugmentationByStore")
            .variable("externalId", input)
            .variable("catalogId", "catalog")
            .variable("storeId", "store")
            .variable("locale", "en-US")
            .execute()
            .path("commerce.categoryAugmentationByStore").hasValue()
            .path("commerce.categoryAugmentationByStore.id").hasValue()
            .path("commerce.categoryAugmentationByStore.grid").hasValue()
            .path("commerce.categoryAugmentationByStore.grid.name").entity(String.class).isEqualTo(GRID_NAME)
            .path("commerce.categoryAugmentationByStore.grid.rows[*]").entityList(Object.class).hasSize(1)
            .path("commerce.categoryAugmentationByStore.grid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("commerce.categoryAugmentationByStore.commerceRef").hasValue()
            .path("commerce.categoryAugmentationByStore.commerceRef.catalogId")
            .entity(String.class).isEqualTo(CATALOG_ID)
            .path("commerce.categoryAugmentationByStore.commerceRef.externalId")
            .entity(String.class).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-product                         | cool-product",
          "mock:///catalog/product/cool-product | cool-product",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testProductAugmentationBySite(String input, String expected) {
    webGraphQlTester.documentName("commerce/productAugmentationBySite")
            .variable("externalId", input)
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("commerce.productAugmentationBySite").hasValue()
            .path("commerce.productAugmentationBySite.pdpPagegrid.cssClassName")
            .entity(String.class).isEqualTo(PDPPAGEGRID_CSS_CLASS_NAME)
            .path("commerce.productAugmentationBySite.pdpPagegrid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("commerce.productAugmentationBySite.pdpPagegrid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("commerce.productAugmentationBySite.commerceRef").hasValue()
            .path("commerce.productAugmentationBySite.commerceRef.externalId")
            .entity(String.class).isEqualTo(expected)
            .path("commerce.productAugmentationBySite.downloads")
            .entityList(Object.class).containsExactly(Map.of("id", DOWNLOAD_ID))
            .path("commerce.productAugmentationBySite.visuals")
            .entityList(Object.class).containsExactly(Map.of("id", VISUAL_ID))
            .path("commerce.productAugmentationBySite.picture")
            .entity(Map.class).isEqualTo(Map.of("id", PICTURE_ID));
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-product                         | cool-product",
          "mock:///catalog/product/cool-product | cool-product",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testProductAugmentationByStore(String input, String expected) {
    webGraphQlTester.documentName("commerce/productAugmentationByStore")
            .variable("externalId", input)
            .variable("catalogId", "catalog")
            .variable("storeId", "store")
            .variable("locale", "en-US")
            .execute()
            .path("commerce.productAugmentationByStore").hasValue()
            .path("commerce.productAugmentationByStore.pdpPagegrid").hasValue()
            .path("commerce.productAugmentationByStore.pdpPagegrid.cssClassName")
            .entity(String.class).isEqualTo(PDPPAGEGRID_CSS_CLASS_NAME)
            .path("commerce.productAugmentationByStore.pdpPagegrid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("commerce.productAugmentationByStore.pdpPagegrid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("commerce.productAugmentationByStore.commerceRef").hasValue()
            .path("commerce.productAugmentationByStore.commerceRef.externalId")
            .entity(String.class).isEqualTo(expected)
            .path("commerce.productAugmentationByStore.downloads")
            .entityList(Object.class).containsExactly(Map.of("id", DOWNLOAD_ID))
            .path("commerce.productAugmentationByStore.visuals")
            .entityList(Object.class).containsExactly(Map.of("id", VISUAL_ID))
            .path("commerce.productAugmentationByStore.picture")
            .entity(Map.class).isEqualTo(Map.of("id", PICTURE_ID));
  }

  @Test
  void testGetExternalPage() {
    // test commerce-reference-schema-ext.graphql
    webGraphQlTester.documentName("commerce/externalPageById")
            .variable("externalId", AUGMENTED_PAGE_EXTERNAL_ID)
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("commerce.externalPage").hasValue()
            .path("commerce.externalPage.id").entity(Integer.class).isEqualTo(AUGMENTED_PAGE_ID)
            .path("commerce.externalPage.externalId").entity(String.class).isEqualTo(AUGMENTED_PAGE_EXTERNAL_ID)
            .path("commerce.externalPage.type").entity(String.class).isEqualTo("CMExternalPage");
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-stuff                           | cool-stuff",
          "mock:///catalog/category/cool-stuff  | cool-stuff",
          "NIL                                  | c",
  }, useHeadersInDisplayName = true, delimiter = '|', nullValues = "NIL")
  void testCategoryAugmentationBySegment(String input, String expected) {
    webGraphQlTester.documentName("content/categoryAugmentationBySegment")
            .variable("externalId", input)
            .variable("breadcrumb", "a/b/c")
            .variable("catalogAlias", "test")
            .variable("rootSegment", ROOT_CHANNEL_SEGMENT)
            .execute()
            .path("content.categoryAugmentationBySegment").hasValue()
            .path("content.categoryAugmentationBySegment.grid").hasValue()
            .path("content.categoryAugmentationBySegment.grid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("content.categoryAugmentationBySegment.grid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("content.categoryAugmentationBySegment.commerceRef").hasValue()
            .path("content.categoryAugmentationBySegment.commerceRef.catalogId")
            .entity(String.class).isEqualTo(CATALOG_ID)
            .path("content.categoryAugmentationBySegment.commerceRef.externalId")
            .entity(String.class).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "input                                | expected",
          "cool-stuff                           | cool-stuff",
          "mock:///catalog/category/cool-stuff  | cool-stuff",
          "NIL                                  | c",
  }, useHeadersInDisplayName = true, delimiter = '|', nullValues = "NIL")
  void testCategoryAugmentationBySite1(String input, String expected) {
    webGraphQlTester.documentName("content/categoryAugmentationBySite")
            .variable("externalId", input)
            .variable("breadcrumb", "a/b/c")
            .variable("catalogAlias", "test")
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("content.categoryAugmentationBySite").hasValue()
            .path("content.categoryAugmentationBySite.grid").hasValue()
            .path("content.categoryAugmentationBySite.grid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("content.categoryAugmentationBySite.grid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("content.categoryAugmentationBySite.commerceRef.catalogId")
            .entity(String.class).isEqualTo(CATALOG_ID)
            .path("content.categoryAugmentationBySite.commerceRef").hasValue()
            .path("content.categoryAugmentationBySite.commerceRef.externalId")
            .entity(String.class).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "externalId                           | catalogAlias | expectedExternalId",
          "cool-product                         | test         | cool-product",
          "mock:///catalog/product/cool-product | catalog      | cool-product",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testProductAugmentationBySegment(String externalId, String catalogAlias, String expectedExternalId) {
    webGraphQlTester.documentName("content/productAugmentationBySegment")
            .variable("externalId", externalId)
            .variable("breadcrumb", "a/b/c")
            .variable("catalogAlias", catalogAlias)
            .variable("rootSegment", ROOT_CHANNEL_SEGMENT)
            .execute()
            .path("content.productAugmentationBySegment").hasValue()
            .path("content.productAugmentationBySegment.id").hasValue()
            .path("content.productAugmentationBySegment.pdpPagegrid.cssClassName").hasValue()
            .path("content.productAugmentationBySegment.pdpPagegrid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("content.productAugmentationBySegment.pdpPagegrid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("content.productAugmentationBySegment.commerceRef").hasValue()
            .path("content.productAugmentationBySegment.commerceRef.catalogId")
            .entity(String.class).isEqualTo(CATALOG_ID)
            .path("content.productAugmentationBySegment.commerceRef.externalId")
            .entity(String.class).isEqualTo(expectedExternalId)
            .path("content.productAugmentationBySegment.downloads")
            .entityList(Object.class).containsExactly(Map.of("id", DOWNLOAD_ID))
            .path("content.productAugmentationBySegment.visuals")
            .entityList(Object.class).containsExactly(Map.of("id", VISUAL_ID))
            .path("content.productAugmentationBySegment.picture")
            .entity(Map.class).isEqualTo(Map.of("id", PICTURE_ID));
  }

  @ParameterizedTest
  @CsvSource(value = {
          "externalId                           | catalogAlias | expectedExternalId",
          "cool-product                         | test         | cool-product",
          "mock:///catalog/product/cool-product | catalog      | cool-product",
  }, useHeadersInDisplayName = true, delimiter = '|')
  void testProductAugmentationBySite1(String externalId, String catalogAlias, String expectedExternalId) {
    webGraphQlTester.documentName("content/productAugmentationBySite")
            .variable("externalId", externalId)
            .variable("breadcrumb", "a/b/c")
            .variable("catalogAlias", catalogAlias)
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("content.productAugmentationBySite").hasValue()
            .path("content.productAugmentationBySite.id").hasValue()
            .path("content.productAugmentationBySite.pdpPagegrid.cssClassName")
            .entity(String.class).isEqualTo(PDPPAGEGRID_CSS_CLASS_NAME)
            .path("content.productAugmentationBySite.pdpPagegrid.rows[*]")
            .entityList(Object.class).hasSize(1)
            .path("content.productAugmentationBySite.pdpPagegrid.placements[*].items[*]")
            .entityList(Object.class).containsExactly(Map.of("id", ARTICLE_ID))
            .path("content.productAugmentationBySite.commerceRef").hasValue()
            .path("content.productAugmentationBySite.commerceRef.externalId")
            .entity(String.class).isEqualTo(expectedExternalId)
            .path("content.productAugmentationBySite.downloads")
            .entityList(Object.class).containsExactly(Map.of("id", DOWNLOAD_ID))
            .path("content.productAugmentationBySite.visuals")
            .entityList(Object.class).containsExactly(Map.of("id", VISUAL_ID))
            .path("content.productAugmentationBySite.picture")
            .entity(Map.class).isEqualTo(Map.of("id", PICTURE_ID));
  }

  @Test
  void testGetProductList() {
    // test commerce-reference-schema-ext.graphql
    webGraphQlTester.documentName("content/productListById")
            .variable("id", PRODUCT_LIST_ID)
            .execute()
            .path("content.productList.externalId").entity(String.class).isEqualTo(CATEGORY_REFERENCE)
            .path("content.productList.categoryRef.externalId").entity(String.class).isEqualTo(CATEGORY_EXTERNAL_ID)
            .path("content.productList.productItems[*]").entityList(Object.class).hasSize(0);
  }

  @Configuration(proxyBeanMethods = false)
  public static class LocalTestConfiguration {
    @Bean
    BeanResolver pluginSchemaAdapterBeansResolver(BeanFactory beanFactory) {
      return new BeanFactoryResolver(beanFactory);
    }
  }
}
