package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.blueprint.base.ecommerce.catalog.CmsCategory;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.Commerce;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilder;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.ecommerce.test.MockCommerceEnvBuilder;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.rest.invalidations.InvalidationSource;
import com.coremedia.rest.linking.Linker;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {XmlRepoConfiguration.class, RootCategoryInvalidationSourceTest.LocalConfig.class})
public class RootCategoryInvalidationSourceTest {

  @Inject
  private ContentRepository contentRepository;
  @Inject
  private SitesService sitesService;
  @Inject
  private RootCategoryInvalidationSource testling;

  private Site site;
  private ContentType cmCategory;
  private Content rootCategory;
  private Version initialRootDocumentVersion;

  @Before
  public void setup() {
    MockCommerceEnvBuilder.create().setupEnv();
    Commerce.getCurrentConnection().getStoreContext().put(StoreContextBuilder.SITE, "theSiteId");
    cmCategory = contentRepository.getContentType("CMCategory");
    site = sitesService.getSite("theSiteId");
    initialRootDocumentVersion = site.getSiteRootDocument().getCheckedInVersion();
    rootCategory = createAndConfigureRootCategory("rootCategory");
  }

  Content createAndConfigureRootCategory(String name) {
    Content rootCategory = cmCategory.create(site.getSiteRootFolder(), name);
    Content siteRootDocument = site.getSiteRootDocument();
    siteRootDocument.checkOut();
    Struct localSettings = siteRootDocument.getStruct("localSettings").builder()
            .remove("livecontext.rootCategory")
            .declareLink("livecontext.rootCategory", cmCategory, rootCategory).build();
    siteRootDocument.set("localSettings", localSettings);
    siteRootDocument.checkIn();
    return rootCategory;
  }

  @After
  public void teardown() {
    // reset site root document to its initial version
    Content siteRootDocument = site.getSiteRootDocument();
    siteRootDocument.checkOut();
    siteRootDocument.setProperties(initialRootDocumentVersion.getProperties());
    siteRootDocument.checkIn();

    if(!rootCategory.isDestroyed()) {
      rootCategory.destroy();
    }
  }

  @Test
  public void configuredRootCategory() {
    Category rootCategory = getSiteRootCategory();

    assertNotNull(rootCategory);
    assertThat(rootCategory, Matchers.instanceOf(CmsCategory.class));
    CmsCategory cmsCategory = (CmsCategory) rootCategory;
    Content rootCategoryContent = cmsCategory.getContent();
    assertEquals(this.rootCategory, rootCategoryContent);
  }

  Category getSiteRootCategory() {
    Collection<Category> rootCategories = testling.getRootCategories();
    assertFalse(rootCategories.isEmpty());
    return Iterables.getFirst(rootCategories, null);
  }

  @Test
  public void destroyedRootCategory() {
    rootCategory.destroy();
    Collection<Category> rootCategories = testling.getRootCategories();
    assertThat(rootCategories, Matchers.<Category>empty());
  }

  @Test
  public void deletedRootCategory() {
    rootCategory.delete();
    Collection<Category> rootCategories = testling.getRootCategories();
    assertThat(rootCategories, Matchers.<Category>empty());
  }

  @Test
  public void wronglyTypedRootCategory() {
    cmCategory = contentRepository.getContentType("CMArticle");
    createAndConfigureRootCategory(RandomStringUtils.randomAlphanumeric(10));

    Collection<Category> rootCategories = testling.getRootCategories();
    assertThat(rootCategories, Matchers.<Category>empty());
  }

  @Test
  public void configureNewRootCategory() throws InterruptedException {
    assertEquals(rootCategory, ((CmsCategory)getSiteRootCategory()).getContent());

    // reconfiguring the root category should raise invalidations
    Content newRootCategory = createAndConfigureRootCategory(RandomStringUtils.randomAlphanumeric(10));
    InvalidationSource.Invalidations invalidations = testling.getInvalidations(String.valueOf(0));
    assertNotNull(invalidations);  // this one needs @DirtiesContext

    // check contents of invalidations
    Category siteRootCategory = getSiteRootCategory();
    assertEquals(newRootCategory, ((CmsCategory) siteRootCategory).getContent());
    Set<String> uris = invalidations.getInvalidations();
    assertEquals(2, uris.size());
    assertTrue(uris.contains(siteRootCategory.getId()));
  }

  @Configuration
  @ImportResource(value = {
          "classpath:/framework/spring/lc-ecommerce-connection.xml",
          "classpath:/framework/spring/bpbase-ec-cms-commercebeans.xml",
          "classpath:/framework/spring/bpbase-ec-cms-connection.xml"
  },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/catalog/studio/lib/validators/lc-studio-lib-test-content.xml");
    }

    @Bean
    RootCategoryInvalidationSource testling() {
      RootCategoryInvalidationSource rootCategoryInvalidationSource = new RootCategoryInvalidationSource();
      rootCategoryInvalidationSource.setCapacity(3);
      return rootCategoryInvalidationSource;
    }

    @Bean
    Linker linker() {
      Linker linker = mock(Linker.class);
      doAnswer(new Answer() {
        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
          Object entity = invocationOnMock.getArguments()[0];
          if(entity instanceof Category) {
            return new URI(((Category)entity).getId());
          }
          return null;
        }
      }).when(linker).link(any(Category.class));
      return linker;
    }

    @Bean
    static BeanPostProcessor cacheCapacityConfigurer() {
      return new BeanPostProcessor() {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
          return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          if(bean instanceof Cache) {
            ((Cache)bean).setCapacity(Object.class.getName(), 100);
          }
          return bean;
        }
      };
    }

  }

}
