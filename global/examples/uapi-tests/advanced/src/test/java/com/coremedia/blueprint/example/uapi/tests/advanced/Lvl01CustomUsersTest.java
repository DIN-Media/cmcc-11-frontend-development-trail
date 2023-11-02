package com.coremedia.blueprint.example.uapi.tests.advanced;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapSession;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.NotAuthorizedException;
import com.coremedia.cap.content.publication.PublicationHelper;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.content.publication.results.PublicationResult;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.cap.user.Group;
import com.coremedia.cap.user.User;
import com.coremedia.cap.user.UserRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.UnknownNullness;
import org.junit.jupiter.api.Nested;
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

import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test demonstrates providing different users with different permissions
 * for testing purpose.
 * </p>
 * <p>
 * While it is possible, creating new users and groups on the fly as can be
 * seen in {@link Lvl01CustomUsersTest.DynamicUserManagement}, it is not
 * (yet) possible modifying rules in access control on the fly. Having this,
 * if your test is about permissions, you are bound to a {@code users.xml}
 * sidecar along with a corresponding {@code rules.xml}, that defines the
 * static rules to apply.
 * </p>
 */
@SpringJUnitConfig(Lvl01CustomUsersTest.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl01CustomUsersTest {
  private static final String TEST_NAME = lookup().lookupClass().getSimpleName();

  /**
   * Users to create by default.
   */
  private static final String CUSTOM_USERS_XML = "classpath:" + TEST_NAME + "/users.xml";
  /**
   * Rules/Permissions that these users shall have.
   */
  private static final String CUSTOM_RULES_XML = "classpath:" + TEST_NAME + "/rules.xml";

  @NonNull
  private final CapConnection connection;
  @NonNull
  private final UserRepository userRepository;
  @NonNull
  private final ContentRepository contentRepository;


  Lvl01CustomUsersTest(@Autowired @NonNull CapConnection connection) {
    this.connection = connection;
    userRepository = requireNonNull(connection.getUserRepository());
    contentRepository = connection.getContentRepository();
  }

  /**
   * Demonstrates that we may access the users provided in the {@code users.xml}
   * file.
   */
  @ParameterizedTest
  @ValueSource(strings = {"editor", "read-only"})
  void shouldBeAbleToAccessUsers(@NonNull String userName) {
    assertThat(userRepository.getUserByName(userName))
            .as("User named %s should exist.", userName)
            .isNotNull();
  }

  /**
   * By default, users do not have any permissions at all. It requires a
   * {@code rules.xml} sidecar, so that these custom users can perform
   * actions on content such as read, write and publish.
   */
  @Nested
  class AccessControlled {
    /**
     * This test demonstrates, how we are able providing a user with full
     * editing permissions (despite supervise permissions). It is also an
     * example for session switching, which is also supported by
     * XML repository. See {@link #getAsUser(String, Supplier)} for
     * reference.
     */
    @Test
    void shouldBeAbleCreatingContentsAsEditor() {
      String userName = "editor";
      Content document = getAsUser(userName, () -> contentRepository.createContentBuilder()
              .type("SimpleEmpty")
              .name("Editor's Document")
              .nameTemplate()
              .checkedIn()
              .create());

      assertThat(document)
              .isNotNull()
              .satisfies(c -> assertThat(c.getCreator())
                      .isEqualTo(userRepository.getUserByName(userName))
              );
    }

    /**
     * Demonstrates a restricted user, that only has read-access.
     */
    @Test
    void shouldNotBeAbleCreatingContentWithLimitedPermissions() {
      assertThatCode(() -> runAsUser("read-only", () -> contentRepository.createContentBuilder()
              .type("SimpleEmpty")
              .name("Probe Document")
              .nameTemplate()
              .create()))
              .isInstanceOfSatisfying(
                      NotAuthorizedException.class,
                      e -> assertThat(e.getErrorName())
                              .isEqualTo("NOT_AUTHORIZED")
              );
    }

    /**
     * <p>
     * We may also publish contents (well, simulate it). Note that there is
     * no master-live-server attached. Thus, we only updated the state of
     * the content here, so that it is marked as published afterward.
     * </p>
     * <p>
     * <strong>Awaitility:</strong> As can be seen in the documentation of
     * {@link PublicationService#getPublisher(Content)} it may reveal an
     * intermediate result, that not necessarily meets our expectations, yet.
     * Instead of risking tests to flake, it is good practice to trigger
     * some active waiting pattern then, which ensures robustness of the test
     * and on the other hand fast execution time, if the expectations is met
     * soon. That is what we use {@code Awaitility} for.
     * </p>
     */
    @Test
    void shouldBeAbleToPublishContentAsEditor() {
      Content document = contentRepository.createContentBuilder()
              .type("SimpleEmpty")
              .name("Editor's Document")
              .nameTemplate()
              .checkedIn()
              .create();

      PublicationHelper publicationHelper = new PublicationHelper(contentRepository);
      PublicationService publicationService = contentRepository.getPublicationService();

      String publisherName = "editor";
      User publishingUser = userRepository.getUserByName(publisherName);

      PublicationResult publicationResult = getAsUser(publisherName, () -> publicationHelper.publish(document));

      assertThat(publicationResult)
              .as("Precondition unmet: The publication should have been successful and contain expected document: %s", document)
              .satisfies(
                      pr -> assertThat(pr.isSuccessful()).isTrue(),
                      pr -> assertThat(pr.getChangedContents()).containsExactly(document)
              );

      assertSoftly(softly -> {
        softly.assertThat(publicationService.isPublished(document))
                .as("Document %s should have been marked as published.", document)
                .isTrue();

        softly.assertThat(publicationService.getPublisher(document))
                .as("By Session Switch, %s (%s) should be stored as publisher of document %s.", publisherName, publishingUser, document)
                .isEqualTo(publishingUser);
      });
    }
  }

  /**
   * As stated above, we can create users and groups on the fly. What is not
   * possible (yet) is creating new rules, so that corresponding groups have
   * access permissions to the repository. Thus, for permissions, at least
   * the groups must be provided in static {@code users.xml} and
   * referenced in {@code rules.xml}, as these are the relevant hooks to
   * manage permissions.
   */
  @Nested
  class DynamicUserManagement {
    @Test
    void shouldBeAbleCreatingGroupsWithMembers() {
      String userName = "john";
      User john = userRepository.createUser(userName, userName);
      Group doe = userRepository.createGroup("doe", false, true, false);
      doe.addMember(john);

      assertThatCode(() -> runAsUser(userName, () -> contentRepository.getRoot().getChildren()))
              .isInstanceOf(NotAuthorizedException.class);
    }
  }

  void runAsUser(@NonNull String userName, @NonNull Runnable runnable) {
    getAsUser(userName, (Supplier<Void>) () -> {
      runnable.run();
      return null;
    });
  }

  /**
   * <p>
   * Demonstrates the session switch as shown in Unified API Developer
   * Manual. This switch also works for the XML repository.
   * </p>
   * <p>
   * <strong>Side Note:</strong> Users created via {@code users.xml} default
   * to the password being the same as the username.
   * </p>
   */
  @UnknownNullness
  <T> T getAsUser(@NonNull String userName, @NonNull Supplier<T> run) {
    CapSession session = connection.login(userName, "", userName);

    CapSession oldSession = session.activate();
    try {
      return run.get();
    } finally {
      oldSession.activate();
    }
  }

  @Configuration(proxyBeanMethods = false)
  @Import(XmlRepoConfiguration.class)
  static class LocalConfig {
    @Bean
    @Scope(SCOPE_SINGLETON)
    XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withUserRepository(CUSTOM_USERS_XML)
              .withRules(CUSTOM_RULES_XML)
              .build();
    }
  }
}
