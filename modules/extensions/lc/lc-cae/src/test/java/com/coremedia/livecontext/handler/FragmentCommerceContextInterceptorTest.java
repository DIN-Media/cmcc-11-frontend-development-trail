package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.cap.multisite.Site;
import com.coremedia.ecommerce.test.MockCommerceEnvBuilder;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.FragmentParametersFactory;
import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.ContextBuilder;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.google.common.base.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FragmentContextProvider.class})
public class FragmentCommerceContextInterceptorTest {

  private static final String REQUEST_PATH_INFO = "/anyShop";
  private FragmentCommerceContextInterceptor testling;

  @Mock
  private LiveContextSiteResolver siteLinkHelper;

  @Mock
  private Site site;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Mock
  private BaseCommerceConnection connection;

  @Mock
  private ContractService contractService;
  private MockCommerceEnvBuilder envBuilder;

  @Mock
  private CatalogAliasTranslationService catalogAliasTranslationService;

  FragmentContext fragmentContext;

  @Before
  public void setup() {
    initMocks(this);

    envBuilder = MockCommerceEnvBuilder.create();
    connection = envBuilder.setupEnv();
    connection.setVendorName("IBM");
    connection.getStoreContext().put(StoreContextImpl.SITE, "siteId");
    connection.setContractService(contractService);
    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(connection));

    testling = spy(new FragmentCommerceContextInterceptor());
    testling.setSiteResolver(siteLinkHelper);
    testling.setCommerceConnectionInitializer(commerceConnectionInitializer);
    testling.setCatalogAliasTranslationService(catalogAliasTranslationService);
    runTestlingInPreviewMode(false);

    when(catalogAliasTranslationService.getCatalogAliasForId(any(CatalogId.class), any(String.class))).thenReturn(Optional.of(DEFAULT_CATALOG_ALIAS));

    fragmentContext = new FragmentContext();
    fragmentContext.setFragmentRequest(true);
    String url = "http://localhost:40081/blueprint/servlet/service/fragment/10001/en-US/params;catalogId=catalog";
    FragmentParameters  fragmentParameters  = FragmentParametersFactory.create(url);
    fragmentContext.setParameters(fragmentParameters);

    mockStatic(FragmentContextProvider.class);
    when(FragmentContextProvider.getFragmentContext(any(ServletRequest.class))).thenReturn(fragmentContext);
  }

  private void runTestlingInPreviewMode(boolean previewMode) {
    testling.setPreview(previewMode);
    doReturn(previewMode).when(testling).isStudioPreviewRequest();
  }

  @After
  public void tearDown() throws Exception {
    envBuilder.tearDownEnv();
  }

  @Test
  public void testInitUserContextProvider() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.user.id", "userId");
    fragmentContext.put("wc.user.loginid", "loginId");
    LiveContextContextHelper.setContext(request, fragmentContext);

    testling.initUserContext(connection, request);

    UserContext userContext = CurrentCommerceConnection.get().getUserContext();
    assertThat(userContext.getUserId()).isEqualTo("userId");
    assertThat(userContext.getUserName()).isEqualTo("loginId");
  }

  @Test
  public void testInitStoreContextWithContractIds() {
    runTestlingInPreviewMode(true);

    Collection<Contract> contracts = new ArrayList<>();
    Contract contract1 = mock(Contract.class);
    Contract contract2 = mock(Contract.class);
    when(contract1.getExternalTechId()).thenReturn("contract1");
    when(contract2.getExternalTechId()).thenReturn("contract2");
    contracts.add(contract1);
    contracts.add(contract2);
    when(contractService.findContractIdsForUser(any(UserContext.class), any(StoreContext.class), nullable(String.class)))
            .thenReturn(contracts);

    MockHttpServletRequest request = new MockHttpServletRequest();
    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.user.id", "userId");
    fragmentContext.put("wc.user.loginid", "loginId");
    fragmentContext.put("wc.preview.contractIds", "contract1 contract2");
    LiveContextContextHelper.setContext(request, fragmentContext);

    testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    testling.initUserContext(connection, request);
    String[] contractIdsInStoreContext = CurrentCommerceConnection.get().getStoreContext().getContractIds();
    List storeContextList = Arrays.asList(contractIdsInStoreContext);
    Collections.sort(storeContextList);
    List expected = Arrays.asList("contract1", "contract2");
    Collections.sort(storeContextList);

    assertThat(storeContextList.toArray()).isEqualTo(expected.toArray());
  }

  @Test
  public void testInitStoreContextWithContractIdsButDisabledProcessing() {
    runTestlingInPreviewMode(true);

    testling.setContractsProcessingEnabled(false);
    Collection<Contract> contracts = new ArrayList<>();
    Contract contract1 = mock(Contract.class);
    Contract contract2 = mock(Contract.class);
    when(contract1.getExternalTechId()).thenReturn("contract1");
    when(contract2.getExternalTechId()).thenReturn("contract2");
    contracts.add(contract1);
    contracts.add(contract2);
    when(contractService.findContractIdsForUser(any(UserContext.class), any(StoreContext.class)))
            .thenReturn(contracts);

    MockHttpServletRequest request = new MockHttpServletRequest();
    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.user.id", "userId");
    fragmentContext.put("wc.user.loginid", "loginId");
    fragmentContext.put("wc.preview.contractIds", "contract1 contract2");
    LiveContextContextHelper.setContext(request, fragmentContext);

    testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    testling.initUserContext(connection, request);
    String[] contractIdsInStoreContext = CurrentCommerceConnection.get().getStoreContext().getContractIds();
    assertThat(contractIdsInStoreContext).isNull();
  }

  @Test
  public void testInitStoreContextProviderInPreview() {
    runTestlingInPreviewMode(true);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setPathInfo(REQUEST_PATH_INFO);

    Timestamp ts = Timestamp.valueOf("2014-07-02 17:57:00.000");

    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.preview.memberGroups", "memberGroup1, memberGroup2");
    fragmentContext.put("wc.preview.timestamp", ts.toString());
    fragmentContext.put("wc.preview.timezone", "Europe/Berlin");
    fragmentContext.put("wc.preview.workspaceId", "4711");
    LiveContextContextHelper.setContext(request, fragmentContext);

    Optional<CommerceConnection> commerceConnection = testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    assertThat(commerceConnection).isPresent();

    StoreContext storeContext = commerceConnection.get().getStoreContext();
    assertThat(storeContext).isNotNull();

    assertThat(storeContext.getUserSegments()).isEqualTo("memberGroup1, memberGroup2");
    assertThat(storeContext.getWorkspaceId()).isEqualTo("4711");

    assertThat(storeContext.getPreviewDate()).isEqualTo("02-07-2014 17:57 Europe/Berlin");

    Calendar calendar = parsePreviewDateIntoCalendar(storeContext.getPreviewDate());
    SimpleDateFormat sdb = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    assertThat(storeContext.getPreviewDate()).isEqualTo(sdb.format(calendar.getTime()) + " Europe/Berlin");
    assertThat(request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE)).isEqualTo(calendar);
  }

  @Test
  public void testInitStoreContextProviderWithTimeShift() {
    runTestlingInPreviewMode(true);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setPathInfo(REQUEST_PATH_INFO);

    Timestamp ts = Timestamp.valueOf("2014-07-02 17:57:00.000");

    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.preview.timestamp", ts.toString());
    fragmentContext.put("wc.preview.timezone", "US/Pacific");
    LiveContextContextHelper.setContext(request, fragmentContext);

    testling.getCommerceConnectionWithConfiguredStoreContext(site, request);

    StoreContext storeContext = CurrentCommerceConnection.get().getStoreContext();
    assertThat(storeContext.getPreviewDate()).isEqualTo("02-07-2014 17:57 US/Pacific");

    Calendar calendar = parsePreviewDateIntoCalendar(storeContext.getPreviewDate());
    String requestParam = FragmentCommerceContextInterceptor.convertToPreviewDateRequestParameterFormat(calendar);
    assertThat(storeContext.getPreviewDate()).isEqualTo(requestParam);
    assertThat(request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE)).isEqualTo(calendar);
  }

  @Test
  public void testConvertPreviewDate() {
    runTestlingInPreviewMode(true);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setPathInfo(REQUEST_PATH_INFO);

    Timestamp ts = Timestamp.valueOf("2014-07-02 17:57:00.000");

    Context fragmentContext = ContextBuilder.create().build();
    fragmentContext.put("wc.preview.memberGroups", "memberGroup1, memberGroup2");
    fragmentContext.put("wc.preview.timestamp", ts.toString());
    fragmentContext.put("wc.preview.timezone", "Europe/Berlin");
    fragmentContext.put("wc.preview.workspaceId", "4711");
    LiveContextContextHelper.setContext(request, fragmentContext);

    Optional<CommerceConnection> connection = testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    assertThat(connection).isPresent();

    StoreContext storeContext = connection.get().getStoreContext();
    assertThat(storeContext).isNotNull();

    assertThat(storeContext.getUserSegments()).isEqualTo("memberGroup1, memberGroup2");
    assertThat(storeContext.getWorkspaceId()).isEqualTo("4711");

    SimpleDateFormat sdb = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    assertThat(storeContext.getPreviewDate()).isEqualTo("02-07-2014 17:57 Europe/Berlin");

    Calendar calendar = parsePreviewDateIntoCalendar(storeContext.getPreviewDate());
    assertThat(storeContext.getPreviewDate()).isEqualTo(sdb.format(calendar.getTime()) + " Europe/Berlin");
  }

  @Test
  public void testInitStoreContextProviderInLive() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setPathInfo(REQUEST_PATH_INFO);
    Context fragmentContext = ContextBuilder.create().build();
    Timestamp ts = Timestamp.valueOf("2014-07-02 17:57:00.000");
    fragmentContext.put("wc.preview.memberGroups", "memberGroup1, memberGroup2");
    fragmentContext.put("wc.preview.timestamp", ts.toString());
    fragmentContext.put("wc.preview.workspaceId", "4711");

    Optional<CommerceConnection> connection = testling.getCommerceConnectionWithConfiguredStoreContext(site, request);
    assertThat(connection).isPresent();

    StoreContext storeContext = connection.get().getStoreContext();
    assertThat(storeContext).isNotNull();

    assertThat(storeContext.getUserSegments()).isNull();
    assertThat(storeContext.getPreviewDate()).isNull();
    assertThat(storeContext.getWorkspaceId()).isNull();
    assertThat(request.getAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE)).isNull();
  }

  @Nullable
  private static Calendar parsePreviewDateIntoCalendar(@Nullable String previewDate) {
    if (Strings.isNullOrEmpty(previewDate)) {
      return null;
    }

    Calendar calendar = null;

    try {
      calendar = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      calendar.setTime(sdf.parse(previewDate.substring(0, previewDate.lastIndexOf(' '))));
      calendar.setTimeZone(TimeZone.getTimeZone(previewDate.substring(previewDate.lastIndexOf(' ') + 1)));
    } catch (ParseException ignored) {
      // do nothing
    }

    return calendar;
  }
}
