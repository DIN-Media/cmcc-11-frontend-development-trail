package com.coremedia.blueprint.example.uapi.tests.expert;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.objectserver.beans.AbstractContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.config.ContentBeanServicesConfiguration;
import com.coremedia.objectserver.web.links.CaeLinkServicesConfiguration;
import com.coremedia.objectserver.web.links.Link;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * Demonstrates testing link formatting. You may notice the usage of
 * {@link SpringJUnitWebConfig}: This ensures, that we have request and
 * response objects, we can autowire.
 * </p>
 * <p>
 * For resolving these links and how to test link resolving, see related
 * {@link ContentAsUuidLinkHandingTest}.
 * </p>
 */
@SpringJUnitWebConfig(ContentAsUuidLinkFormattingTest.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class ContentAsUuidLinkFormattingTest {
  public static final String UUID_VIEW = "uuid";
  @NonNull
  private final UuidLinkFormatter uuidLinkFormatter;
  @NonNull
  private final ContentRepository repository;

  ContentAsUuidLinkFormattingTest(@Autowired @NonNull UuidLinkFormatter uuidLinkFormatter,
                                  @Autowired @NonNull ContentRepository repository) {
    this.uuidLinkFormatter = uuidLinkFormatter;
    this.repository = repository;
  }

  @Test
  void shouldFormatUuidAsLink(@Autowired HttpServletRequest request,
                              @Autowired HttpServletResponse response) {
    Content content = repository.createContentBuilder()
            .type("SimpleEmpty")
            .name("LinkedByUuid")
            .nameTemplate()
            .checkedIn()
            .create();
    String result = uuidLinkFormatter.formatLink(content, request, response);
    assertThat(result)
            .as("Content should be represented by its UUID in the URL.")
            .isEqualTo(format("/%s", content.getUuid()));
  }

  /**
   * We import several configurations from CAE, that are required for our link
   * processing here. {@link ContentBeanServicesConfiguration} provides the
   * {@link ContentBeanFactory}, we require for our link-formatting, while
   * {@link CaeLinkServicesConfiguration} provides the required
   * {@link LinkFormatter}.
   */
  @Configuration(proxyBeanMethods = false)
  @Import({
          XmlRepoConfiguration.class,
          CaeLinkServicesConfiguration.class,
          ContentBeanServicesConfiguration.class
  })
  static class LocalConfig {
    @Bean
    @Scope(SCOPE_SINGLETON)
    UuidLinkFormatter linkFormattingUnderTest(@NonNull ContentBeanFactory contentBeanFactory,
                                              @NonNull LinkFormatter linkFormatter) {
      return new UuidLinkFormatter(contentBeanFactory, linkFormatter);
    }

    /**
     * To represent our content as content-bean.
     */
    @Bean(name = "contentBeanFactory:Simple")
    @Scope(SCOPE_PROTOTYPE)
    SimpleContentBean simpleContentBean() {
      return new SimpleContentBean();
    }

    /**
     * This is the link-scheme, that will be accessed via the
     * {@link LinkFormatter}.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    UuidLinkScheme uuidLinkScheme() {
      return new UuidLinkScheme();
    }
  }

  /**
   * Minimal content-bean that just provides direct access to
   * the UUID of the wrapped content.
   */
  static final class SimpleContentBean extends AbstractContentBean {
    @NonNull
    public UUID getUuid() {
      return getContent().getUuid();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
              .add("uuid", getUuid())
              .toString();
    }
  }

  /**
   * Provides the UUID as the URL path.
   */
  @Link
  public static final class UuidLinkScheme {
    @SuppressWarnings("unused")
    @Link(type = SimpleContentBean.class, view = UUID_VIEW)
    public UriComponents buildLink(@NonNull SimpleContentBean contentBean) {
      return UriComponentsBuilder.newInstance()
              .pathSegment(contentBean.getUuid().toString())
              .build();
    }
  }

  /**
   * Our facade that integrates content-bean mapping as well as creating
   * the UUID-link for the given content.
   */
  static final class UuidLinkFormatter {
    @NonNull
    private final ContentBeanFactory contentBeanFactory;
    @NonNull
    private final LinkFormatter linkFormatter;

    UuidLinkFormatter(@NonNull ContentBeanFactory contentBeanFactory,
                      @NonNull LinkFormatter linkFormatter) {
      this.contentBeanFactory = requireNonNull(contentBeanFactory);
      this.linkFormatter = requireNonNull(linkFormatter);
    }

    @Nullable
    public String formatLink(@NonNull Content content,
                             @NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response) {
      if (!content.getType().isSubtypeOf("Simple")) {
        return null;
      }
      SimpleContentBean bean = requireNonNull(
              contentBeanFactory.createBeanFor(content, SimpleContentBean.class),
              () -> format("Content destroyed: %s", content)
      );
      return linkFormatter.formatLink(bean, UUID_VIEW, request, response, false);
    }
  }
}
