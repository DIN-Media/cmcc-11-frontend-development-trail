package com.coremedia.blueprint.example.uapi.tests.expert;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.objectserver.web.config.CaeHandlerServicesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * <p>
 * While in {@link ContentAsUuidLinkFormattingTest} we tested a content to be
 * represented by its UUID in the link, we now demonstrate the required handler
 * to eventually resolve these URLs again.
 * </p>
 * <p>
 * We require {@link SpringJUnitWebConfig}, so that we have {@link MockMvc}
 * available, that mocks requests for us and provides ways to assert the
 * response.
 * </p>
 */
@SpringJUnitWebConfig(ContentAsUuidLinkHandingTest.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class ContentAsUuidLinkHandingTest {
  private static final Logger LOG = getLogger(lookup().lookupClass());
  @NonNull
  private final MockMvc mockMvc;
  @NonNull
  private final ContentRepository repository;

  ContentAsUuidLinkHandingTest(@Autowired @NonNull MockMvc mockMvc,
                               @Autowired @NonNull ContentRepository repository) {
    this.mockMvc = mockMvc;
    this.repository = repository;
  }

  /**
   * <p>
   * Simply speaking, we create some content here, and then validate, if we
   * can access the content via URL get request. The most complex part here
   * is using the {@link MockMvc} framework for sending the request and
   * validating its response.
   * </p>
   * <p>
   * Note that we do not use
   * {@link org.springframework.test.web.servlet.result.MockMvcResultMatchers}
   * as they are bound to so-called Hamcrest matchers, while we prefer the
   * AssertJ assertion framework. For details, see
   * <a href="https://github.com/spring-projects/spring-framework/issues/21178">spring-projects/spring-framework#21178</a>.
   * Instead, we use a generic {@link org.springframework.test.web.servlet.ResultMatcher},
   * that uses AssertJ for our assertions.
   * </p>
   */
  @Test
  void shouldResolveContentViaUuidLinkUrl() throws Exception {
    Content content = repository.createContentBuilder()
            .type("SimpleEmpty")
            .name("RepresentedByUuid")
            .nameTemplate()
            .checkedIn()
            .create();

    String link = format("/%s", content.getUuid());

    mockMvc
            .perform(
                    MockMvcRequestBuilders
                            .get(link)
                            .accept("application/json")
            )
            .andExpect(result -> {
              MockHttpServletResponse response = result.getResponse();
              String contentAsString = response.getContentAsString();
              String contentType = response.getContentType();
              int status = response.getStatus();

              LOG.debug("Received response: status={}, contentType={}, content: {}", status, contentType, contentAsString);

              assertSoftly(softly -> {
                softly.assertThat(contentAsString)
                        .contains(
                                content.getUuid().toString(),
                                content.getId(),
                                content.getPath()
                        );
                softly.assertThat(status).isEqualTo(HttpStatus.OK.value());
                softly.assertThat(contentType).isEqualTo(APPLICATION_JSON_VALUE);
              });
            });
  }

  /**
   * Note that we need to instantiate {@link MockMvc} here, while
   * {@link WebApplicationContext} comes with the annotation
   * {@link SpringJUnitWebConfig}.
   */
  @Configuration(proxyBeanMethods = false)
  @Import({
          XmlRepoConfiguration.class,
          CaeHandlerServicesConfiguration.class
  })
  static class LocalConfig {
    @Bean
    @Scope(SCOPE_SINGLETON)
    UuidUrlHandler uuidUrlHandler(@NonNull ContentRepository repository) {
      return new UuidUrlHandler(repository);
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    MockMvc mockMvc(@NonNull WebApplicationContext wac) {
      return webAppContextSetup(wac).build();
    }
  }

  /**
   * Our link handler under test, that matches the UUID within a GET request
   * URL and returns some details of the content found (as JSON).
   */
  @Controller
  public static final class UuidUrlHandler {
    @NonNull
    private final ContentRepository repository;

    public UuidUrlHandler(@NonNull ContentRepository repository) {
      this.repository = repository;
    }

    @GetMapping(path = "/{uuid:^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> handle(@PathVariable("uuid") UUID uuid) {
      Content content = repository.getContent(uuid);
      if (content == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(format("{uuid:\"%s\", id:\"%s\", path:\"%s\"}", uuid, content.getId(), content.getPath()));
    }
  }
}
