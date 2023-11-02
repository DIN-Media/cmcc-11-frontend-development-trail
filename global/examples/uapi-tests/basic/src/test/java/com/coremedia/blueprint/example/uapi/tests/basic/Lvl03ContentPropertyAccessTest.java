package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.BlobService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.create.ContentBuilder;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.dtd.RichtextDtd;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test is two-fold: It shows available properties ready-to-use for testing
 * from default content-type {@code SimpleAll} and provides some ideas how to
 * generate some suitable test-fixtures for property values of various types.
 * </p>
 * <p>
 * Note, that despite {@code SimpleAll} other content-types exist each having
 * just one property of respective type, like {@code SimpleBlob}, that has
 * a property {@code value}, for example.
 * </p>
 */
@SpringJUnitConfig(XmlRepoConfiguration.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl03ContentPropertyAccessTest {
  /**
   * Test Fixture: 1x1 transparent image/png.
   *
   * @see <a href="https://png-pixel.com/">Transparent PNG Pixel Base64 Encoded</a>
   */
  private static final byte[] IMAGE_BLOB_BYTES = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
  private static final String IMAGE_BLOB_MIME_TYPE = "image/png";
  private static final String SOME_RICH_TEXT_STRING = String.format("<div xmlns='%s'><p>Example</p></div>", RichtextDtd.NAMESPACE_URI);
  /**
   * Test Fixture: It is typical, to use {@link MarkupFactory#fromString(String)}
   * within tests for small rich text snippets.
   */
  private static final Markup SOME_RICH_TEXT = MarkupFactory.fromString(SOME_RICH_TEXT_STRING).withGrammar(RichtextDtd.GRAMMAR_NAME);

  @NonNull
  private final CapConnection connection;
  private final ContentRepository repository;
  private final ContentType simpleAll;
  private String testName;

  Lvl03ContentPropertyAccessTest(@Autowired @NonNull CapConnection connection) {
    this.connection = connection;
    repository = connection.getContentRepository();

    simpleAll = requireContentType();
  }

  /**
   * In this test, we use the test's name as "unique pattern" to prevent name
   * collisions in the content repository. This is an alternative to prevent
   * name clashes via {@link ContentBuilder#nameTemplate()}.
   *
   * @param testInfo parameter resolved by JUnit 5
   */
  @BeforeEach
  void setUp(@NonNull TestInfo testInfo) {
    testName = testInfo.getTestMethod().map(Method::getName).orElseThrow();
  }

  @Test
  void shouldBeAbleToSetBlobProperty() throws Exception {
    String propertyName = requireProperty("blob");

    BlobService blobService = connection.getBlobService();
    byte[] plainTextBlobBytes = "test".getBytes(UTF_8);
    Blob expectedValue = blobService.fromBytes(plainTextBlobBytes, "text/plain;charset=\"UTF-8\"");

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    Blob actualValue = content.getBlob(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @SuppressWarnings("UseOfObsoleteDateTimeApi")
  @Test
  void shouldBeAbleToSetDateProperty() {
    String propertyName = requireProperty("date");

    ZonedDateTime now = ZonedDateTime.now();
    Calendar expectedValue = GregorianCalendar.from(now);

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    Calendar actualValue = content.getDate(propertyName);

    assertThat(actualValue).isEqualByComparingTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetImageProperty() throws Exception {
    String propertyName = requireProperty("image");

    BlobService blobService = connection.getBlobService();
    Blob expectedValue = blobService.fromBytes(IMAGE_BLOB_BYTES, IMAGE_BLOB_MIME_TYPE);

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    Blob actualValue = content.getBlob(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetIntegerProperty() {
    String propertyName = requireProperty("int");
    int expectedValue = 42;

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    int actualValue = content.getInt(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetLinkProperty() {
    String propertyName = requireProperty("link");
    Content expectedValue = repository.createContentBuilder()
            .name(testName + "_linkTarget")
            .type(simpleAll)
            .create();

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, List.of(expectedValue))
            .create();

    Content actualValue = content.getLink(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetAnyLinkProperty() {
    String propertyName = requireProperty("anyLink");
    Content expectedValue = repository.createContentBuilder()
            .name(testName + "_linkTarget")
            .folderType()
            .create();

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, List.of(expectedValue))
            .create();

    Content actualValue = content.getLink(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }


  @Test
  void shouldBeAbleToSetRichTextProperty() {
    String propertyName = requireProperty("richtext");

    Markup expectedValue = SOME_RICH_TEXT;

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    Markup actualValue = content.getMarkup(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetStringProperty() {
    String propertyName = requireProperty("string");
    String expectedValue = testName;

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    String actualValue = content.getString(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  void shouldBeAbleToSetStructProperty() {
    String propertyName = requireProperty("struct");

    StructService structService = connection.getStructService();
    Struct expectedValue = structService.emptyStruct();

    Content content = repository.createContentBuilder()
            .name(testName)
            .type(simpleAll)
            .property(propertyName, expectedValue)
            .create();

    Struct actualValue = content.getStruct(propertyName);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  /**
   * <p>
   * Validates that the given required property descriptor exists and returns
   * its name (which should be the same as the provided name).
   * </p>
   * <p>
   * It is good practice validating test pre-requisites explicitly, rather than
   * failing late with possibly less meaningful error messages.
   * </p>
   *
   * @param propertyName the property name to check for existence
   * @return the existing property name
   * @throws java.util.NoSuchElementException if property is missing
   */
  @NonNull
  private String requireProperty(@NonNull String propertyName) {
    CapPropertyDescriptor descriptor = requireNonNull(simpleAll.getDescriptor(propertyName), "Required property missing: " + propertyName);
    // Could also just directly return propertyName.
    return descriptor.getName();
  }

  /**
   * Similar to {@link #requireProperty(String)}, this adds some confidence
   * that the required content-type exists.
   *
   * @return {@code SimpleAll} content-type
   * @throws java.util.NoSuchElementException if content-type does not exist
   */
  @NonNull
  private ContentType requireContentType() {
    ContentType contentType = repository.getContentType("SimpleAll");
    return requireNonNull(contentType, "Required Content-Type is missing: SimpleAll");
  }
}
