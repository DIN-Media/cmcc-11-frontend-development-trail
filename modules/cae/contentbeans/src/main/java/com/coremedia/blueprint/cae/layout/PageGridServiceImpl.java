package com.coremedia.blueprint.cae.layout;

import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.common.contentbeans.CMHasContexts;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.layout.HasPageGrid;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.layout.PageGridService;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.blueprint.viewtype.ViewtypeService;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;

public class PageGridServiceImpl implements PageGridService {

  private ContentBackedPageGridService contentBackedPageGridService;
  private ValidationService<Linkable> validationService;
  private ViewtypeService viewtypeService;

  @Required
  public void setContentBackedPageGridService(ContentBackedPageGridService contentBackedPageGridService) {
    this.contentBackedPageGridService = contentBackedPageGridService;
  }

  @Required
  public void setValidationService(ValidationService<Linkable> validationService) {
    this.validationService = validationService;
  }

  @Required
  public void setViewtypeService(ViewtypeService viewtypeService) {
    this.viewtypeService = viewtypeService;
  }

  @Nonnull
  @Override
  public PageGrid getContentBackedPageGrid(HasPageGrid bean) {
    return new PageGridImpl(bean, contentBackedPageGridService, validationService, viewtypeService);
  }
}
