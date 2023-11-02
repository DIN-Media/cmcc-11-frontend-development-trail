package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test replaces the default content-types that ship with
 * {@link XmlRepoConfiguration}. It uses the so-called XML-Repository
 * configuration for defining these types. This is an artificial scheme, that
 * can only be used in test contexts. As alternatives to this came late, many
 * tests are used using this schema.
 * </p>
 * <p><strong>LocalConfig:</strong></p>
 * <pre>{@code
 * @SpringJUnitConfig(Lvl03CustomContentTypes1Test.LocalConfig.class)
 * }</pre>
 * <p>
 * The test uses a typical pattern, that can be found in many of the tests
 * used and provided by CoreMedia: the
 * {@link Lvl04CustomContentTypes1Test.LocalConfig LocalConfig}.
 * It is meant to hold a test-specific configuration, which we use here, to
 * provide a set of custom content-types.
 * </p>
 * <p><strong>Alternative:</strong></p>
 * <ul><li>
 * In {@link Lvl04CustomContentTypes2Test} you will see, how to use
 * content-type definition syntax as known in production environments.
 * </li></ul>
 */
@SpringJUnitConfig(Lvl04CustomContentTypes1Test.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl04CustomContentTypes1Test {
  private static final String TEST_NAME = lookup().lookupClass().getSimpleName();

  /**
   * Location of the custom content-types in classpath. Used in
   * {@link Lvl04CustomContentTypes1Test.LocalConfig LocalConfig}.
   */
  private static final String CUSTOM_SCHEMA_LOCATION = "classpath:" + TEST_NAME + "/custom-schema.xml";

  @NonNull
  private final ContentRepository repository;

  Lvl04CustomContentTypes1Test(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

  /**
   * <p>
   * This test shows (and tests) that the custom content types are available.
   * Note, that the default content-types that ship with
   * {@link XmlRepoConfiguration} are not available anymore.
   * </p>
   * <p>
   * Built-in content-types for contents, folders and documents are always
   * available, although not defined explicitly in our custom schema.
   * </p>
   */
  @Test
  void shouldHaveCustomContentTypes() {
    Set<String> actual = repository.getContentTypesByName().keySet();

    assertThat(actual)
            .as("Custom content types should be available, replacing the default ones.")
            .contains(
                    "Custom",
                    "CustomAll",
                    "CustomEmpty"
            )
            .contains(
                    ContentType.CONTENT,
                    ContentType.DOCUMENT,
                    ContentType.FOLDER
            )
            .doesNotContain(
                    "Simple",
                    "SimpleEmpty"
            );
  }

  /**
   * Of course, all the custom content types (despite the abstract one)
   * can be created.
   *
   * @param contentTypeName name provided by {@link ValueSource}
   */
  @ParameterizedTest(name = "[{index}] Content-Type: {0}")
  @ValueSource(strings = {"CustomAll", "CustomEmpty"})
  void shouldBeAbleToCreateContentsOfType(@NonNull String contentTypeName) {
    Content content = repository.createContentBuilder()
            .name("Example")
            .nameTemplate()
            .type(contentTypeName)
            .checkedIn()
            .create();

    assertThat(content.getType().getName())
            .as("Content should have been created with requested content-type.")
            .isEqualTo(contentTypeName);
  }

  /**
   * <p>
   * This is a typical test-local configuration found in many tests used and
   * provided by CoreMedia. It follows the general best-practice to have all
   * relevant artifacts local to one test only. This eases debugging and
   * maintenance (perhaps you know question such as: <em>Is this content-type
   * defined in XML still in use by any test?</em>).
   * </p>
   * <p><strong>Remarks:</strong></p>
   * <pre>{@code
   * @Import(XmlRepoConfiguration.class)
   * }</pre>
   * <p>
   * This is the import, we had in previous tests along with
   * {@link SpringJUnitConfig}. Now, we have an indirection here, to be able
   * to adjust the configuration.
   * </p>
   */
  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  static class LocalConfig {
    /**
     * {@link XmlUapiConfig} provides an API for defining several aspects
     * of the connection and its repositories.
     * <p><strong>Remarks:</strong></p>
     * <pre>{@code
     * .withSchema(CUSTOM_SCHEMA_LOCATION)
     * }</pre>
     * <p>
     * This is the central configuration, that reads our custom content types.
     * User repository as well as empty content structure (the root folder as
     * only existing content) are provided by the default configuration.
     * </p>
     * <p>
     * Without this extra configuration, {@link XmlUapiConfig.Builder#build()}
     * just creates the default configuration known from previous examples like
     * {@link Lvl02ContentRepositoryTest}.
     * </p>
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withSchema(CUSTOM_SCHEMA_LOCATION)
              .build();
    }
  }
}
