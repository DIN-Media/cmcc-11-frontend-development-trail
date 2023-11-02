package com.coremedia.blueprint.headlessserver.contentschema;

import com.coremedia.blueprint.base.caas.web.BlueprintBaseMediaConfig;
import com.coremedia.blueprint.coderesources.ThemeServiceConfiguration;
import com.coremedia.blueprint.headlessserver.CaasConfig;
import com.coremedia.caas.media.TransformationServiceConfiguration;
import com.coremedia.caas.web.controller.graphql.GraphQLController;
import com.coremedia.caas.wrapper.UrlPathFormater;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.dataloader.CacheMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.springframework.test.web.reactive.server.WebTestClient;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.function.Predicate;

import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.ARTICLE_ID;
import static com.coremedia.blueprint.headlessserver.contentschema.TestRepoConstants.PICTURE_ID;
import static java.lang.String.format;

@SpringBootTest(classes = {
        CaasConfig.class,
        BlueprintBaseMediaConfig.class,
        GraphQLController.class,
        TransformationServiceConfiguration.class,
        ThemeServiceConfiguration.class,
        RichtextTransformationGraphQLTest.LocalTestConfiguration.class,
}, properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/content/contentrepository.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml"
})
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@SuppressWarnings("java:S2699") // assertions from spring-graphql are not detected by sonar
class RichtextTransformationGraphQLTest {
  @SuppressWarnings("HttpUrlsUsage")
  private static final String RICH_TEXT_NAMESPACE = "http://www.coremedia.com/2003/richtext-1.0";
  private static final String RICH_TEXT_RESULT_NO_ROOT = "<p>foo bar</p>";
  private static final String RICH_TEXT_RESULT_WITH_ROOT = "<div><p>foo bar</p></div>";

  @MockBean
  UrlPathFormater urlPathFormater;
  @MockBean
  ObjectMapper objectMapper;
  @MockBean
  CacheMap remoteLinkCacheMap;
  private String testName;

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private ContentRepository repository;
  private WebGraphQlTester webGraphQlTester;

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    webGraphQlTester = HttpGraphQlTester.builder(
                    webTestClient
                            .mutate()
                            .baseUrl("/graphql")
            )
            .header("Content-Type", "application/json")
            .build();
    testName = testInfo.getTestMethod().map(Method::getName).orElseThrow();
  }

  @ParameterizedTest(name = "[{index}] Should map <{0} class=\"{1}\"> to <{2}>")
  @CsvSource({
          "span,code,code",
          "span,strike,s",
          "span,underline,u",
  })
  void shouldMapInlineStyles(String originalElementName, String className, String targetElementName) {
    String inlineRichText = MessageFormat.format("<{0} class=\"{1}\">Lorem</{0}>", originalElementName, className);
    String inlineHtml = MessageFormat.format("<{0}>Lorem</{0}>", targetElementName);
    Markup markup = MarkupFactory.fromString(format("<div xmlns=\"%s\"><p>%s</p></div>", RICH_TEXT_NAMESPACE, inlineRichText));

    Content content = repository.createContentBuilder()
            .name(format("%s_%s", testName, className))
            .nameTemplate()
            .type("CMArticle")
            .property("detailText", markup)
            .checkedIn()
            .create();

    int contentId = IdHelper.parseContentId(content.getId());

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", contentId)
            .variable("pid", PICTURE_ID)
            .variable("view", "default")
            .variable("suppressRootTag", true)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(contentId)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(logValueIfFalse(html -> html.contains(inlineHtml)));
  }

  @ParameterizedTest(name = "[{index}] Should map <p class=\"p--heading-{0}\"> to <h{0}>")
  @ValueSource(ints = {1, 2, 3, 4, 5, 6})
  void shouldMapHeadings(int headingLevel) {
    String richTextHeading = format("<p class=\"p--heading-%d\">Lorem</p>", headingLevel);
    String htmlHeading = format("<h%d>Lorem</h%d>", headingLevel, headingLevel);
    Markup markup = MarkupFactory.fromString(format("<div xmlns=\"%s\">%s</div>", RICH_TEXT_NAMESPACE, richTextHeading));

    Content content = repository.createContentBuilder()
            .name(format("%s_h%d", testName, headingLevel))
            .nameTemplate()
            .type("CMArticle")
            .property("detailText", markup)
            .checkedIn()
            .create();

    int contentId = IdHelper.parseContentId(content.getId());

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", contentId)
            .variable("pid", PICTURE_ID)
            .variable("view", "default")
            .variable("suppressRootTag", true)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(contentId)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(logValueIfFalse(html -> html.contains(htmlHeading)));
  }

  @ParameterizedTest(name = "[{index}] Should process table cells (use tbody? {0})")
  @ValueSource(booleans = {true, false})
  void shouldProcessTablesAsIs(boolean withTBody) {
    String richTextTable = withTBody ?
            "<table><tbody><tr><td>C1</td><td>C2</td></tr></tbody></table>" :
            "<table><tr><td>C1</td><td>C2</td></tr></table>";
    Markup markup = MarkupFactory.fromString(format("<div xmlns=\"%s\">%s</div>", RICH_TEXT_NAMESPACE, richTextTable));

    Content content = repository.createContentBuilder()
            .name(testName)
            .nameTemplate()
            .type("CMArticle")
            .property("detailText", markup)
            .checkedIn()
            .create();

    int contentId = IdHelper.parseContentId(content.getId());

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", contentId)
            .variable("pid", PICTURE_ID)
            .variable("view", "default")
            .variable("suppressRootTag", true)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(contentId)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(logValueIfFalse(html -> html.contains(richTextTable)));
  }

  @ParameterizedTest(name = "[{index}] Should process table cells (use tbody? {0})")
  @ValueSource(booleans = {true, false})
  void shouldMapTableHeaderCells(boolean withTBody) {
    String richTextTable = withTBody ?
            "<table><tbody><tr><td class=\"td--header\">Key</td><td>Value</td></tr></tbody></table>" :
            "<table><tr><td class=\"td--header\">Key</td><td>Value</td></tr></table>";
    String htmlTable = withTBody ?
            "<table><tbody><tr><th>Key</th><td>Value</td></tr></tbody></table>" :
            "<table><tr><th>Key</th><td>Value</td></tr></table>";
    Markup markup = MarkupFactory.fromString(format("<div xmlns=\"%s\">%s</div>", RICH_TEXT_NAMESPACE, richTextTable));

    Content content = repository.createContentBuilder()
            .name(testName)
            .nameTemplate()
            .type("CMArticle")
            .property("detailText", markup)
            .checkedIn()
            .create();

    int contentId = IdHelper.parseContentId(content.getId());

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", contentId)
            .variable("pid", PICTURE_ID)
            .variable("view", "default")
            .variable("suppressRootTag", true)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(contentId)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(logValueIfFalse(html -> html.contains(htmlTable)));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testRichtextTransformation(boolean suppressRootTag) {
    String richTextResult = (suppressRootTag) ? RICH_TEXT_RESULT_NO_ROOT : RICH_TEXT_RESULT_WITH_ROOT;

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", ARTICLE_ID)
            .variable("pid", PICTURE_ID)
            .variable("view", "default")
            .variable("suppressRootTag", suppressRootTag)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(ARTICLE_ID)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(html -> html.equals(richTextResult))
            .path("content.article.teaserText.text").entity(String.class).matches(html -> html.equals(richTextResult))
            .path("content.picture.id").entity(Integer.class).isEqualTo(PICTURE_ID)
            .path("content.picture.type").entity(String.class).isEqualTo("CMPicture")
            .path("content.picture.caption.text").entity(String.class).matches(html -> html.equals(richTextResult));
  }

  @Test
  void testRichtextTransformationNoParams() {
    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", ARTICLE_ID)
            .variable("pid", PICTURE_ID)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(ARTICLE_ID)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(html -> html.equals(RICH_TEXT_RESULT_NO_ROOT))
            .path("content.article.teaserText.text").entity(String.class).matches(html -> html.equals(RICH_TEXT_RESULT_NO_ROOT))
            .path("content.picture.id").entity(Integer.class).isEqualTo(PICTURE_ID)
            .path("content.picture.type").entity(String.class).isEqualTo("CMPicture")
            .path("content.picture.caption.text").entity(String.class).matches(html -> html.equals(RICH_TEXT_RESULT_NO_ROOT));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testRichtextTransformationSuppressOnly(boolean suppressRootTag) {
    String richTextResult = (suppressRootTag) ? RICH_TEXT_RESULT_NO_ROOT : RICH_TEXT_RESULT_WITH_ROOT;

    webGraphQlTester.documentName("richtextTransformationByView")
            .variable("aid", ARTICLE_ID)
            .variable("pid", PICTURE_ID)
            .variable("suppressRootTag", suppressRootTag)
            .execute()
            .path("content.article.id").entity(Integer.class).isEqualTo(ARTICLE_ID)
            .path("content.article.type").entity(String.class).isEqualTo("CMArticle")
            .path("content.article.detailText.text").entity(String.class).matches(html -> html.equals(richTextResult))
            .path("content.article.teaserText.text").entity(String.class).matches(html -> html.equals(richTextResult))
            .path("content.picture.id").entity(Integer.class).isEqualTo(PICTURE_ID)
            .path("content.picture.type").entity(String.class).isEqualTo("CMPicture")
            .path("content.picture.caption.text").entity(String.class).matches(html -> html.equals(richTextResult));
  }

  @NonNull
  private static <T> Predicate<T> logValueIfFalse(@NonNull Predicate<T> predicate) {
    return value -> {
      boolean result = predicate.test(value);
      if (!result) {
        System.err.printf("Unexpected value: %s%n", value);
      }
      return result;
    };
  }

  @Configuration(proxyBeanMethods = false)
  public static class LocalTestConfiguration {
    @Bean
    BeanResolver pluginSchemaAdapterBeansResolver(BeanFactory beanFactory) {
      return new BeanFactoryResolver(beanFactory);
    }
  }
}
