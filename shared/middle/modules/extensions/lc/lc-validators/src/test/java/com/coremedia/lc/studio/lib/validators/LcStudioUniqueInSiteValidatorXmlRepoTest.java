package com.coremedia.lc.studio.lib.validators;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.rest.cap.validation.CapTypeValidator;
import com.coremedia.rest.cap.validation.impl.ApplicationContextCapTypeValidators;
import com.coremedia.rest.validation.impl.Issue;
import com.coremedia.rest.validation.impl.IssuesImpl;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.coremedia.lc.studio.lib.validators.LcStudioValidatorsXmlRepoTest.code;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        LcStudioUniqueInSiteValidatorXmlRepoTest.LocalConfig.class
})
@TestPropertySource(properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/com/coremedia/lc/studio/lib/validators/lc-studio-lib-test-content.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml",
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LcStudioUniqueInSiteValidatorXmlRepoTest {

  private static final String PROPERTY_NAME = "externalId";

  @Autowired
  private ContentRepository contentRepository;

  @Autowired
  private ApplicationContextCapTypeValidators testling;

  @Test
  public void testDuplicateExternalId() {
    Iterable<Issue> issues1 = validate(16);

    assertIssueCode(issues1, "UniqueInSiteStringValidator");

    Iterable<Issue> issues2 = validate(18);

    assertIssueCode(issues2, "UniqueInSiteStringValidator");
  }

  private Iterable<Issue> validate(int contentId) {
    Content content = contentRepository.getContent(String.valueOf(contentId));
    IssuesImpl issues = new IssuesImpl<>(content, emptySet());

    testling.validate(content, issues);

    //noinspection unchecked
    return (Iterable<Issue>) issues.getByProperty().get(PROPERTY_NAME);
  }

  @Configuration(proxyBeanMethods = false)
  @Import(LcUniqueInSiteValidatorsConfiguration.class)
  public static class LocalConfig {

    @Bean
    ApplicationContextCapTypeValidators validators(List<CapTypeValidator> capTypeValidators) {
      return new ApplicationContextCapTypeValidators(capTypeValidators);
    }

  }

  private void assertIssueCode(Iterable<Issue> issues, String expectedCode) {
    MatcherAssert.assertThat(issues, hasItem(code(expectedCode)));
  }

}
