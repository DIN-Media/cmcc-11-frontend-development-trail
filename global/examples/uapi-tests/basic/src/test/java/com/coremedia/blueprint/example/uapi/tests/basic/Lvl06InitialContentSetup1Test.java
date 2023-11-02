package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.server.importexport.base.exporter.ServerExporter;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.activation.MimeType;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This is one of the tests demonstrating how to possibly set up an initial
 * set of contents to work with within your tests.
 * </p>
 * <p>
 * <strong>Preventing Pitfalls:</strong>
 * Dealing with <em>shared</em> content structures within tests may cause
 * troubles when maintaining tests. The general advice is to only use these
 * <em>pre-fabricated</em> content structures rarely and sparsely.
 * </p>
 * <p>
 * Note, that any of these <em>rules</em> are better read as
 * <em>recommendations</em>. There may always be exceptions to these rules,
 * but these should be based on conscious decisions.
 * </p>
 * <dl>
 * <dt><strong>Do not share across test classes:</strong></dt>
 * <dd>
 * <p>
 * Think of a module with 20 test classes and one file denoting the content
 * structure, which again contains hundreds of contents from folders to
 * documents. Some contents are used in several tests, others are only
 * dedicated to one test.
 * </p>
 * <p>
 * At some point, you will lose overview, which contents belong to which tests
 * with results such as orphaned contents, because the corresponding test
 * got deleted meanwhile, or failing tests, because you adjusted the given
 * content for your test to meet your requirements for a given test method
 * you refactor or develop.
 * </p>
 * </dd>
 * <dt><strong>Minimal Pre-Fabrication:</strong></dt>
 * <dd>
 * <p>
 * The XML repository can create contents on the fly, thus, in general, it is
 * best to create relevant contents for your tests just within the test or
 * some {@code setUp} method. Starting with an empty repository is a good
 * advice, if feasible.
 * </p>
 * <p>
 * Exception may exist, for example, for multi-site related tests. Such
 * site-structures are sometimes cumbersome to rebuild again and again from
 * scratch. Recommendation here: Provide a minimal site setup (a site root
 * folder and a site indicator is enough, possibly accompanied by some root
 * document) - and create all other required contents on the fly.
 * </p>
 * </dd>
 * </dl>
 */
@SpringJUnitConfig(Lvl06InitialContentSetup1Test.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl06InitialContentSetup1Test {
  private static final String TEST_NAME = lookup().lookupClass().getSimpleName();

  private static final String INITIAL_CONTENT_LOCATION = "classpath:" + TEST_NAME + "/content.xml";

  @NonNull
  private final ContentRepository repository;

  Lvl06InitialContentSetup1Test(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

  /**
   * <p>
   * Main purpose of this test is demonstrating, that we can access contents
   * that have been initialized via XML Content Repository definition. As noted
   * in class' comment, it is recommended to only use this option sparsely to
   * enhance maintainability of test code. See, for example, that for debugging
   * each of the tests in here, you may require to open not only the test but
   * also its used artifacts in parallel.
   * </p>
   * <p>
   * <strong>No Multi-Site, yet:</strong>
   * As we do not provide a site model or sites service, yet, we have a
   * multi-site alike structure here, but multi-site access is not supported.
   * You will get to know how to set up testing for multi-site scenarios in
   * other tests.
   * </p>
   */
  // TODO[mmi] Ensure, that such a multi-site test exists.
  @Test
  void shouldHaveCreatedSiteRootFolder() {
    Content actual = repository.getChild("/Sites/Example/en-US");
    assertSoftly(softly -> {
      softly.assertThat(actual)
              .as("Path should denote a folder.")
              .extracting(Content::isFolder, as(BOOLEAN))
              .isTrue();

      softly.assertThat(actual)
              .as("The site-root-folder should have children.")
              .extracting(Content::getChildren, as(COLLECTION))
              .isNotEmpty();
    });
  }

  /**
   * Unlike other formats, the XML Content Repository format provides the
   * opportunity to easily design contents having multiple versions. Here,
   * we have a content, which gets a link added in its second version.
   */
  @Test
  void shouldProvideAccessToSeveralVersionsOfDocument() {
    Content actual = repository.getChild("/Sites/Example/en-US/Root");
    assertThat(actual.getVersions())
            .as("Content should have two versions, one with empty and one with non-empty link property.")
            .hasSize(2)
            .anySatisfy(v -> assertThat(v.getLinks("link")).isEmpty())
            .anySatisfy(v -> assertThat(v.getLinks("link")).isNotEmpty());
  }

  /**
   * Just as the in-production repository, the XML repository supports various
   * lookup methods for content items.
   */
  @Nested
  class LocateContent {
    @Test
    void shouldFindContentByPath() {
      String id = IdHelper.formatContentId(13576);
      String path = "/Sites/Example/en-US/Child";

      Content expected = repository.getContent(id);
      Content actual = repository.getChild(path);

      assertThat(actual)
              .as("Content with ID %s should have been found by path '%s'.", id, path)
              .isNotNull()
              .isEqualTo(expected);
    }

    @Test
    void shouldFindContentByUuid() {
      String id = IdHelper.formatContentId(13576);
      UUID uuid = UUID.fromString("6c5e6560-9b92-48df-83c4-e604613782c0");

      Content expected = repository.getContent(id);
      Content actual = repository.getContent(uuid);

      assertThat(actual)
              .as("Content with ID %s should have been found by UUID %s.", id, uuid)
              .isNotNull()
              .isEqualTo(expected);
    }

    @Test
    void shouldFindContentById() {
      String path = "/Sites/Example/en-US/Child";
      String id = IdHelper.formatContentId(13576);

      Content expected = repository.getChild(path);
      Content actual = repository.getContent(id);

      assertThat(actual)
              .as("Content at path %s by ID '%s'.", path, id)
              .isNotNull()
              .isEqualTo(expected);
    }
  }

  /**
   * Properties in XML repository may have initial values set. While mostly
   * straightforward for simple properties such as strings and integers, this
   * test, and especially the XML sidecar, shows some ways to initialize
   * blob, date and markup properties.
   */
  @Nested
  class PropertyInitialization {
    private Content actualContent;

    @BeforeEach
    void setUp() {
      actualContent = repository.getChild("/Sites/Example/en-US/Child");
    }

    @Test
    void shouldHaveInitializedBlobPropertyFromBase64String() {
      Blob blob = actualContent.getBlob("blob");

      assertThat(blob).satisfies(b -> assertSoftly(softly -> {
        softly.assertThat(blob.getContentType())
                .extracting(MimeType::toString, as(STRING))
                .isEqualTo("image/png");
        softly.assertThat(blob.getSize())
                .isGreaterThan(0);
      }));
    }

    @Test
    void shouldHaveInitializedImagePropertyFromSidecar() {
      Blob blob = actualContent.getBlob("image");

      assertThat(blob).satisfies(b -> assertSoftly(softly -> {
        softly.assertThat(blob.getContentType())
                .extracting(MimeType::toString, as(STRING))
                .isEqualTo("image/png");
        softly.assertThat(blob.getSize())
                .isGreaterThan(0);
      }));
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void shouldHaveParsedDatePropertyFromIsoString() {
      ZonedDateTime expected = ZonedDateTime.parse("2023-06-07T01:23:45.123+02:00[Europe/Berlin]");
      Calendar date = actualContent.getDate("date");

      assertThat(date)
              .isInstanceOfSatisfying(
                      GregorianCalendar.class,
                      gc -> assertThat(gc.toZonedDateTime()).isEqualTo(expected)
              );
    }

    @Test
    void shouldHaveInitializedLinkProperty() {
      List<Content> expected = List.of(actualContent, actualContent);

      assertThat(actualContent.getLinks("link"))
              .isEqualTo(expected);
    }

    @Test
    void shouldHaveInitializedRichTextPropertyFromSidecar() {
      assertThat(actualContent.getMarkup("richtext"))
              // As the property value may be `null`, isInstanceOfSatisfying
              // helps to grant the required type, while providing a better
              // error message on failure.
              .isInstanceOfSatisfying(Markup.class, m -> assertSoftly(softly -> {
                softly.assertThat(m.getGrammar()).isEqualTo("coremedia-richtext-1.0");
                softly.assertThat(m.asXml()).contains("<p>Example</p>");
              }));
    }

    @Test
    void shouldHaveInitializedStructPropertyFromSidecar() {
      assertThat(actualContent.getStruct("struct"))
              .isInstanceOfSatisfying(
                      Struct.class,
                      struct -> assertThat(struct.getString("lorem"))
                              .isEqualTo("ipsum")
              );
    }
  }

  /**
   * <p>
   * While not necessarily a beginner level use-case, the test allows us to
   * align the two related tests for content initialization. Thus, we may
   * copy the exported data here for initialization of the other test.
   * </p>
   * <p>
   * To have access to the created artifacts after test, ensure not to use
   * the path denoted by {@code TempDir}, as this is automatically removed
   * after the test exists.
   * </p>
   */
  @Test
  void shouldBeAbleToExportContents(@TempDir Path outputPath,
                                    @Autowired @NonNull ServerExporter serverExporter) {
    // TODO[mmi] Having exported artifacts, write comparable test using serverimport.
    serverExporter
            .setRecursive(true)
            .setPrettyPrint(true)
            .setBaseDir(outputPath.toFile())
            .setContentIds(repository.getRoot().getId())
            .doExport();

    assertThat(outputPath)
            // The exported root document.
            .isDirectoryRecursivelyContaining(childPath -> "Root.xml".equals(childPath.getFileName().toString()))
            // The exported child document.
            .isDirectoryRecursivelyContaining(childPath -> "Child.xml".equals(childPath.getFileName().toString()))
            // The exported sidecar of the blob property value.
            .isDirectoryRecursivelyContaining(childPath -> "Child.blob.png".equals(childPath.getFileName().toString()))
            // The exported sidecar of the image property value.
            .isDirectoryRecursivelyContaining(childPath -> "Child.image.png".equals(childPath.getFileName().toString()))
    ;
  }

  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  static class LocalConfig {
    /**
     * We rely on the default schema from {@code schema-default.xml} here,
     * which, for example, provides content types {@code SimpleSite} and
     * {@code SimpleSiteContent}. Having this, we may reduce content repository
     * initialization to only the contents itself.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withContent(INITIAL_CONTENT_LOCATION)
              .build();
    }

    /**
     * For test maintenance purpose, we add an exporter here, which provides
     * the API for the command-line tool {@code cm serverexport}.
     *
     * @param connection connection to XML repository
     * @return facade to the server export utility
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    ServerExporter serverExporter(@NonNull CapConnection connection) {
      // Not having the sites-service at hand is irrelevant if
      // not testing multi-site aspects of the CMS.
      return new ServerExporter(connection, null);
    }
  }
}
