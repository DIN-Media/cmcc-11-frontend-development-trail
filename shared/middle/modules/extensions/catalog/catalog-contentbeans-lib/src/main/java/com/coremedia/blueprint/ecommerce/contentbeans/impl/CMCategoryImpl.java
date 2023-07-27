package com.coremedia.blueprint.ecommerce.contentbeans.impl;

import com.coremedia.blueprint.base.ecommerce.catalog.content.CatalogContentHelper;
import com.coremedia.blueprint.ecommerce.common.contentbeans.CMAbstractCategoryImpl;
import com.coremedia.blueprint.ecommerce.contentbeans.CMCategory;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.cae.aspect.Aspect;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CMCategoryImpl extends CMAbstractCategoryImpl implements CMCategory {
  private CatalogContentHelper catalogContentHelper;

  // --- configuration ----------------------------------------------

  public void setCatalogContentHelper(CatalogContentHelper catalogContentHelper) {
    this.catalogContentHelper = catalogContentHelper;
  }

  @Override
  protected void initialize() {
    super.initialize();
    if (catalogContentHelper == null) {
      throw new IllegalStateException("Required property not set: catalogContentHelper");
    }
  }

  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMCategory getMaster() {
    return (CMCategory) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMCategory> getVariantsByLocale() {
    return getVariantsByLocale(CMCategory.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMCategory> getLocalizations() {
    return (Collection<? extends CMCategory>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMCategory>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMCategory>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMCategory>> getAspects() {
    return (List<? extends Aspect<? extends CMCategory>>) super.getAspects();
  }

  @NonNull
  @Override
  public List<CMCategory> getSubcategories() {
    return createBeansFor(catalogContentHelper.getSubCategories(getContent()), CMCategory.class);
  }

  @NonNull
  @Override
  public List<CMProduct> getProducts() {
    return createBeansFor(catalogContentHelper.getProductsForCategory(getContent()), CMProduct.class);
  }

}
