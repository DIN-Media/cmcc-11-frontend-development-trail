package com.coremedia.lc.studio.lib.augmentation;

import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.rest.linking.ResponseLocationHeaderLinker;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.invoke.MethodHandles.lookup;

@RestController
public class AugmentationResource {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  private final CategoryAugmentationHelper categoryAugmentationHelper;
  private final ProductAugmentationHelper productAugmentationHelper;

  public AugmentationResource(CategoryAugmentationHelper categoryAugmentationHelper,
                              ProductAugmentationHelper productAugmentationHelper) {
    this.categoryAugmentationHelper = categoryAugmentationHelper;
    this.productAugmentationHelper = productAugmentationHelper;
  }

  @PostMapping("livecontext/store/{siteId}/augment") // extracted from com.coremedia.ecommerce.studio.rest.StoreResource
  @ResponseLocationHeaderLinker
  @Nullable
  public Content augment(@RequestBody @NonNull Object catalogObject) {
    if (catalogObject instanceof Category) {
      return categoryAugmentationHelper.augment((Category) catalogObject);
    } else if (catalogObject instanceof Product) {
      return productAugmentationHelper.augment((Product) catalogObject);
    } else {
      LOG.debug("Cannot augment object {}: only categories and products are supported. JSON parameters: {}", catalogObject, catalogObject);
      return null;
    }
  }

}
