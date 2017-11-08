package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommercePropertyHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentCommerceConnection;
import com.coremedia.blueprint.base.livecontext.service.StoreFrontResponse;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.CommerceUrlPropertyProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreFrontService;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.login.CommerceUserIsLoggedInCacheKey;
import com.coremedia.livecontext.ecommerce.ibm.login.WcLoginWrapperService;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.user.UserService;
import com.coremedia.livecontext.ecommerce.user.UserSessionService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

public class UserSessionServiceImpl extends IbmStoreFrontService implements UserSessionService {

  private static final Logger LOG = LoggerFactory.getLogger(UserSessionServiceImpl.class);

  protected static final String LOGON_URL = "Logon?reLogonURL=fail&storeId={storeId}&catalogId={catalogId}&logonId={logonId}&logonPassword={logonPassword}&URL=TopCategoriesDisplay"; // NOSONAR false positive: Credentials should not be hard-coded
  protected static final String LOGOUT_URL = "Logoff?storeId={storeId}&catalogId={catalogId}";
  protected static final String GUEST_LOGIN_URL = "OrderCreate?storeId={storeId}&catalogId={catalogId}&URL=TopCategoriesDisplay";
  protected static final String GUEST_LOGIN_URL_2 = "MiniShopCartDisplayView?storeId={storeId}&catalogId={catalogId}";

  protected static final String STORE_ID_URL_VAR = "storeId";
  protected static final String CATALOG_ID_URL_VAR = "catalogId";
  protected static final String LOGON_ID_URL_VAR = "logonId";
  protected static final String LOGON_PASSWORD_URL_VAR = "logonPassword"; // NOSONAR false positive: Credentials should not be hard-coded

  private String wcsStorefrontUrl;

  private UserService userService;
  private WcLoginWrapperService loginWrapperService;
  private CommerceCache commerceCache;

  // ----- methods that use WCS storefront services -----------------------------

  @Override
  public boolean ensureGuestIdentity(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
    if (isKnownUser(request)) {
      return true;
    }

    try {
      Map<String, String> uriTemplateParameters = ImmutableMap.of(
              STORE_ID_URL_VAR, resolveStoreId(),
              CATALOG_ID_URL_VAR, resolveCatalogId());
      StoreFrontResponse storeFrontResponse = handleStorefrontCall(GUEST_LOGIN_URL, uriTemplateParameters, request, response);

      //apply user id update on the user context, other user context values remain untouched: not relevant
      String newUserId = resolveUserId(storeFrontResponse, resolveStoreId(), true);

      UserContext userContext = UserContextHelper.getCurrentContext();
      String mergedCookies = addCookiesToCookieHeader(userContext.getCookieHeader(), storeFrontResponse.getCookies());

      StoreContext currentContext = StoreContextHelper.getCurrentContext();
      if (null != currentContext && WCS_VERSION_7_7.lessThan(StoreContextHelper.getWcsVersion(currentContext))) {
        //bugfix CMS-4132: call guest login url twice because guest session was broken when called only once
        Map<String, String> cookies = storeFrontResponse.getCookies();
        HttpServletRequest requestWrapper = createRequestWrapper(request, cookies);
        StoreFrontResponse storeFrontResponse2 = handleStorefrontCall(GUEST_LOGIN_URL_2, uriTemplateParameters, requestWrapper, response);
        mergedCookies = addCookiesToCookieHeader(mergedCookies, storeFrontResponse2.getCookies());
      }

      String newCookieHeader = WcCookieHelper.rewritePreviewCookies(mergedCookies);

      updateUserContext(userContext, newUserId, newCookieHeader);

      //return if the guest upgrade was successful.
      return isKnownUser(storeFrontResponse);
    } catch (Exception e) {
      LOG.error("Error executing guest login for user: {}", e.getMessage(), e);
      return false;
    }
  }

  private static HttpServletRequest createRequestWrapper(HttpServletRequest request, Map<String, String> cookies) {
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getHeader(String name) {
        if ("Cookie".equals(name) && !isEmpty(cookies)) {
          StringBuilder sb = new StringBuilder();
          for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            sb.append(cookie.getKey()).append("=").append(cookie.getValue()).append("; ");
          }
          return sb.toString();
        }
        return super.getHeader(name);
      }
    };
  }

  @Override
  public void pingCommerce(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
    try {
      StoreContext currentContext = CurrentCommerceConnection.get().getStoreContext();
      String baseUrl = getWcsStorefrontUrl(currentContext);

      Map<String, Object> params = new HashMap<>();
      params.put(CommerceUrlPropertyProvider.STORE_CONTEXT, currentContext);
      params.put(CommerceUrlPropertyProvider.URL_TEMPLATE, baseUrl);

      UriComponents pingUrl = (UriComponents) getUrlProvider().provideValue(params);
      handleStorefrontCall(pingUrl.toUriString(), Collections.emptyMap(), request, response);
    } catch (GeneralSecurityException e) {
      LOG.warn("Security exception occurred.", e);
    }
  }

  @Override
  @Nullable
  public String resolveUserId(HttpServletRequest request, String currentStoreId, boolean ignoreAnonymous) {
    if (request != null) {
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        return resolveUserIdFromJavaxCookies(Arrays.asList(cookies), currentStoreId, ignoreAnonymous);
      }
    }
    return null;
  }

  @Nullable
  private String resolveUserId(@Nonnull StoreFrontResponse response, String currentStoreId, boolean ignoreAnonymous) {
    return resolveUserIdFromApacheCookies(response.getCookies(), currentStoreId, ignoreAnonymous);
  }

  @Nullable
  private String resolveUserIdFromJavaxCookies(@Nonnull List<Cookie> javaxCookies, String currentStoreId,
                                               boolean ignoreAnonymous) {
    for (Cookie cookie : javaxCookies) {
      String name = cookie.getName();
      String value = cookie.getValue();
      value = decodeCookieValue(value);
      if (value == null) {
        continue;
      }

      String userId = resolveUserIdFromCookieData(name, value, currentStoreId, ignoreAnonymous);
      if (userId != null) {
        return userId;
      }
    }
    return null;
  }

  @Nullable
  private String resolveUserIdFromApacheCookies(@Nonnull Map<String, String> cookies, String currentStoreId,
                                                boolean ignoreAnonymous) {
    for (Map.Entry<String, String> cookie : cookies.entrySet()) {
      String name = cookie.getKey();
      String value = cookie.getValue();
      value = decodeCookieValue(value);
      if (value == null) {
        continue;
      }

      String userId = resolveUserIdFromCookieData(name, value, currentStoreId, ignoreAnonymous);
      if (userId != null) {
        return userId;
      }
    }
    return null;
  }

  @Nullable
  @VisibleForTesting
  String resolveUserIdFromCookieData(@Nonnull String cookieName, @Nonnull String cookieValue, String currentStoreId,
                                     boolean ignoreAnonymous) {
    boolean isWcUserActivityCookie = cookieName.startsWith(IBM_WC_USERACTIVITY_COOKIE_NAME);
    boolean isPreviewWcUserActivityCookie = cookieName.startsWith(IBM_WCP_USERACTIVITY_COOKIE_NAME);
    boolean isNotDeleted = !cookieValue.contains("DEL");
    boolean isUserContainingCookie = (isWcUserActivityCookie || isPreviewWcUserActivityCookie) && isNotDeleted;

    if (isUserContainingCookie) {
      String[] tokens = splitCookie(cookieValue, ignoreAnonymous);
      if (tokens.length > 0 && tokens[1].equals(currentStoreId)) {
        return tokens[0];
      }
    }

    return null;
  }

  @Nonnull
  private static String[] splitCookie(@Nonnull String cookieValue, boolean ignoreAnonymous) {
    String[] values = cookieValue.split(",");
    if (values.length > 1) {
      try {
        int userId = Integer.parseInt(values[0]);
        if (!ignoreAnonymous || userId >= 0) {
          return values;
        }
      } catch (NumberFormatException nfe) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Cannot parse user id from cookie: " + cookieValue, nfe);
        }
      }
    }
    return new String[0];
  }

  @Override
  public boolean loginUser(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, String username,
                           String password) {
    String storeId = resolveStoreId();
    if (storeId == null) {
      return false;
    }

    try {
      Map<String, String> uriTemplateParameters = ImmutableMap.of(
              STORE_ID_URL_VAR, storeId,
              CATALOG_ID_URL_VAR, resolveCatalogId(),
              LOGON_ID_URL_VAR, username,
              LOGON_PASSWORD_URL_VAR, password);

      StoreFrontResponse storeFrontResponse = handleStorefrontCall(LOGON_URL, uriTemplateParameters, request, response);

      if (storeFrontResponse.isSuccess()) {
        refreshUserContext(storeFrontResponse);
      }

      return isKnownUser(storeFrontResponse);
    } catch (GeneralSecurityException e) {
      LOG.warn("Error executing login for user '{}': {}", username, e.getMessage());
      LOG.trace("For debugging purpose...", e);
      return false;
    }
  }

  /**
   * A user is assumed to be logged into the commerce system, if he is a {@link #isKnownUser() known}
   * and {@link #isRegisteredUser() registered} user. Unfortunately there is no single REST call that
   * answers the question if a user is logged in, hence we have to combine both methods.
   */
  @Override
  public boolean isLoggedIn() {
    return isKnownUser() && isRegisteredUser();
  }

  /**
   * Returns true if the current user is a known user, which is a registered user or a guest
   */
  private boolean isKnownUser() {
    UserContext userContext = UserContextHelper.getCurrentContext();

    if (isAnonymousUser(userContext)) {
      return false;
    }

    String userId = userContext.getUserId();

    try {
      CommerceUserIsLoggedInCacheKey cacheKey = new CommerceUserIsLoggedInCacheKey(userId,
              StoreContextHelper.getCurrentContext(), UserContextHelper.getCurrentContext(), loginWrapperService,
              commerceCache);

      return commerceCache.get(cacheKey);
    } catch (Exception e) {
      LOG.debug("error while trying to load the current user context data, assume the user is not logged in", e);
      return false;
    }
  }

  /**
   * Returns whether the current user is a registered user. Be aware that this method returns true
   * whether or not the user is logged in.
   */
  private boolean isRegisteredUser() {
    if (isAnonymousUser(UserContextHelper.getCurrentContext())) {
      return false;
    }

    try {
      User user = userService.findCurrentUser();
      return user != null && isNotBlank(user.getLogonId());
    } catch (Exception e) {
      LOG.error("Unknown error while trying to find a person in commerce. Will return false as answer to isLoggedIn.", e);
      return false;
    }
  }

  @Override
  public boolean logoutUser(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException {
    String storeId = resolveStoreId();

    if (storeId == null) {
      return true;
    }

    Map<String, String> uriTemplateParameters = ImmutableMap.of(STORE_ID_URL_VAR, storeId, CATALOG_ID_URL_VAR, resolveCatalogId());
    StoreFrontResponse storeFrontResponse = handleStorefrontCall(LOGOUT_URL, uriTemplateParameters, request, response);
    return !isKnownUser(storeFrontResponse);
  }

  @Override
  public void clearCommerceSession(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
    String storeId = StoreContextHelper.findCurrentContext().map(StoreContext::getStoreId).orElse(null);

    if (storeId == null) {
      LOG.debug("cannot clear commerce session, no commerce context / storeId available");
      return;
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ((cookie.getName().startsWith(IBM_WC_USERACTIVITY_COOKIE_NAME) || cookie.getName().startsWith(IBM_WCP_USERACTIVITY_COOKIE_NAME)) &&
                cookie.getValue() != null && cookie.getValue().contains("%2c" + storeId)) {
          cookie.setValue(null);
          cookie.setMaxAge(0);
          cookie.setPath("/");
          response.addCookie(cookie);
        }
      }
    }
  }

  private void refreshUserContext(@Nonnull StoreFrontResponse storeFrontResponse) {
    UserContext userContext = UserContextHelper.getCurrentContext();

    String newUserId = resolveUserId(storeFrontResponse, resolveStoreId(), false);
    String newCookieHeader = addCookiesToCookieHeader(userContext.getCookieHeader(), storeFrontResponse.getCookies());

    updateUserContext(userContext, newUserId, newCookieHeader);
  }

  private static void updateUserContext(@Nonnull UserContext userContext, @Nullable String userId,
                                        @Nullable String cookieHeader) {
    UserContext updatedUserContext = UserContext.buildCopyOf(userContext)
            .withUserId(userId)
            .withCookieHeader(cookieHeader)
            .build();

    CurrentCommerceConnection.get().setUserContext(updatedUserContext);
  }

  private static boolean isAnonymousUser(@Nonnull UserContext userContext) {
    return Optional.ofNullable(userContext.getUserId())
            .map(Ints::tryParse)
            .map(userId -> userId < 0)
            .orElse(true);
  }

  /**
   * Add cookies from {@link HttpClientContext} to cookieHeader String.
   *
   * @param cookieHeader cookie header String. Value of "Set-Cookie" header.
   * @param cookies      cookies names and their values
   * @return the augmented cookie header String
   */
  @Nullable
  @VisibleForTesting
  String addCookiesToCookieHeader(@Nullable String cookieHeader, @Nonnull Map<String, String> cookies) {
    if (cookies.isEmpty()) {
      return cookieHeader;
    }

    Joiner cookieJoiner = Joiner.on("; ");

    String joinedCookies = cookieJoiner.withKeyValueSeparator('=').join(cookies);

    if (isNullOrEmpty(cookieHeader)) {
      return joinedCookies;
    } else {
      return cookieJoiner.join(cookieHeader, joinedCookies);
    }
  }

  @Required
  public void setWcsStorefrontUrl(String wcsStorefrontUrl) {
    this.wcsStorefrontUrl = wcsStorefrontUrl;
  }

  private String getWcsStorefrontUrl(StoreContext storeContext) {
    return CommercePropertyHelper.replaceTokens(wcsStorefrontUrl, storeContext);
  }

  @Required
  public void setLoginWrapperService(WcLoginWrapperService loginWrapperService) {
    this.loginWrapperService = loginWrapperService;
  }

  @Required
  public void setCommerceCache(CommerceCache commerceCache) {
    this.commerceCache = commerceCache;
  }

  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @Nullable
  private static String decodeCookieValue(@Nonnull String encodedValue) {
    try {
      return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 is not supported, must not happen, use an approved JVM.";
      LOG.error(msg, e);
      throw new InternalError(msg);
    } catch (IllegalArgumentException iae) {
      LOG.warn("Cookie '{}' can not be URL-decoded.", encodedValue);
      LOG.trace("For debugging purpose...", iae);
      return null;
    }
  }
}
