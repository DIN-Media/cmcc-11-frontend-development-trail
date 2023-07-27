package com.coremedia.blueprint.analytics.elastic.retrieval;

import com.coremedia.blueprint.analytics.elastic.tasks.FetchPageViewHistoryTask;
import com.coremedia.blueprint.analytics.elastic.tasks.FetchReportsTask;
import com.coremedia.blueprint.base.analytics.elastic.PageViewReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.PageViewTaskReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.ReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.common.xml.XmlCapRepositoryConfiguration;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cms.delivery.configuration.DeliveryConfigurationProperties;
import com.coremedia.elastic.core.api.models.Model;
import com.coremedia.elastic.core.api.models.Query;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(EsAlxRetrievalApplicationContextTest.LocalConfig.class)
@TestPropertySource(properties = {
        "elastic.core.persistence=memory",
        "repository.params.contentxml=classpath:/com/coremedia/testing/contenttest.xml",
        "repository.params.userxml=classpath:/com/coremedia/testing/usertest.xml",
        "tenant.default=tenant",
        "mongodb.models.create-indexes=false",
        "elastic.solr.lazyIndexCreation=true"
})
@EnableConfigurationProperties({
        DeliveryConfigurationProperties.class
})
class EsAlxRetrievalApplicationContextTest {

  private static final String SERVICE = "service"; // compare with 12345settings.xml
  static final Object APPLICATION_NAME = "CoreMedia";
  static final Map<String, Object> EFFECTIVE_SETTINGS = Map.of("applicationName", APPLICATION_NAME, "password", "password", "timeRange", 14, "maxLength", 4, "interval", 50000);
  static final List<String> ARTICLES = asList("113110", "1131110", "113152", "206", "886", "1234");
  static final List<String> TOP_4_ARTICLES = asList("contentbean:113110", "contentbean:1131110", "contentbean:113152", "contentbean:206");

  @Inject
  private AnalyticsServiceProvider analyticsServiceProvider;

  @Inject
  private FetchReportsTask fetchReportsTask;

  @Inject
  private TopNReportModelService topNReportModelService;

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private FetchPageViewHistoryTask fetchPageViewHistoryTask;

  @Inject
  private PageViewTaskReportModelService pageViewTaskReportModelService;

  @Inject
  private PageViewReportModelService pageViewReportModelService;

  @Inject
  private SettingsService settingsService;

  @Inject
  private SitesService sitesService;

  private Content pageList;
  private long start;

  @BeforeEach
  public void setup() {
    pageList = contentRepository.getContent("12348"); // compare with contenttest.xml

    when(analyticsServiceProvider.getServiceKey()).thenReturn(SERVICE);
    when(analyticsServiceProvider.computeEffectiveRetrievalSettings(any(), any(Content.class)))
            .then(invocation -> {
              final Object[] args = invocation.getArguments();
              return RetrievalUtil.computeEffectiveRetrievalSettings(SERVICE, EFFECTIVE_SETTINGS, (Content) args[0], (Content) args[1], settingsService, sitesService);
            });

    assertThat(pageList.getType()).isEqualTo(contentRepository.getContentType("CMALXPageList"));
    start = System.currentTimeMillis();
  }

  @AfterEach
  public void teardown() {
    reset(analyticsServiceProvider);

    removeModels(topNReportModelService);
    removeModels(pageViewReportModelService);
    removeModels(pageViewTaskReportModelService);
  }

  private void removeModels(ReportModelService service) {
    for(Model model : service.query().fetch()){
      try {
        model.remove();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  void runFetchReportsTaskWithConfiguration() throws Exception {
    when(analyticsServiceProvider.fetchDataFor(pageList, EFFECTIVE_SETTINGS)).thenReturn(ARTICLES);

    fetchReportsTask.run();
    verify(analyticsServiceProvider).computeEffectiveRetrievalSettings(eq(pageList), any(Content.class));
    verify(analyticsServiceProvider).fetchDataFor(eq(pageList), any(Map.class));

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertThat(reportModel.getReportData()).isEqualTo(TOP_4_ARTICLES);
    recentlySaved(reportModel);
  }

  private void recentlySaved(ReportModel reportModel) {
    final long lastSaved = reportModel.getLastSaved();
    assertThat(lastSaved).as(lastSaved + " should be greater equals than " + start).isGreaterThanOrEqualTo(start);
  }

  @Test
  void runFetchReportsTaskWithException() throws Exception {
    when(analyticsServiceProvider.fetchDataFor(pageList, EFFECTIVE_SETTINGS)).thenThrow(RuntimeException.class);

    fetchReportsTask.run();
    verify(analyticsServiceProvider).computeEffectiveRetrievalSettings(eq(pageList), any(Content.class));
    verify(analyticsServiceProvider).fetchDataFor(eq(pageList), any(Map.class));

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertThat(reportModel.getReportData()).isEmpty();
    recentlySaved(reportModel);
  }

  @Test
  void runFetchReportsTaskForNotConfiguredService() throws Exception {
    final Content pageList = this.pageList.copyTo(this.pageList.getParent(), this.toString());
    pageList.checkOut();
    pageList.set("analyticsProvider", "ignoredService");

    when(analyticsServiceProvider.fetchDataFor(pageList, EFFECTIVE_SETTINGS)).thenReturn(ARTICLES);

    fetchReportsTask.run();
    verify(analyticsServiceProvider, never()).computeEffectiveRetrievalSettings(any(Content.class), same(pageList));
    verify(analyticsServiceProvider, never()).fetchDataFor(same(pageList), anyMap());

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertThat(reportModel.getReportData()).isEmpty();
    assertThat(reportModel.getLastSaved()).isZero();
  }

  @Test
  void runPageViewHistoryTaskWithoutResult() throws Exception {
    fetchPageViewHistoryTask.run();

    verify(analyticsServiceProvider, atLeast(1)).getServiceKey();
    verify(analyticsServiceProvider).fetchPageViews(any(Content.class), anyMap());

    // root model is saved
    final Query<ReportModel> query = pageViewTaskReportModelService.query();
    assertThat(query.count()).isOne();
    recentlySaved(query.get());

    // but we have no page views at all
    assertThat(pageViewReportModelService.query().count()).isZero();
  }

  @Test
  void runPageViewHistoryTaskWithResult() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    String today = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault()).format(new Date());
    data.put("not_a_content_id", Map.of(today, 13L));
    final String articleId = ARTICLES.get(0);
    data.put(articleId, Map.of(today, 5L));
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);

    fetchPageViewHistoryTask.run();

    verify(analyticsServiceProvider, atLeast(1)).getServiceKey();
    verify(analyticsServiceProvider).fetchPageViews(any(Content.class), anyMap());

    // root model is saved
    final Query<ReportModel> query = pageViewTaskReportModelService.query();
    assertThat(query.count()).isOne();
    recentlySaved(query.get());

    // article model is saved
    recentlySaved(pageViewReportModelService.getReportModel(contentRepository.getContent(articleId), SERVICE));
  }

  @Configuration(proxyBeanMethods = false)
  @Import(XmlCapRepositoryConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/blueprint/base/navigation/context/bpbase-default-contextstrategy.xml",
          "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml"
  }, reader = ResourceAwareXmlBeanDefinitionReader.class)
  @EnableAutoConfiguration
  public static class LocalConfig {

    @Bean
    @Scope("singleton")
    public AnalyticsServiceProvider analyticsServiceProvider() {
      return mock(AnalyticsServiceProvider.class);
    }
  }
}
