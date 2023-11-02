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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test replaces the default content-types that ship with
 * {@link XmlRepoConfiguration}. It uses the Blueprint content-type
 * definitions that ship with {@code contentserver-blueprint-component}.
 * </p>
 */
@SpringJUnitConfig(Lvl05BlueprintContentTypesTest.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl05BlueprintContentTypesTest {
  /**
   * Location of the Blueprint content-types in classpath. Used in
   * {@link Lvl05BlueprintContentTypesTest.LocalConfig LocalConfig}.
   * Requires dependency to {@code contentserver-blueprint-component}.
   */
  private static final String BLUEPRINT_SCHEMA_LOCATION = "classpath:framework/doctypes/blueprint/blueprint-doctypes.xml";

  @NonNull
  private final ContentRepository repository;

  Lvl05BlueprintContentTypesTest(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

  /**
   * <p>
   * This test shows (and tests) that the Blueprint content types are available.
   * Note, that the default content-types that ship with
   * {@link XmlRepoConfiguration} are not available anymore.
   * </p>
   */
  @Test
  void shouldHaveBlueprintContentTypes() {
    Set<String> actual = repository.getContentTypesByName().keySet();

    assertThat(actual)
            .as("Blueprint content types should be available, replacing the default ones.")
            .contains(
                    "CMObject",
                    "CMArticle",
                    "CMSite",
                    "CMChannel"
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
   * Of course, all the Blueprint content types (despite the abstract one)
   * can be created.
   *
   * @param contentTypeName name provided by {@link ValueSource}
   */
  @ParameterizedTest(name = "[{index}] Content-Type: {0}")
  @ValueSource(strings = {"CMArticle", "CMChannel"})
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
     * .withContentTypes(BLUEPRINT_SCHEMA_LOCATION)
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
              .withContentTypes(BLUEPRINT_SCHEMA_LOCATION)
              .build();
    }
  }
}
