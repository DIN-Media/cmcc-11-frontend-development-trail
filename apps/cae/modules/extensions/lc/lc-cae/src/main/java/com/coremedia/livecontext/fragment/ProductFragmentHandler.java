package com.coremedia.livecontext.fragment;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.contentbeans.VirtualEntity;
import com.coremedia.blueprint.common.layout.PageGridPlacement;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.contentbeans.LiveContextExternalProduct;
import com.coremedia.livecontext.contentbeans.ProductDetailPage;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.dataviews.AssumesIdentity;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import com.google.common.base.Splitter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

import static com.coremedia.blueprint.cae.view.DynamicIncludeHelper.createDynamicIncludeRootDelegateModelAndView;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A fragment handler that handles all fragment requests that within the context of a given product. It will fall back
 * to the sites root channel if no context could be found for the given category.
 *
 * The entity parameters should contain {@link CommerceId#getExternalId() external ids}.
 * If there are only {@link CommerceId#getTechId()}  external technical ids} available,
 * the {@link CommerceIdProvider#format(CommerceBeanType, CatalogAlias, String)} needs to handle this.
 */
public class ProductFragmentHandler extends FragmentHandler {

  private static final String AS_ASSETS_VIEW = "asAssets";
  private static final String ORIENTATION_PARAM_NAME = "orientation";
  private static final String TYPES_PARAM_NAME = "types";
  private static final String PDP_PAGE_ID = "pdpPage";

  private ResolveContextStrategy contextStrategy;
  private boolean useContentPagegrid = false;
  private AugmentationService productAugmentationService;

  /**
   * Renders the complete context (which is a CMChannel) of the given <code>product</code> using the given <code>view</code>.
   * If no context can be found for the product, the <code>view</code> of the root channel will be rendered. The site
   * is determined by the tuple <code>(storeId, locale)</code>, which must be unique across all sites.
   *
   * @return the {@link ModelAndView model and view} containing the {@link com.coremedia.blueprint.common.contentbeans.Page page}
   * as <code>self</code> object, that contains the context (CMChannel) that shall be rendered.
   */
  @Nullable
  @Override
  public ModelAndView createModelAndView(@NonNull FragmentParameters params, @NonNull HttpServletRequest request) {
    Site site = SiteHelper.getSiteFromRequest(request);

    if (site == null) {
      throw buildExceptionForMissingNavigation(params);
    }

    String productIdParam = params.getProductId();
    String view = params.getView();
    String placement = params.getPlacement();
    String categoryIdParam = params.getCategoryId();

    LiveContextNavigation navigation = getLiveContextNavigation(params, site, productIdParam, categoryIdParam, request);

    Content rootChannelContent = site.getSiteRootDocument();
    CMChannel rootChannel = getContentBeanFactory().createBeanFor(rootChannelContent, CMChannel.class);
    User developer = UserVariantHelper.getUser(request);

    if (!isNullOrEmpty(placement)) {
      return createFragmentModelAndViewForPlacementAndView(navigation, productIdParam, placement, view, rootChannel,
              developer, request);
    }

    if (view != null && view.equals(AS_ASSETS_VIEW)) {
      String parameter = params.getParameter();

      String orientation = extractParameterValue(parameter, ORIENTATION_PARAM_NAME);
      String types = extractParameterValue(parameter, TYPES_PARAM_NAME);

      return createModelAndViewForProductPage(navigation, productIdParam, view, orientation, types, developer, request);
    }

    return createModelAndViewForProductPage(navigation, productIdParam, view, null, null, developer, request);
  }

  private LiveContextNavigation getLiveContextNavigation(@NonNull FragmentParameters params, Site site, String productId, String categoryId, HttpServletRequest request) {
    StoreContext storeContext = CurrentStoreContext.get(request);
    CommerceConnection connection = storeContext.getConnection();

    CatalogService catalogService = connection.getCatalogService();
    CommerceIdProvider idProvider = connection.getIdProvider();

    CommerceBean commerceBean;
    CatalogAlias catalogAlias = storeContext.getCatalogAlias();
    if (StringUtils.isNotEmpty(categoryId)) {
      commerceBean = catalogService.findCategoryById(idProvider.format(BaseCommerceBeanType.CATEGORY, catalogAlias, categoryId), storeContext);
    } else {
      commerceBean = catalogService.findProductById(idProvider.format(BaseCommerceBeanType.PRODUCT, catalogAlias, productId), storeContext);
    }

    if (commerceBean == null) {
      throw buildExceptionForMissingNavigation(params);
    }

    //noinspection ConstantConditions
    return contextStrategy.resolveContext(site, commerceBean)
            .orElseThrow(() -> buildExceptionForMissingNavigation(params));
  }

  private static IllegalStateException buildExceptionForMissingNavigation(@NonNull FragmentParameters params) {
    String storeId = params.getStoreId();
    Locale locale = params.getLocale();
    String categoryId = params.getCategoryId();

    return new IllegalStateException(String.format(
            "ProductFragmentHandler did not find a navigation for store ID \"%s\", locale \"%s\", category ID \"%s\".",
            storeId, locale, categoryId));
  }

  @NonNull
  protected ModelAndView createFragmentModelAndViewForPlacementAndView(@NonNull Navigation navigation,
                                                                       @NonNull String productId,
                                                                       @NonNull String placement,
                                                                       @Nullable String view,
                                                                       @NonNull CMChannel rootChannel,
                                                                       @Nullable User developer,
                                                                       @NonNull ServletRequest request) {
    Product product = getProductFromId(productId, request);
    Content externalProductContent = getAugmentedProductContent(product);
    if (externalProductContent == null) {
      // product is not augmented we need to wrap the context of the navigation
      Navigation virtualNavigation = navigation;
      if (navigation instanceof AssumesIdentity) {
        virtualNavigation = VirtualEntity.ofBean((AssumesIdentity & Navigation) navigation);
      }
      return createFragmentModelAndViewForPlacementAndView(virtualNavigation,
              placement, view, rootChannel, developer);
    }

    LiveContextExternalProduct externalProduct = getContentBeanFactory().createBeanFor(externalProductContent,
            LiveContextExternalProduct.class);

    PageGridPlacement pageGridPlacement = pageGridPlacementResolver.resolvePageGridPlacement(externalProduct, placement);
    if (pageGridPlacement == null) {
      return createPlacementUnresolvableError(externalProduct, placement);
    }

    Page page = asPage(navigation, externalProduct, developer);
    // We need to wrap the placement into a DynamicInclude object to bypass the loop protection in DynamicIncludeRenderNodeDecoratorProvider.
    ModelAndView resultMV = createDynamicIncludeRootDelegateModelAndView(pageGridPlacement, view);
    RequestAttributeConstants.setPage(resultMV, page);
    NavigationLinkSupport.setNavigation(resultMV, navigation);

    return resultMV;
  }

  @Nullable
  private Content getAugmentedProductContent(@Nullable Product product) {
    if (product == null) {
      return null;
    }

    Content externalProductContent = productAugmentationService.getContent(product);
    if (externalProductContent != null) {
      return externalProductContent;
    }

    if (!product.isVariant()) {
      return null;
    }

    Product parentProduct = ((ProductVariant) product).getParent();

    while (parentProduct instanceof ProductVariant && parentProduct.isVariant()) {
      parentProduct = ((ProductVariant) parentProduct).getParent();
    }

    if (parentProduct == null) {
      return null;
    }

    return productAugmentationService.getContent(parentProduct);
  }

  @NonNull
  protected ModelAndView createModelAndViewForProductPage(Navigation navigation, String productId, String view,
                                                          String orientation, String types, @Nullable User developer,
                                                          @NonNull ServletRequest request) {
    Product product = getProductFromId(productId, request);
    Content augmentedProductContent = getAugmentedProductContent(product);

    ModelAndView modelAndView = HandlerHelper.createModelWithView(product, view);
    if (!isNullOrEmpty(orientation)) {
      modelAndView.addObject("orientation", orientation);
    }
    if (!isNullOrEmpty(types)) {
      modelAndView.addObject("types", types);
    }

    Linkable linkable = augmentedProductContent != null ? getContentBeanFactory().createBeanFor(augmentedProductContent, Linkable.class) : navigation;
    Page page = createProductDetailPage(linkable, navigation, developer);
    addPageModel(modelAndView, page);

    return modelAndView;
  }

  @Nullable
  private Product getProductFromId(@Nullable String productId, ServletRequest request) {
    if (isNullOrEmpty(productId)) {
      return null;
    }

    StoreContext storeContext = CurrentStoreContext.get(request);
    CommerceConnection connection = storeContext.getConnection();

    CommerceIdProvider idProvider = connection.getIdProvider();

    CatalogAlias catalogAlias = storeContext.getCatalogAlias();

    CommerceId commerceId = idProvider.format(BaseCommerceBeanType.PRODUCT, catalogAlias, productId);

    CommerceBeanFactory commerceBeanFactory = connection.getCommerceBeanFactory();
    Product product = (Product) commerceBeanFactory.createBeanFor(commerceId, storeContext);

    if (product.isVariant()) {
      commerceId = idProvider.format(BaseCommerceBeanType.SKU, catalogAlias, productId);

      return (ProductVariant) commerceBeanFactory.createBeanFor(commerceId, storeContext);
    }

    return product;
  }

  @NonNull
  @Override
  protected PageImpl createPageImpl(Object content, Navigation context, @Nullable User developer) {
    return useContentPagegrid
            ? super.createPageImpl(content, context, developer)
            : createProductDetailPage(content, context, developer);
  }

  @NonNull
  private ProductDetailPage createProductDetailPage(Object content, Navigation context, User developer) {
    ProductDetailPage page = getBeanFactory().getBean(PDP_PAGE_ID, ProductDetailPage.class);
    page.setContent(content);
    page.setNavigation(context);
    page.setDeveloper(developer);
    return page;
  }

  @Nullable
  private static String extractParameterValue(@Nullable String parameters, @NonNull String parameterName) {
    if (isNullOrEmpty(parameters)) {
      return parameters;
    }

    Iterable<String> keyValueStrs = Splitter.on(',').split(parameters);
    for (String keyValueStr : keyValueStrs) {
      List<String> keyValue = Splitter.on('=').splitToList(keyValueStr);

      if (keyValue.size() == 2 && parameterName.equals(keyValue.get(0))) {
        return keyValue.get(1);
      }
    }

    return null;
  }

  @Override
  public boolean test(@NonNull FragmentParameters params) {
    return !isNullOrEmpty(params.getProductId())
            && (isNullOrEmpty(params.getExternalRef()) || !params.getExternalRef().startsWith("cm-"));
  }

  public void setContextStrategy(ResolveContextStrategy contextStrategy) {
    this.contextStrategy = contextStrategy;
  }

  /**
   * Determines whether to use the page grid for content pages or for
   * product detail pages.
   * <p>
   * Default is false, which means the pdp pagegrid is used.
   */
  public void setUseContentPagegrid(boolean useContentPagegrid) {
    this.useContentPagegrid = useContentPagegrid;
  }

  @Autowired
  @Qualifier("productAugmentationService")
  public void setProductAugmentationService(AugmentationService augmentationService) {
    this.productAugmentationService = augmentationService;
  }

  @Override
  protected void initialize() {
    super.initialize();
    if (contextStrategy == null) {
      throw new IllegalStateException("Required property not set: contextStrategy");
    }
  }

}
