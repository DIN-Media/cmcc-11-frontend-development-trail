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

import java.util.Set;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test replaces the default content-types that ship with
 * {@link XmlRepoConfiguration}. It uses the standard content-type definition
 * syntax as known for production scenarios.
 * </p>
 * <p><strong>Alternative:</strong></p>
 * <ul><li>
 * In {@link Lvl04CustomContentTypes1Test} the more often used artificial
 * syntax is used for defining content-types.
 * </li></ul>
 */
@SpringJUnitConfig(Lvl04CustomContentTypes2Test.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl04CustomContentTypes2Test {
  private static final String TEST_NAME = lookup().lookupClass().getSimpleName();

  /**
   * Location of the custom content-types in classpath. Used in
   * {@link Lvl04CustomContentTypes2Test.LocalConfig LocalConfig}.
   */
  private static final String CUSTOM_SCHEMA_LOCATION = "classpath:" + TEST_NAME + "/custom-schema.xml";

  @NonNull
  private final ContentRepository repository;

  Lvl04CustomContentTypes2Test(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

  /**
   * <p>
   * This test shows (and tests) that the custom content types are available.
   * Note, that the default content-types that ship with
   * {@link XmlRepoConfiguration} are not available anymore.
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
   * Test-local configuration.
   */
  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  static class LocalConfig {
    /**
     * {@link XmlUapiConfig} provides an API for defining several aspects
     * of the connection and its repositories.
     * <p><strong>Remarks:</strong></p>
     * <pre>{@code
     * .withContentTypes(CUSTOM_SCHEMA_LOCATION)
     * }</pre>
     * <p>
     * In contrast to {@link XmlUapiConfig.Builder#withSchema(String)} this
     * method understands the default content-type definition syntax as known
     * from production scenarios.
     * </p>
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withContentTypes(CUSTOM_SCHEMA_LOCATION)
              .build();
    }
  }
}
