package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * In {@link Lvl01CapConnectionTest} you saw, how to establish a connection
 * and why {@link DirtiesContext} is important.
 * </p>
 * <p>
 * This test shows access to the default (empty) content repository that comes
 * with {@link XmlRepoConfiguration}. It contains some basic content-types
 * tailored for testing purpose. You will get a rough idea of the available
 * content-types (not meant to be complete) and some first scenarios for
 * creating contents.
 * </p>
 */
@SpringJUnitConfig(XmlRepoConfiguration.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl02ContentRepositoryTest {
  @NonNull
  private final ContentRepository repository;

  Lvl02ContentRepositoryTest(@Autowired @NonNull ContentRepository repository) {
    this.repository = repository;
  }

  /**
   * <p>
   * As stated above, the content repository in its default configuration comes
   * with a set of ready-to use content-types, tailored for testing purpose.
   * </p>
   * <p>
   * To get an overview of these, you may want to consult the documentation
   * of {@link com.coremedia.cap.test.xmlrepo.XmlUapiConfig#SCHEMA_DEFAULT}.
   * </p>
   */
  @Test
  void shouldHaveSomeDefaultContentTypesAvailable() {
    // Just using a sorted list for better error-reporting.
    List<String> actual = repository.getContentTypesByName().keySet()
            .stream()
            .sorted()
            .collect(toList());

    // The following are just some examples of available content types.
    assertThat(actual)
            .contains(
                    "Simple",
                    "SimpleAll",
                    "SimpleBlob",
                    "SimpleDate",
                    "SimpleEmpty",
                    "SimpleImage",
                    "SimpleInteger",
                    "SimpleLink",
                    "SimpleRichtext",
                    "SimpleSite",
                    "SimpleSiteContent",
                    "SimpleString",
                    "SimpleStruct",
                    "SimpleXml"
            );
  }

  /**
   * <p>
   * Shows an example, how to create contents on the fly, for example, as test
   * fixtures.
   * </p>
   * <p><strong>Remarks:</strong></p>
   * <pre>{@code
   * .nameTemplate()
   * }</pre>
   * <p>
   * If reusing the connection between tests and to prevent duplicate
   * name exceptions, it is good practice configuring a name template.
   * Here, we use the wildly used default name template.
   * </p>
   * <pre>{@code
   * .type("SimpleEmpty")
   * }</pre>
   * <p>
   * This type just has no properties. Despite being a good candidate
   * for demonstration here, the type is possibly interesting for
   * testing corner cases if you sketch features for various content
   * types.
   * </p>
   */
  @Test
  void shouldBeAbleToCreateContents() {
    Content content = repository.createContentBuilder()
            .name("Example")
            .nameTemplate()
            .type("SimpleEmpty")
            .checkedIn()
            .create();

    assertThat(content.isCheckedIn())
            .as("Content should have been created and should be checked in.")
            .isTrue();
  }
}
