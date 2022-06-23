package com.coremedia.blueprint.training.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMArticleImpl;
import com.coremedia.blueprint.common.contentbeans.CMVideo;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.blueprint.training.validation.SupportsCustomValidation;
import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;

public class CMVideoTutorialImpl extends CMArticleImpl implements CMVideoTutorial, SupportsCustomValidation {

  @Override
  public CMVideo getVideo() {
    return filter(getVideoUnfiltered());
  }

  public CMVideo getVideoUnfiltered() {
    Content link = getContent().getLink("video");
    return createBeanFor(link, CMVideo.class);
  }

  @Override
  public Markup getProductionInfo() {
    return getMarkup("productionInfo");
  }

  @Override
  public int getDuration() {
    return getContent().getInt("duration");
  }

  @Override
  public String getCopyright() {
    return getContent().getString("copyright");
  }

  @Override
  public CMProduct getFeaturedProduct() {
    return filter(getFeaturedProductUnfiltered());
  }

  public CMProduct getFeaturedProductUnfiltered() {
    Content product = getContent().getLink("featuredProduct");
    return createBeanFor(product, CMProduct.class);
  }

  private <T extends Linkable> T filter (T bean) {
    return bean!=null && getValidationService().validate(bean) ? bean : null;
  }

  @Override
  public boolean validate() {
    CMVideo video = getVideoUnfiltered();
    return (video == null || getValidationService().validate(video));
  }
}
