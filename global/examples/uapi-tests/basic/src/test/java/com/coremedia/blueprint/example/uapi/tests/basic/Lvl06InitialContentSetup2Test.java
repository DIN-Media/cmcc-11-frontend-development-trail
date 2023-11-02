package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.server.importexport.base.importer.ServerImport;
import com.coremedia.cap.server.importexport.base.importer.ServerImportBuilderProvider;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.activation.MimeType;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
 * set of contents to work with within your tests. In contrast to
 * {@link Lvl04CustomContentTypes1Test} the contents are imported via
 * server-import tool, which uses the same input format as known from
 * production scenarios ({@code cm serverimport} tool).
 * </p>
 */
@SpringJUnitConfig(Lvl06InitialContentSetup2Test.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl06InitialContentSetup2Test {
  private static final String TEST_NAME = lookup().lookupClass().getSimpleName();

  private static final URL SERVER_IMPORT_FOLDER_RESOURCE = lookup().lookupClass().getResource("/" + TEST_NAME);

  @NonNull
  private final ContentRepository repository;

  Lvl06InitialContentSetup2Test(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

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
   * Similar to the tests in {@link Lvl06InitialContentSetup1Test}, but not
   * locating contents by ID or UUID: Different to the XML repository
   * content definition, we have less control on the result of the import.
   */
  @Nested
  class LocateContent {
    @Test
    void shouldFindContentByPath() {
      String path = "/Sites/Example/en-US/Child";

      Content actual = repository.getChild(path);

      assertThat(actual)
              .as("Imported content should have been found by path '%s'.", path)
              .isNotNull();
    }
  }

  /**
   * These checks are close to those of {@link Lvl06InitialContentSetup1Test}
   * with one exception about date handling. Thus, we see that just as we
   * could initialize property values in the XML repository content definition,
   * we can do so for server import as well.
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

    /**
     * The expectation in here got adapted slightly due to the default
     * limitation of artifacts created by serverexport, that do not keep
     * timestamps at millisecond precision.
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void shouldHaveParsedDatePropertyFromIsoString() {
      ZonedDateTime expected = ZonedDateTime.parse("2023-06-07T01:23:45+02:00[Europe/Berlin]");
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
   * We cannot create multiple versions directly with server import tool.
   * But we may either create new versions by UAPI – or, as an alternative,
   * just import the same content again, which will create a new version of
   * this content.
   * </p>
   * <p>
   * The use-case in here is slightly artificial, as it just re-imports the
   * same paths again, resulting in unchanged properties – but still creating
   * a new version of the contents.
   * </p>
   *
   * @param serverImport server import bean
   */
  @Test
  void shouldBeAbleToServerImportInTests(@Autowired @NonNull ServerImport serverImport) {
    String path = "/Sites/Example/en-US/Child";
    Content content = repository.getChild(path);
    List<Version> previousVersions = content.getVersions();

    serverImport.perform(SERVER_IMPORT_FOLDER_RESOURCE);

    List<Version> actualVersions = content.getVersions();
    assertThat(actualVersions)
            .hasSizeGreaterThan(previousVersions.size());
  }

  /**
   * Note that we skip initializing {@code XmlUapiConfig} as in other examples.
   * This is because {@link XmlRepoConfiguration} automatically provides this
   * bean with all defaults applied, if we do not create it here.
   */
  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  static class LocalConfig {
    /**
     * Provides a factory for instances of {@link ServerImport}.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    ServerImportBuilderProvider serverImportBuilderProvider(@NonNull CapConnection connection) {
      return new ServerImportBuilderProvider(connection);
    }

    /**
     * A server import instance, which provides API access to the
     * {@code cm serverimport} tool.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    ServerImport serverImport(@NonNull ServerImportBuilderProvider serverImportBuilderProvider) {
      return serverImportBuilderProvider
              .get()
              .haltOnFailure(true)
              .recursive(true)
              .validatingXml(true)
              .build();
    }

    /**
     * This bean provides an option to import the content early and thus
     * providing a similar experience as when using the XML repository
     * where contents are available right when the content repository got
     * created.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    InitializeContent initializeContent(@NonNull ServerImport serverImport) {
      return new InitializeContent(serverImport);
    }
  }

  /**
   * Bean that initially triggers the first server import.
   */
  static class InitializeContent implements InitializingBean {
    @NonNull
    private final ServerImport serverImport;

    InitializeContent(@NonNull ServerImport serverImport) {
      this.serverImport = serverImport;
    }

    @Override
    public void afterPropertiesSet() {
      serverImport.perform(SERVER_IMPORT_FOLDER_RESOURCE);
    }
  }
}
