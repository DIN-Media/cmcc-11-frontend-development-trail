package com.coremedia.lc.studio.lib.augmentation;

import com.coremedia.blueprint.base.livecontext.augmentation.config.AugmentationPageGridServiceConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.studio.rest.CommerceAugmentationException;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.InterceptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.coremedia.lc.studio.lib.augmentation.AugmentationHelperBase.DEFAULT_BASE_FOLDER_NAME;
import static com.coremedia.lc.studio.lib.augmentation.AugmentationHelperBase.EXTERNAL_ID;
import static com.coremedia.lc.studio.lib.augmentation.CategoryAugmentationHelper.CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY;
import static com.coremedia.lc.studio.lib.augmentation.ProductAugmentationHelper.TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        AugmentationPageGridServiceConfiguration.class,
        ProductAugmentationHelper.class,
}, properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/com/coremedia/lc/studio/lib/augmentation/contenttest.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml",
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProductAugmentationHelperTest {

  private static final String PRODUCT_EXTERNAL_ID = "prodId";
  private static final String PRODUCT_ID = "test:///catalog/product/" + PRODUCT_EXTERNAL_ID;
  private static final String CATEGORY_ID = "test:///catalog/category/leafCategory";
  private static final String CATEGORY_DISPLAY_NAME = "leaf";
  private static final String PRODUCT_NAME = "productName";
  private static final String ROOT = "root";
  private static final String TOP = "top";

  @Autowired
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @SpyBean
  private ProductAugmentationHelper testling;

  @SpyBean(name = "categoryAugmentationService")
  private AugmentationService categoryAugmentationService;

  @Mock
  private Category rootCategory;

  @Mock
  private Category leafCategory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Product product;

  @Mock
  private Catalog catalog;

  @SuppressWarnings("unused")
  @MockBean
  private InterceptService interceptService;

  @BeforeEach
  public void setUp() {
    Content rootCategoryContent = contentRepository.getContent("20");
    when(categoryAugmentationService.getContent(rootCategory)).thenReturn(rootCategoryContent);

    //mock category tree
    when(rootCategory.isRoot()).thenReturn(true);
    when(rootCategory.getDisplayName()).thenReturn(ROOT);
    Category topCategory = mock(Category.class);
    when(topCategory.getParent()).thenReturn(rootCategory);
    when(topCategory.getDisplayName()).thenReturn(TOP);
    when(leafCategory.getParent()).thenReturn(topCategory);
    when(leafCategory.getDisplayName()).thenReturn(CATEGORY_DISPLAY_NAME);
    when(leafCategory.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow(CATEGORY_ID));
    doReturn(Optional.of(catalog)).when(testling).getCatalog(leafCategory);
    when(catalog.isDefaultCatalog()).thenReturn(true);

    List<Category> breadcrumb = List.of(rootCategory, topCategory, leafCategory);
    when(leafCategory.getBreadcrumb()).thenReturn(breadcrumb);
    StoreContext storeContext = mock(StoreContext.class);
    when(leafCategory.getContext()).thenReturn(storeContext);
    when(storeContext.getSiteId()).thenReturn("theSiteId");

    when(product.getCategory()).thenReturn(leafCategory);
    when(product.getName()).thenReturn(PRODUCT_NAME);
    when(product.getExternalId()).thenReturn(PRODUCT_EXTERNAL_ID);
    when(product.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow(PRODUCT_ID));
  }

  @Test
  public void testAugment() {
    testling.augment(product);

    Content cmProduct = contentRepository.getChild("/Sites/Content Test/" + DEFAULT_BASE_FOLDER_NAME + "/"
            + ROOT + "/" + TOP + "/" + CATEGORY_DISPLAY_NAME + "/" + product.getName() + " (" + PRODUCT_EXTERNAL_ID + ")");
    assertThat(cmProduct).isNotNull();
    assertThat(cmProduct.getName()).isEqualTo(PRODUCT_NAME + " (" + PRODUCT_EXTERNAL_ID + ")");
    assertThat(cmProduct.getString(EXTERNAL_ID)).isEqualTo(PRODUCT_ID);
    assertThat(cmProduct.getString(TITLE)).isEqualTo(PRODUCT_NAME);

    // Assert the initialized layout for product pages.
    Struct productPageGridStruct = cmProduct.getStruct(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);
    assertThat(productPageGridStruct).isNotNull();

    Struct productPlacements2Struct = productPageGridStruct.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    assertThat(productPlacements2Struct).isNotNull();

    Content productLayout = (Content) productPlacements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(productLayout).isNotNull();
    assertThat(productLayout.getName()).isEqualTo("ProductLayoutSettings");
  }

  @Test
  public void testInitializeLayoutSettingsWithInvalidState() {
    when(categoryAugmentationService.getContent(rootCategory)).thenReturn(null);

    assertThatThrownBy( () -> testling.initializeLayoutSettings(product, Collections.emptyMap()))
            .isInstanceOf(CommerceAugmentationException.class);
  }

}
