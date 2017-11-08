package com.coremedia.livecontext.ecommerce.ibm.order;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWcWrapperService;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.springframework.http.HttpMethod;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getCurrency;
import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getLocale;
import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.getStoreId;
import static java.util.Collections.singletonList;

/**
 * A service that uses the catalog getRestConnector() to get cart wrappers.
 */
public class WcCartWrapperService extends AbstractWcWrapperService {

  private static final WcRestServiceMethod<WcCart, Void> GET_CART =
          WcRestConnector.createServiceMethod(HttpMethod.GET, "store/{storeId}/cart/@self", false, false, false, true,
                  null, WcCart.class);

  private static final WcRestServiceMethod<Void, WcUpdateCartParam> UPDATE_CART =
          WcRestConnector.createServiceMethod(HttpMethod.PUT, "store/{storeId}/cart/@self", false, false, false, true,
                  WcUpdateCartParam.class, Void.class);

  private static final WcRestServiceMethod<Void, Void> CANCEL_CART =
          WcRestConnector.createServiceMethod(HttpMethod.DELETE, "store/{storeId}/cart/@self", false, false, false,
                  true, null, Void.class);

  private static final WcRestServiceMethod<Void, WcAddToCartParam> ADD_TO_CART =
          WcRestConnector.createServiceMethod(HttpMethod.POST, "store/{storeId}/cart", false, false, false, true,
                  WcAddToCartParam.class, Void.class);

  public WcCart getCart(UserContext userContext, @Nonnull StoreContext storeContext) {
    try {
      Integer userId = UserContextHelper.getForUserId(userContext);

      if (!UserContextHelper.isAnonymousId(userId)) {
        List<String> variableValues = singletonList(getStoreId(storeContext));

        Map<String, String[]> optionalParameters = createParametersMap(
                null,
                getLocale(storeContext),
                getCurrency(storeContext),
                UserContextHelper.getForUserId(userContext),
                UserContextHelper.getForUserName(userContext),
                null,
                storeContext);

        return getRestConnector().callService(GET_CART, variableValues, optionalParameters, null, storeContext,
                userContext);
      }

      return null;
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void updateCart(UserContext userContext, @Nonnull StoreContext storeContext, WcUpdateCartParam updateCartParam) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = createParametersMap(
              null,
              getLocale(storeContext),
              getCurrency(storeContext),
              UserContextHelper.getForUserId(userContext),
              UserContextHelper.getForUserName(userContext),
              null,
              storeContext);

      getRestConnector().callService(UPDATE_CART, variableValues, optionalParameters, updateCartParam, storeContext,
              userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void addToCart(UserContext userContext, @Nonnull StoreContext storeContext, WcAddToCartParam addToCartParam) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = createParametersMap(
              null,
              getLocale(storeContext),
              getCurrency(storeContext),
              UserContextHelper.getForUserId(userContext),
              UserContextHelper.getForUserName(userContext),
              null,
              storeContext);

      getRestConnector().callService(ADD_TO_CART, variableValues, optionalParameters, addToCartParam, storeContext,
              userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }

  public void cancelCart(UserContext userContext, @Nonnull StoreContext storeContext) {
    try {
      List<String> variableValues = singletonList(getStoreId(storeContext));

      Map<String, String[]> optionalParameters = createParametersMap(
              null,
              getLocale(storeContext),
              getCurrency(storeContext),
              UserContextHelper.getForUserId(userContext),
              UserContextHelper.getForUserName(userContext),
              null,
              storeContext);

      getRestConnector().callService(CANCEL_CART, variableValues, optionalParameters, null, storeContext, userContext);
    } catch (CommerceException e) {
      throw e;
    } catch (Exception e) {
      throw new CommerceException(e);
    }
  }
}
