package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.DefaultConnection;
import com.coremedia.blueprint.base.multisite.SiteResolver;
import com.coremedia.blueprint.cae.handlers.PreviewHandler;
import com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator;
import com.coremedia.blueprint.ecommerce.cae.AbstractCommerceContextInterceptor;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.contract.Contract;
import com.coremedia.livecontext.ecommerce.contract.ContractService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.livecontext.fragment.links.context.Context;
import com.coremedia.livecontext.fragment.links.context.LiveContextContextHelper;
import com.coremedia.livecontext.fragment.links.context.accessors.LiveContextContextAccessor;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;

/**
 * Suitable for URLs whose second segment denotes the store, e.g. /fragment/10001/...
 */
public class FragmentCommerceContextInterceptor extends AbstractCommerceContextInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(FragmentCommerceContextInterceptor.class);
  private static final long MILLISECONDS_PER_MINUTE = 60 * 1000L;

  private LiveContextContextAccessor fragmentContextAccessor;
  private LiveContextSiteResolver liveContextSiteResolver;
  private boolean contractsProcessingEnabled = true;

  // Names of context entries
  private String contextNameMemberGroup = "wc.preview.memberGroups";
  private String contextNamePreviewMode = "wc.p13n_test";
  private String contextNameTimeIsElapsing = "wc.preview.timeiselapsing";
  private String contextNameTimestamp = "wc.preview.timestamp";
  private String contextNamePreviewUserGroup = "wc.preview.usergroup";
  private String contextNameTimezone = "wc.preview.timezone";
  private String contextNameWorkspaceId = "wc.preview.workspaceId";
  private String contextNameUserId = "wc.user.id";
  private String contextNameUserName = "wc.user.loginid";
  private String contextNameContractIds = "wc.preview.contractIds";
  private String contextNameUserGroupIds = "wc.user.membergroupids"; //TODO: mbi

  // --- AbstractCommerceContextInterceptor -------------------------

  @Override
  public boolean preHandle(@Nonnull HttpServletRequest request, HttpServletResponse response, Object handler) {
    setFragmentContext(request);
    return super.preHandle(request, response, handler);
  }

  @Override
  @Nonnull
  protected Optional<CommerceConnection> getCommerceConnectionWithConfiguredStoreContext(
          @Nonnull Site site, @Nonnull HttpServletRequest request) {
    Optional<CommerceConnection> connection = super.getCommerceConnectionWithConfiguredStoreContext(site, request);

    if (connection.isPresent() && isPreview()) {
      Context fragmentContext = LiveContextContextHelper.fetchContext(request);
      if (fragmentContext != null) {
        StoreContext storeContext = connection.get().getStoreContext();
        initStoreContextPreview(fragmentContext, storeContext, connection.get(), request);
      }
    }

    return connection;
  }

  @Override
  protected void initUserContext(@Nonnull CommerceConnection commerceConnection, @Nonnull HttpServletRequest request) {
    super.initUserContext(commerceConnection, request);

    UserContext userContext = commerceConnection.getUserContext();
    Context fragmentContext = LiveContextContextHelper.fetchContext(request);
    if (userContext == null || fragmentContext == null) {
      return;
    }

    initUserContextNameAndId(fragmentContext, userContext);
    if (contractsProcessingEnabled) {
      ContractService contractService = commerceConnection.getContractService();
      StoreContext storeContext = commerceConnection.getStoreContext();
      initUserContextContractsProcessing(fragmentContext, storeContext, userContext, contractService);
    }
  }

  @Override
  @Nullable
  protected Site getSite(@Nonnull HttpServletRequest request, String normalizedPath) {
    FragmentParameters parameters = FragmentContextProvider.getFragmentContext(request).getParameters();
    return liveContextSiteResolver.findSiteFor(parameters);
  }

  @Override
  public SiteResolver getSiteResolver() {
    return liveContextSiteResolver;
  }

  // --- features (expected to be useful) ---------------------------

  protected void setFragmentContext(@Nonnull HttpServletRequest request) {
    // apply the absolute URL flag for fragment requests
    request.setAttribute(ABSOLUTE_URI_KEY, true);
    fragmentContextAccessor.openAccessToContext(request);
  }

  // --- customization hooks (expected to be overridden) ------------

  /**
   * Convert the timestamp to millis.
   * <p>
   * Invoked with the value of the {@link #setContextNameTimestamp(String)}
   * attribute from the fragment context.
   *
   * @return The time represented by the timestamp as millis
   * @throws IllegalArgumentException if the timestamp cannot be converted
   */
  protected long timestampToMillis(@Nonnull String timestamp) {
    return Timestamp.valueOf(timestamp).getTime();
  }

  protected boolean isTimeElapsing(@Nonnull Context fragmentContext) {
    Object value = fragmentContext.get(contextNameTimeIsElapsing);
    return value != null && Boolean.parseBoolean(value.toString());
  }

  // --- configure --------------------------------------------------

  @Required
  public void setFragmentContextAccessor(LiveContextContextAccessor fragmentContextAccessor) {
    this.fragmentContextAccessor = fragmentContextAccessor;
  }

  @Required
  public void setLiveContextSiteResolver(LiveContextSiteResolver liveContextSiteResolver) {
    this.liveContextSiteResolver = liveContextSiteResolver;
  }

  public void setContractsProcessingEnabled(boolean contractsProcessingEnabled) {
    this.contractsProcessingEnabled = contractsProcessingEnabled;
  }

  public void setContextNameMemberGroup(String contextNameMemberGroup) {
    this.contextNameMemberGroup = contextNameMemberGroup;
  }

  public void setContextNamePreviewMode(String contextNamePreviewMode) {
    this.contextNamePreviewMode = contextNamePreviewMode;
  }

  public void setContextNameTimeIsElapsing(String contextNameTimeIsElapsing) {
    this.contextNameTimeIsElapsing = contextNameTimeIsElapsing;
  }

  public void setContextNameTimestamp(String contextNameTimestamp) {
    this.contextNameTimestamp = contextNameTimestamp;
  }

  public void setContextNameTimezone(String contextNameTimezone) {
    this.contextNameTimezone = contextNameTimezone;
  }

  public void setContextNameWorkspaceId(String contextNameWorkspaceId) {
    this.contextNameWorkspaceId = contextNameWorkspaceId;
  }

  public void setContextNameUserId(String contextNameUserId) {
    this.contextNameUserId = contextNameUserId;
  }

  public void setContextNameUserName(String contextNameUserName) {
    this.contextNameUserName = contextNameUserName;
  }

  public void setContextNameContractIds(String contextNameContractIds) {
    this.contextNameContractIds = contextNameContractIds;
  }

  // --- internal ---------------------------------------------------

  private void initStoreContextPreview(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext,
                                       @Nonnull CommerceConnection connection, @Nonnull HttpServletRequest request) {
    initStoreContextUserSegments(fragmentContext, storeContext);
    initStoreContextPreviewMode(fragmentContext, storeContext, connection, request);
    initStoreContextWorkspaceId(fragmentContext, storeContext);
  }

  private void initStoreContextUserSegments(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext) {
    String memberGroups = (String) fragmentContext.get(contextNameMemberGroup);
    if (memberGroups != null) {
      storeContext.setUserSegments(memberGroups);
    }
  }

  private void initStoreContextWorkspaceId(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext) {
    String workspaceId = (String) fragmentContext.get(contextNameWorkspaceId);
    if (workspaceId != null) {
      storeContext.setWorkspaceId(workspaceId);
    }
  }

  private void initStoreContextPreviewMode(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext,
                                           @Nonnull CommerceConnection connection, @Nonnull HttpServletRequest request) {
    boolean previewMode = Boolean.valueOf(fragmentContext.get(contextNamePreviewMode) + "");
    boolean timeIsElapsing = isTimeElapsing(fragmentContext);
    // are we in a studio preview call?
    request.setAttribute(PreviewHandler.REQUEST_ATTR_IS_STUDIO_PREVIEW, timeIsElapsing || previewMode);
    if (!timeIsElapsing) {
      initStoreContextPreviewDate(fragmentContext, storeContext, connection, request);
    }
  }

  private void initStoreContextPreviewDate(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext,
                                           @Nonnull CommerceConnection connection, @Nonnull HttpServletRequest request) {
    Calendar calendar = createPreviewCalendar(fragmentContext, connection);
    if (calendar != null) {
      storeContext.setPreviewDate(convertToPreviewDateRequestParameterFormat(calendar));
      request.setAttribute(ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE, calendar);
    }

    //TODO: mbi
    Object previewUserGroupObj = fragmentContext.get(contextNamePreviewUserGroup);
    if (null != previewUserGroupObj){
      storeContext.setUserSegments((String) previewUserGroupObj);
    }
  }

  private Calendar createPreviewCalendar(@Nonnull Context fragmentContext, @Nonnull CommerceConnection connection) {
    String timestamp = (String) fragmentContext.get(contextNameTimestamp);
    if (timestamp == null) {
      return null;
    }

    //TODO mbi vendor specific krempel
    long timestampMillis;
    if ("IBM".equals(connection.getVendorName())){
      try {
        timestampMillis = timestampToMillis(timestamp);
      } catch (IllegalArgumentException e) {
        LOG.warn("Cannot convert timestamp \"{}\", ignore", timestamp, e);
        return null;
      }
    } else {
      timestampMillis = Long.valueOf(timestamp);
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(roundToMinute(timestampMillis));
    String timezone = (String) fragmentContext.get(contextNameTimezone);
    if (timezone != null) {
      calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    }
    return calendar;
  }

  private void initUserContextNameAndId(@Nonnull Context fragmentContext, @Nonnull UserContext userContext) {
    String userId = (String) fragmentContext.get(contextNameUserId);
    if (userId != null) {
      userContext.setUserId(userId);
    }

    String userName = (String) fragmentContext.get(contextNameUserName);
    if (userName != null) {
      userContext.setUserName(userName);
    }
    //TODO: mbi
    String userGroupIds = (String) fragmentContext.get(contextNameUserGroupIds);
    if (userGroupIds != null){
      StoreContext storeContext = DefaultConnection.get().getStoreContext();
      storeContext.setUserSegments(userGroupIds);
    }
  }

  private void initUserContextContractsProcessing(@Nonnull Context fragmentContext, @Nonnull StoreContext storeContext,
                                                  @Nonnull UserContext userContext,
                                                  @Nullable ContractService contractService) {
    Collection<String> contractIdsFromContext = contractIds(fragmentContext);
    if (contractIdsFromContext.isEmpty()) {
      return;
    }

    // check if user is allowed to execute a call for the passed contracts
    if (contractService != null) {
      Collection<String> contractIdsForUser = contractIds(contractService, storeContext, userContext,
              (String) fragmentContext.get("user.organization.id"));
      Collection intersection = CollectionUtils.intersection(contractIdsForUser, contractIdsFromContext);
      if (!intersection.isEmpty()) {
        storeContext.setContractIds(Arrays.copyOf(intersection.toArray(), intersection.size(), String[].class));
      }
    }
  }

  private Collection<String> contractIds(@Nonnull Context fragmentContext) {
    String contractIdsStr = (String) fragmentContext.get(contextNameContractIds);
    if (contractIdsStr == null || contractIdsStr.isEmpty()) {
      return Collections.emptyList();
    }
    String[] contractIdsFromContext = contractIdsStr.split(" ");
    return Arrays.asList(contractIdsFromContext);
  }

  private Collection<String> contractIds(@Nonnull ContractService contractService,
                                         StoreContext storeContext,
                                         UserContext userContext,
                                         String organizationId) {
    Collection<String> contractIdsForUser = new ArrayList<>();
    Collection<Contract> contractsForUser = contractService.findContractIdsForUser(userContext, storeContext, organizationId);
    if (contractsForUser!=null) {
      for (Contract contract : contractsForUser) {
        contractIdsForUser.add(contract.getExternalTechId());
      }
    }
    return contractIdsForUser;
  }

  @VisibleForTesting
  static String convertToPreviewDateRequestParameterFormat(Calendar calendar) {
    if (calendar == null) {
      return null;
    }

    Format dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm ");
    return dateFormat.format(calendar.getTimeInMillis()) + calendar.getTimeZone().getID();
  }

  private static long roundToMinute(long millis) {
    millis += MILLISECONDS_PER_MINUTE / 2;
    return millis - millis % MILLISECONDS_PER_MINUTE;
  }
}
