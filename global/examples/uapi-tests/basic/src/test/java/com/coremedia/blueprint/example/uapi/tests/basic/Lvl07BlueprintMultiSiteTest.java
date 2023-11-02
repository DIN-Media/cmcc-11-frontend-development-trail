package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.blueprint.base.multisite.BlueprintMultisiteConfiguration;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteModel;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * If you want to test multi-site features, you require having a sites-service
 * at hand. Using the Blueprint content types as shown in
 * {@link Lvl05BlueprintContentTypesTest}, we can now also directly use
 * {@link BlueprintMultisiteConfiguration}, that comes with the configuration
 * for multi-site behavior as we know it from Blueprint.
 * </p>
 * <p>
 * This test also shows one way to set up a really minimal site. This just
 * consists of a site-root folder and a corresponding site-indicator, that
 * again must be located at the configured site-indicator-depth, which we
 * recommend to leave at default 0 (zero) meanwhile.
 * </p>
 */
@SpringJUnitConfig(Lvl07BlueprintMultiSiteTest.LocalConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl07BlueprintMultiSiteTest {
  /**
   * Location of the Blueprint content-types in classpath. Used in
   * {@link Lvl07BlueprintMultiSiteTest.LocalConfig LocalConfig}.
   * Requires dependency to {@code contentserver-blueprint-component}.
   */
  private static final String BLUEPRINT_SCHEMA_LOCATION = "classpath:framework/doctypes/blueprint/blueprint-doctypes.xml";
  private static final Locale ROOT_MASTER_LOCALE = Locale.US;
  private static final String SITE_NAME = "The Site";
  private static final String ROOT_MASTER_ID = "root-master";

  @NonNull
  private final SitesService sitesService;

  Lvl07BlueprintMultiSiteTest(@Autowired @NonNull SitesService sitesService) {
    this.sitesService = sitesService;
  }

  /**
   * A minimal test that just shows: We now have the first site
   * available, which we may use for multi-site testing.
   */
  @Test
  void shouldHaveInitializedRootMasterSite() {
    assertThat(sitesService.findSite(ROOT_MASTER_ID)).isPresent();
  }

  /**
   * <p>
   * We may now also use the sites-service to create derived sites for us.
   * While this test is more like testing core multi-site features, it is
   * meant just to show that you may create derived sites, which then can
   * be used for testing more advanced multi-site setups. Thus, for a suite
   * of tests that require this structure, you may also put this to the
   * initializing bean, where we created our root-master-site.
   * </p>
   * <p>
   * Regarding the derived site settings, we mostly rely on defaults, which may
   * be useful especially in tests. Just to mention some:
   * </p>
   * <dl>
   * <dt>{@code targetSiteId}:</dt>
   * <dd>Using {@code null}, will create a random ID for us. This helps in our
   * tests, as we do not need to take care of possible ID collisions.</dd>
   * <dt>{@code targetSiteManagerGroup}:</dt>
   * <dd>If the site-manager-group is unset, it will default to the admin
   * user.</dd>
   * </dl>
   * <p>
   * <strong>Awaitility - Deal with asynchronous updates:</strong>
   * Even in unit-tests (strictly speaking, we have an integration test here)
   * you may have to deal with asynchronous updates - such as the cache
   * invalidation for available sites. In these cases, you need to apply some
   * wait pattern to make your test robust on the one hand and execute fast
   * on the other hand. A simple {@code Thread.sleep()} may fulfill the first
   * requirement already, but not the second one: In some cases it will just
   * "wait too much". Here come libraries such as Awaitility into play. They
   * allow to wait for some assertion to become valid, while ensuring to end
   * as soon as fulfilled - and to fail on timeout to prevent endless running
   * tests trying to wait for something, that will never happen (due to a
   * bug, for example).
   * </p>
   */
  @SuppressWarnings("ConstantValue")
  @Test
  void shouldBeAbleToDeriveASite() {
    Site rootMasterSite = sitesService.findSite(ROOT_MASTER_ID).orElseThrow();

    String targetSiteId = null;
    String derivedLocale = Locale.FRANCE.toLanguageTag();
    String targetUriSegment = null;
    String targetSiteManagerGroup = null;

    Site derivedSite = sitesService.deriveSite(
            targetSiteId,
            rootMasterSite,
            derivedLocale,
            targetUriSegment,
            targetSiteManagerGroup
    );

    await().atMost(1, TimeUnit.SECONDS)
            .untilAsserted(() -> assertThat(derivedSite)
                    .isInstanceOfSatisfying(Site.class,
                            s -> assertThat(s.getMasterSite())
                                    .isEqualTo(rootMasterSite)
                    ));
  }

  /**
   * Test-local configuration.
   */
  @Configuration(proxyBeanMethods = false)
  @Import({XmlRepoConfiguration.class, BlueprintMultisiteConfiguration.class})
  static class LocalConfig {
    /**
     * As seen in {@link Lvl05BlueprintContentTypesTest}, we use the
     * Blueprint content-types, which are required for using the
     * {@link BlueprintMultisiteConfiguration} as is.
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder()
              .withContentTypes(BLUEPRINT_SCHEMA_LOCATION)
              .build();
    }

    /**
     * <p>
     * Similar to the initial content import in
     * {@link Lvl06InitialContentSetup2Test}, we now use this initializing
     * bean, to create a root-master-site, which subsequently can be used
     * in all tests.
     * </p>
     * <p>
     * As an alternative, you may import such initial structures just as
     * shown in {@link Lvl06InitialContentSetup2Test}.
     * </p>
     */
    @Bean
    @Scope(SCOPE_SINGLETON)
    InitializeRootMasterSite initializeRootMasterSite(@NonNull ContentRepository repository,
                                                      @NonNull SitesService sitesService) {
      return new InitializeRootMasterSite(repository, sitesService);
    }
  }

  /**
   * <p>
   * Initializes a root-master-site. Note that the site is minimal: It just
   * consists of the site-root-folder and the site-indicator. This is enough,
   * so that the system identifies it as a valid site.
   * </p>
   * <p>
   * As an alternative, you may have just created a bean of type
   * {@link com.coremedia.cap.multisite.Site}, that is a result of this
   * initialization. Thus, creating the site and then using the sites-service
   * to find and return the site by ID.
   * </p>
   */
  static class InitializeRootMasterSite implements InitializingBean {
    @NonNull
    private final ContentRepository repository;
    @NonNull
    private final SitesService sitesService;

    InitializeRootMasterSite(@NonNull ContentRepository repository,
                             @NonNull SitesService sitesService) {
      this.repository = repository;
      this.sitesService = sitesService;
    }

    /**
     * Creates some initial site, so that we may start using this site
     * in all tests.
     */
    @Override
    public void afterPropertiesSet() {
      initSite(initSiteRootFolder());
    }

    /**
     * Initializes the site at the given site-root-folder. There is no
     * return value required, as the site exists implicitly, as soon as
     * the site-indicator got created.
     */
    private void initSite(@NonNull Content siteRootFolder) {
      ContentType siteIndicatorType = requireSiteIndicatorDocumentType();

      repository.createContentBuilder()
              .type(siteIndicatorType)
              .parent(respectSiteIndicatorDepth(siteRootFolder))
              .name(format("%s [Site]", SITE_NAME))
              .property("id", ROOT_MASTER_ID)
              .property("name", SITE_NAME)
              .property("locale", ROOT_MASTER_LOCALE.toLanguageTag())
              .checkedIn()
              .create();
    }

    /**
     * While we recommend keeping the site-indicator depth at 0 (zero), we
     * may have a different depth configured here. This method possibly
     * adapts the parent folder for the site-indicator based on the configured
     * site-indicator depth by creating some artificial folders, if required.
     */
    @NonNull
    Content respectSiteIndicatorDepth(@NonNull Content siteRootFolder) {
      int siteIndicatorDepth = requireSiteModel().getSiteIndicatorDepth();

      Content siteIndicatorParentFolder = siteRootFolder;

      for (int i = 0; i < siteIndicatorDepth; i++) {
        siteIndicatorParentFolder = repository.createContentBuilder()
                .folderType()
                .name("siteIndicator")
                .parent(siteIndicatorParentFolder)
                .create();
      }

      return siteIndicatorParentFolder;
    }

    /**
     * We create a site-root-folder that respects the path pattern, as it
     * would also be applied when deriving sites. This is not really necessary,
     * as the path pattern is just for convenience. A site-root folder may be
     * anywhere - it is only made a site-root-folder by its contained
     * site-indicator.
     */
    @NonNull
    private Content initSiteRootFolder() {
      String recommendedRootFolderPath = sitesService.computeDerivedSitePath(SITE_NAME, ROOT_MASTER_LOCALE.toLanguageTag());

      return repository.createContentBuilder()
              .folderType()
              .name(recommendedRootFolderPath)
              .create();
    }

    /**
     * Ensures that the required site-model is available. While often, these
     * robustness checks are skipped within tests in favor of less code, these
     * fail-early approach help to detect issues soon on integration level.
     * So here, we would be aware early if our imported site-model vanished
     * unexpectedly.
     */
    @NonNull
    private SiteModel requireSiteModel() {
      return requireNonNull(
              sitesService.getSiteModel(),
              "Required SiteModel unavailable."
      );
    }

    /**
     * Similar to the above for {@link #requireSiteModel()}, this provides
     * a fail-early reporting, if our test setup breaks in any way. So, the
     * content-types and the site-model we use may be misaligned. Instead of
     * possibly having surprising test results, we favor to be informed early
     * on these changes.
     */
    @NonNull
    private ContentType requireSiteIndicatorDocumentType() {
      String contentTypeName = requireSiteModel().getSiteIndicatorDocumentType();
      return requireNonNull(
              repository.getContentType(contentTypeName),
              () -> format("Required content-type missing: %s", contentTypeName)
      );
    }
  }
}
