package com.coremedia.livecontext.ecommerce.ibm.user;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.DefaultConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.user.UserContext;

import static com.coremedia.blueprint.base.livecontext.ecommerce.user.UserContextImpl.newUserContext;

/**
 * Helper class to build an "IBM WCS conform" user context.
 * You do not have to know the exact keys if you use the helper method.
 * Use this class as static import.
 */
public class UserContextHelper {

  public static final String FOR_USER_NAME = "forUser";
  public static final String FOR_USER_ID = "forUserId";


  /**
   * Set the given user context as default in the current request (thread).
   * Read the default context with #getCurrentContext().
   * @param context the default context
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException
   */
  public static void setCurrentContext(UserContext context) throws InvalidContextException {
    CommerceConnection currentConnection = DefaultConnection.get();
    if (currentConnection != null) {
      currentConnection.setUserContext(context);
    }
  }

  /**
   * Gets the default user context within the current request (thread).
   * Set the default context with #setCurrentContext();
   * @return the UserContext
   */
  public static UserContext getCurrentContext() {
    CommerceConnection currentConnection = DefaultConnection.get();
    if (currentConnection != null) {
      UserContext userContext = currentConnection.getUserContext();
      userContext = userContext == null ? newUserContext() :userContext;
      return userContext;
    } else {
      return newUserContext();
    }
  }

  public static UserContext createContext(String forUser, String userId) throws InvalidContextException {
    UserContext context = newUserContext();
    context.put(FOR_USER_ID, userId);
    context.put(FOR_USER_NAME, forUser);
    return context;
  }

  public static String getForUserName(UserContext context) {
    if (context == null) {
      return null;
    }
    Object value = context.get(FOR_USER_NAME);
    if (!(value instanceof String)) {
      return null;
    }
    return (String) value;
  }

  public static Integer getForUserId(UserContext context) {
    if (context == null) {
      return null;
    }
    Object value = context.get(FOR_USER_ID);
    if (!(value instanceof String)) {
      return null;
    }

    try {
      return Integer.parseInt(String.valueOf(value));
    }
    catch (NumberFormatException e) {
      //ignore
    }
    return null;
  }

  public static boolean isAnonymousId(Integer userId) {
    return userId == null || userId < 0;
  }
}
