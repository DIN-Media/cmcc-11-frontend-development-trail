package com.coremedia.blueprint.headlessserver.contentschema;

import com.coremedia.blueprint.base.caas.web.BlueprintBaseMediaConfig;
import com.coremedia.blueprint.caas.p13n.P13nConfig;
import com.coremedia.blueprint.coderesources.ThemeServiceConfiguration;
import com.coremedia.blueprint.headlessserver.CaasConfig;
import com.coremedia.caas.media.TransformationServiceConfiguration;
import com.coremedia.caas.web.controller.graphql.GraphQLController;
import com.coremedia.caas.wrapper.UrlPathFormater;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataloader.CacheMap;
import org.junit.jupiter.api.BeforeAll;
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

import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ARTICLE_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ARTICLE_REPO_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ARTICLE_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_ROOT_CHANNEL_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_ROOT_CHANNEL_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_SITE_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_SITE_LOCALE;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_SITE_NAME;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DERIVED_SITE_REPO_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DOWNLOAD_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.DOWNLOAD_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.HTML_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.HTML_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.LINKED_SETTING_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MASTER_SITE_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MASTER_SITE_LOCALE;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MASTER_SITE_NAME;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MASTER_SITE_REPO_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MEDIA_DELIVERY_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.MEDIA_REPO_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.PICTURE_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.PICTURE_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ROOT_CHANNEL_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ROOT_CHANNEL_REPO_PATH;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ROOT_CHANNEL_SEGMENT;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ROOT_CHANNEL_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SELECTION_RULES_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SELECTION_RULES_UUID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SETTINGS_KEY;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SETTINGS_VALUE;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SUB_SETTINGS_KEY;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.SUB_SETTINGS_VALUE;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.VIDEO_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.VIDEO_UUID;

@SpringBootTest(classes = {
        CaasConfig.class,
        BlueprintBaseMediaConfig.class,
        P13nConfig.class,
        GraphQLController.class,
        TransformationServiceConfiguration.class,
        ThemeServiceConfiguration.class,
        ContentSchemaGraphQLTest.LocalTestConfiguration.class,
}, properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/content/contentrepository.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml"
})
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SuppressWarnings("java:S2699") // assertions from spring-graphql are not detected by sonar
class ContentSchemaGraphQLTest {

  @MockBean
  UrlPathFormater urlPathFormater;
  @MockBean
  ObjectMapper objectMapper;
  @MockBean
  CacheMap remoteLinkCacheMap;

  @Autowired
  private WebTestClient webTestClient;
  private WebGraphQlTester webGraphQlTester;

  @BeforeAll
  public void setUp() {
    webGraphQlTester = HttpGraphQlTester.builder(
                    webTestClient
                            .mutate()
                            .baseUrl("/graphql")
            )
            .header("Content-Type", "application/json")
            .build();
  }

  @Test
  void testGetAllSites() {
    webGraphQlTester.documentName("allSites")
            .execute()
            .path("content.sites[*].id").entityList(String.class).contains(MASTER_SITE_ID, DERIVED_SITE_ID)
            .path("content.sites[*].name").entityList(String.class).contains(MASTER_SITE_NAME, DERIVED_SITE_NAME)
            .path("content.sites[*].locale").entityList(String.class).contains(MASTER_SITE_LOCALE, DERIVED_SITE_LOCALE)
            .path("content.sites[*].repositoryPath").entityList(String.class).contains(MASTER_SITE_REPO_PATH, DERIVED_SITE_REPO_PATH)
            .path("content.sites[*].crops").hasValue()
            .path("content.sites[?(@.name == '" + MASTER_SITE_NAME + "')].root.id").entityList(Integer.class).containsExactly(ROOT_CHANNEL_ID)
            .path("content.sites[0].crops").hasValue();
  }

  @Test
  void testGetSiteById() {
    webGraphQlTester.documentName("siteById")
            .variable("siteId", MASTER_SITE_ID)
            .execute()
            .path("content.site.id").entity(String.class).isEqualTo(MASTER_SITE_ID)
            .path("content.site.name").entity(String.class).isEqualTo(MASTER_SITE_NAME)
            .path("content.site.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)
            .path("content.site.repositoryPath").entity(String.class).isEqualTo(MASTER_SITE_REPO_PATH)
            .path("content.site.root.id").entity(Integer.class).isEqualTo(ROOT_CHANNEL_ID)
            .path("content.site.derivedSites[0].id").entity(String.class).isEqualTo(DERIVED_SITE_ID)
            .path("content.site.crops").hasValue();
  }

  @Test
  void testGetSiteByRootSegment() {
    webGraphQlTester.documentName("siteByRootSegment")
            .variable("rootSegment", ROOT_CHANNEL_SEGMENT)
            .execute()
            .path("content.site.id").entity(String.class).isEqualTo(MASTER_SITE_ID)
            .path("content.site.name").entity(String.class).isEqualTo(MASTER_SITE_NAME)
            .path("content.site.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)
            .path("content.site.repositoryPath").entity(String.class).isEqualTo(MASTER_SITE_REPO_PATH)
            .path("content.site.root.id").entity(Integer.class).isEqualTo(ROOT_CHANNEL_ID)
            .path("content.site.root.segment").entity(String.class).isEqualTo(ROOT_CHANNEL_SEGMENT)
            .path("content.site.derivedSites[0].id").entity(String.class).isEqualTo(DERIVED_SITE_ID)
            .path("content.site.crops").hasValue();
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111112",
          "2b44cbcf-4dca-405f-a274-bae6d9eeadfd",
  })
  void testGetPageById(String idOrUuid) {
    webGraphQlTester.documentName("pageById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.page.id").entity(Integer.class).isEqualTo(ROOT_CHANNEL_ID)
            .path("content.page.uuid").entity(String.class).isEqualTo(ROOT_CHANNEL_UUID)
            .path("content.page.type").entity(String.class).isEqualTo("CMChannel")
            .path("content.page.repositoryPath").entity(String.class).isEqualTo(ROOT_CHANNEL_REPO_PATH)
            .path("content.page.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)
            .path("content.page.title").entity(String.class).isEqualTo("USHomePage")
            .path("content.page.keywords").entity(String.class).isEqualTo("key 1,key2,key3")
            .path("content.page.keywordsList").entityList(String.class).contains("key 1", "key2", "key3")
            .path("content.page.htmlTitle").entity(String.class).isEqualTo("htmlTitle")
            .path("content.page.htmlDescription").entity(String.class).isEqualTo("htmlDescription")

            .path("content.page.localizedVariants[*].id").entityList(Integer.class).contains(ROOT_CHANNEL_ID, DERIVED_ROOT_CHANNEL_ID)
            .path("content.page.localizedVariants[*].uuid").entityList(String.class).contains(ROOT_CHANNEL_UUID, DERIVED_ROOT_CHANNEL_UUID)
            .path("content.page.localizedVariants[*].locale").entityList(String.class).contains(MASTER_SITE_LOCALE, DERIVED_SITE_LOCALE)
            .path("content.page.localizedVariant.id").entity(Integer.class).isEqualTo(DERIVED_ROOT_CHANNEL_ID)
            .path("content.page.localizedVariant.uuid").entity(String.class).isEqualTo(DERIVED_ROOT_CHANNEL_UUID)
            .path("content.page.localizedVariant.locale").entity(String.class).isEqualTo(DERIVED_SITE_LOCALE)
            .path("content.page.localizationRoot.id").entity(Integer.class).isEqualTo(ROOT_CHANNEL_ID)
            .path("content.page.localizationRoot.uuid").entity(String.class).isEqualTo(ROOT_CHANNEL_UUID)
            .path("content.page.localizationRoot.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)

            .path("content.page.children").hasValue()
            .path("content.page.pictures[*].id").entityList(Integer.class).contains(PICTURE_ID)
            .path("content.page.pictures[*].uuid").entityList(String.class).contains(PICTURE_UUID)
            .path("content.page.picture.id").entity(Integer.class).isEqualTo(PICTURE_ID)
            .path("content.page.picture.uuid").entity(String.class).isEqualTo(PICTURE_UUID)
            .path("content.page.picturesPaged.totalCount").entity(Integer.class).isEqualTo(1)
            .path("content.page.picturesPaged.result[*].id").entityList(Integer.class).contains(PICTURE_ID)
            .path("content.page.picturesPaged.result[*].uuid").entityList(String.class).contains(PICTURE_UUID)
            .path("content.page.picturesPaged.result[*].type").entityList(String.class).contains("CMPicture")

            .path("content.page.settings." + SETTINGS_KEY).entity(Integer.class).isEqualTo(SETTINGS_VALUE)
            .path("content.page.settings." + SUB_SETTINGS_KEY).pathDoesNotExist();
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "root-en;111112",
          "root-en/subpage-en;1111112",
          "root-de;11144",
          "root-en/hidden-subpage-en;1111114",
  })
  void testGetPageByPath(String path, String contentId) {
    webGraphQlTester.documentName("pageByPath")
            .variable("path", path)
            .execute()
            .path("content.pageByPath.id").entity(String.class).isEqualTo(contentId);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "subpage-en",
          "non-existing-path",
  })
  void testGetPageByWrongPath(String path) {
    webGraphQlTester.documentName("pageByPath")
            .variable("path", path)
            .execute()
            .path("content.pageByPath").valueIsNull();
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111116",
          "a3eef115-05f6-4d62-a784-37218575ff79",
  })
  void testGetArticleById(String idOrUuid) {
    webGraphQlTester.documentName("articleById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(ARTICLE_ID)
            .path("content.article.uuid").entity(String.class).isEqualTo(ARTICLE_UUID)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)
            .path("content.article.repositoryPath").entity(String.class).isEqualTo(ARTICLE_REPO_PATH)
            .path("content.article.linkedSettings.[0].id").entity(Integer.class).isEqualTo(LINKED_SETTING_ID)
            .path("content.article.mainNavigation").valueIsNull()
            .path("content.article.detailText.text").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.article.detailText.textAsTree").valueIsNull() // element tree
            .path("content.article.detailText.textReferencedContent").hasValue()
            .path("content.article.teaserText.text").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.article.teaserText.textAsTree").valueIsNull() // element tree
            .path("content.article.teaserText.textReferencedContent").hasValue()
            // legacy properties w/o objects 'detailText' or 'teaserText'
            .path("content.article.detailTextLegacy").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.article.detailTextAsTree").valueIsNull()
            .path("content.article.detailTextReferencedContent").hasValue()
            .path("content.article.teaserTextLegacy").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.article.teaserTextAsTree").valueIsNull()
            .path("content.article.teaserTextReferencedContent").hasValue()
            // Note: These assertions effectively test the settings adapter with various input parameters (see query articleById.graphql)
            //       All settings return null as there are not setting in xml repo content!
            .path("content.article.onePath1.p1").valueIsNull()
            .path("content.article.onePath2.p1").valueIsNull()
            .path("content.article.twoPaths1.p1").valueIsNull()
            .path("content.article.twoPaths1.p2").valueIsNull()
            .path("content.article.twoPaths2.p1").valueIsNull()
            .path("content.article.twoPaths2.p2").valueIsNull()
            .path("content.article.tree1.p1.p11").valueIsNull()
            .path("content.article.tree1.p1.p12").valueIsNull()
            .path("content.article.tree2.p1.p11").valueIsNull()
            .path("content.article.tree2.p1.p12.p121").valueIsNull()
            .path("content.article.tree2.p1.p12.p122").valueIsNull();
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111114",
          "6a454873-f57e-4ef4-8a60-0e2083e9d6d5",
  })
  void testGetPictureById(String idOrUuid) {
    webGraphQlTester.documentName("pictureById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.picture.id").entity(Integer.class).isEqualTo(PICTURE_ID)
            .path("content.picture.uuid").entity(String.class).isEqualTo(PICTURE_UUID)
            .path("content.picture.type").entity(String.class).isEqualTo("CMPicture")
            .path("content.picture.name").entity(String.class).isEqualTo("pic16")
            .path("content.picture.locale").entity(String.class).isEqualTo(MASTER_SITE_LOCALE)
            .path("content.picture.repositoryPath").entity(String.class).isEqualTo(MEDIA_REPO_PATH)
            .path("content.picture.caption.text").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.picture.caption.textReferencedContent").entityList(String.class).hasSize(0)
            .path("content.picture.uriTemplate").entity(String.class).matches(uriTemplate -> uriTemplate.startsWith(MEDIA_DELIVERY_PATH + PICTURE_ID + "/data"));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111118",
          "47112af0-97b1-49bd-84e7-80701297422e",
  })
  void testGetVideoById(String idOrUuid) {
    webGraphQlTester.documentName("videoById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.content.id").entity(Integer.class).isEqualTo(VIDEO_ID)
            .path("content.content.uuid").entity(String.class).isEqualTo(VIDEO_UUID)
            .path("content.content.type").entity(String.class).isEqualTo("CMVideo")
            .path("content.content.name").entity(String.class).isEqualTo("vid18")
            .path("content.content.repositoryPath").entity(String.class).isEqualTo(MEDIA_REPO_PATH)
            .path("content.content.fullyQualifiedUrl").entity(String.class).matches(uriTemplate -> uriTemplate.startsWith(MEDIA_DELIVERY_PATH + VIDEO_ID + "/data"));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111120",
          "682cffee-6b67-42f4-b608-b56e07dae22b",
  })
  void testGetDownloadById(String idOrUuid) {
    webGraphQlTester.documentName("cmDownloadById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.content.id").entity(Integer.class).isEqualTo(DOWNLOAD_ID)
            .path("content.content.uuid").entity(String.class).isEqualTo(DOWNLOAD_UUID)
            .path("content.content.type").entity(String.class).isEqualTo("CMDownload")
            .path("content.content.name").entity(String.class).isEqualTo("pdf20")
            .path("content.content.repositoryPath").entity(String.class).isEqualTo(MEDIA_REPO_PATH)
            .path("content.content.fullyQualifiedUrl").entity(String.class).matches(url -> url.startsWith(MEDIA_DELIVERY_PATH + DOWNLOAD_ID + "/data"));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111122",
          "0ebe01ee-341c-4667-88a0-67f740c668a0",
  })
  void testGetHtmlFragmentById(String idOrUuid) {
    webGraphQlTester.documentName("htmlFragmentById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.content.id").entity(Integer.class).isEqualTo(HTML_ID)
            .path("content.content.uuid").entity(String.class).isEqualTo(HTML_UUID)
            .path("content.content.type").entity(String.class).isEqualTo("CMHTML")
            .path("content.content.name").entity(String.class).isEqualTo("html22")
            .path("content.content.repositoryPath").entity(String.class).isEqualTo(MEDIA_REPO_PATH)
            .path("content.content.description").entity(String.class).isEqualTo("html description")
            .path("content.content.html").entity(String.class).matches(html -> html.contains("foo bar"))
            .path("content.content.data.uri").entity(String.class).matches(url -> url.startsWith(MEDIA_DELIVERY_PATH + "markup/" + HTML_ID + "/data"));
  }

  @Test
  void testPageSettingsByWildcard() {
    webGraphQlTester.documentName("pageSettingsByWildcard")
            .variable("id", ROOT_CHANNEL_ID)
            .execute()
            .path("content.page.settings." + SETTINGS_KEY).entity(Integer.class).isEqualTo(SETTINGS_VALUE)
            .path("content.page.settings." + SUB_SETTINGS_KEY).entity(String.class).isEqualTo(SUB_SETTINGS_VALUE);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
          "111128",
          "257e360c-865c-47cd-a3d1-3279640142f5",
  })
  void testGetCMSelectionRulesById(String idOrUuid) {
    webGraphQlTester.documentName("selectionRulesById")
            .variable("id", idOrUuid)
            .execute()
            .path("content.content.id").entity(Integer.class).isEqualTo(SELECTION_RULES_ID)
            .path("content.content.uuid").entity(String.class).isEqualTo(SELECTION_RULES_UUID)
            .path("content.content.type").entity(String.class).isEqualTo("CMSelectionRules")
            .path("content.content.name").entity(String.class).isEqualTo("Personalized Teaser")
            .path("content.content.rules[*].rule").entityList(String.class).hasSize(2)
            .path("content.content.rules[*].target.id").entityList(Integer.class).hasSize(2)
            .path("content.content.rules[*].target.id").entityList(Integer.class).contains(111116)
            .path("content.content.defaultContent").entityList(Object.class);
  }

  @Configuration(proxyBeanMethods = false)
  public static class LocalTestConfiguration {
    @Bean
    BeanResolver pluginSchemaAdapterBeansResolver(BeanFactory beanFactory) {
      return new BeanFactoryResolver(beanFactory);
    }
  }
}
