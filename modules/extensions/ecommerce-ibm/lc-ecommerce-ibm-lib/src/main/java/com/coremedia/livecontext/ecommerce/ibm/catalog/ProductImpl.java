package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.ProductAttribute;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.catalog.VariantFilter;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.inventory.AvailabilityInfo;
import com.coremedia.livecontext.ecommerce.inventory.AvailabilityService;
import com.coremedia.livecontext.ecommerce.user.UserContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType.PRODUCT;
import static com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider.commerceId;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ProductImpl extends ProductBase {

  private List<String> variantAxis;
  private List<ProductVariant> variants;

  private AvailabilityService availabilityService;


  @Override
  @SuppressWarnings("unchecked")
  protected Map<String, Object> getDelegate() {
    if (delegate == null) {
      delegate = getDelegateFromCache();
      if (delegate == null) {
        throw new NotFoundException(getId() + " (product not found in catalog)");
      }
    }
    return delegate;
  }

  /**
   * Perform by-id-call to get detail data
   * @return detail data map
   */
  Map<String, Object> getDelegateFromCache() {
    return getCommerceCache().get(
            new ProductCacheKey(getId(), getContext(), UserContextHelper.getCurrentContext(), getCatalogWrapperService(), getCommerceCache()));
  }

  @Override
  public void load() {
    getDelegate();
  }

  @Override
  @Nonnull
  public List<String> getVariantAxisNames() {
    if (variantAxis == null) {
      List<String> newVariantAxis = new ArrayList<>();
      List<ProductAttribute> definingAttributes = getDefiningAttributes();
      for (ProductAttribute definingAttribute : definingAttributes) {
        if (!newVariantAxis.contains(definingAttribute.getId())) {
          newVariantAxis.add(definingAttribute.getId());
        }
      }
      variantAxis = newVariantAxis;
    }
    return variantAxis;
  }

  @Override
  @Nonnull
  public List<ProductVariant> getVariants() {
    if (variants == null) {
      List<ProductVariant> newVariants = new ArrayList<>();
      @SuppressWarnings("unchecked")
      List<HashMap<String, Object>> wcSkus = DataMapHelper.getValueForKey(getDelegate(), "sKUs", List.class);
      if (wcSkus != null && !wcSkus.isEmpty()) {
        for (Map<String, Object> wcSku : wcSkus) {
          String technicalId = DataMapHelper.getValueForKey(wcSku, "uniqueID", String.class);
          if (technicalId != null) {
            CatalogAlias catalogAlias = getCatalogAlias();
            CommerceId commerceId = getCommerceIdProvider().formatProductVariantTechId(catalogAlias, technicalId);
            ProductVariant pv = (ProductVariant) getCommerceBeanFactory().createBeanFor(commerceId, getContext());
            newVariants.add(pv);
          }
        }
      } else {
        //In some cases the initial load mechanism does not come with containing SKUs (e.g. findProductsByCategory).
        //Therefor the product is loaded again via #findProductById to make sure all product data is loaded.
        CatalogAlias catalogAlias = getCatalogAlias();
        CommerceId productTechId = commerceId(PRODUCT).withCatalogAlias(catalogAlias).withTechId(getExternalTechId()).build();
        UserContext userContext = UserContextHelper.getCurrentContext();
        ProductCacheKey productCacheKey = new ProductCacheKey(productTechId, getContext(), userContext, getCatalogWrapperService(), getCommerceCache());
        Map<String, Object> wcProduct = getCommerceCache().get(productCacheKey);
        if (wcProduct != null && wcProduct.containsKey("sKUs")) {
          setDelegate(wcProduct);
          //reset the fields after a new delegate is set.
          variants = null;
          variantAxis = null;
          return getVariants();
        }
      }

      variants = newVariants;
    }
    return variants;
  }

  @Override
  @Nonnull
  public List<ProductVariant> getVariants(@Nullable List<VariantFilter> filters) {
    List<ProductVariant> result = new ArrayList<>();
    List<ProductVariant> allVariants = getVariants();
    if (filters == null || filters.isEmpty()) {
      return allVariants;
    }
    for (ProductVariant productVariant : allVariants) {
      boolean isIncluded = true;
      for (VariantFilter filter : filters) {
        if (!filter.matches(productVariant)) {
          isIncluded = false;
          break;
        }
      }
      if (isIncluded) {
        result.add(productVariant);
      }
    }
    return result;
  }

  @Override
  @Nonnull
  public List<ProductVariant> getVariants(VariantFilter filter) {
    if (filter == null) {
      return getVariants(emptyList());
    }

    return getVariants(singletonList(filter));
  }

  @Override
  @Nonnull
  public Map<ProductVariant, AvailabilityInfo> getAvailabilityMap() {
    return availabilityService.getAvailabilityInfo(this.getVariants());
  }

  @Override
  public float getTotalStockCount() {
    Map<ProductVariant, AvailabilityInfo> availabilityMap = getAvailabilityMap();
    float result = 0;
    for (Map.Entry<ProductVariant, AvailabilityInfo> entry : availabilityMap.entrySet()) {
      result += entry.getValue().getQuantity();
    }

    return result;
  }

  @Override
  public boolean isAvailable() {
    // a product is available if at least one product variant is available
    boolean result = false;

    for (ProductVariant variant : getVariants()) {
      result = result || variant.isAvailable();
    }

    return result;
  }

  @Override
  @Nonnull
  public List<Object> getVariantAxisValues(@Nonnull String axisName, @Nonnull List<VariantFilter> filters) {
    List<Object> result = new ArrayList<>();

    List<ProductVariant> availableProducts = getVariants(filters);
    for (ProductVariant productVariant : availableProducts) {
      Object attributeValue = productVariant.getAttributeValue(axisName);
      if (attributeValue != null && !result.contains(attributeValue)) {
        result.add(attributeValue);
      }
    }
    return result;
  }

  @Override
  @Nonnull
  public List<Object> getVariantAxisValues(@Nonnull String axisName, @Nullable VariantFilter filter) {
    if (filter == null) {
      return getVariantAxisValues(axisName, emptyList());
    }

    return getVariantAxisValues(axisName, singletonList(filter));
  }

  @SuppressWarnings("unused")
  public AvailabilityService getAvailabilityService() {
    return availabilityService;
  }

  public void setAvailabilityService(AvailabilityService availabilityService) {
    this.availabilityService = availabilityService;
  }

  @Override
  public String toString() {
    return "[Product " + getId() + "]";
  }

}
