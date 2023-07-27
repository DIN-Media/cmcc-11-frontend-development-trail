package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.augmentation.config.AugmentationPageGridServiceConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.MappedCatalogsProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.InterceptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.AugmentationHelperBase.DEFAULT_BASE_FOLDER_NAME;
import static com.coremedia.ecommerce.studio.rest.AugmentationHelperBase.EXTERNAL_ID;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.CATEGORY_PAGEGRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.SEGMENT;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.TITLE;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        AugmentationPageGridServiceConfiguration.class,
        CategoryAugmentationHelper.class,
}, properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "repository.params.contentxml=classpath:/com/coremedia/ecommerce/studio/rest/ec-studio-lib-test-content.xml",
        "repository.params.userxml=classpath:/com/coremedia/cap/common/xml/users-default.xml",
})
class CategoryAugmentationHelperTest {

  private static final String CATEGORY_EXTERNALID = "leafCategory";
  private static final String CATEGORY_ID = "test:///catalog/category/" + CATEGORY_EXTERNALID;
  //External ids of category can contain '/'. See CMS-5075
  private static final String CATEGORY_DISPLAY_NAME = "le/af";
  private static final String ESCAPED_CATEGORY_DISPLAY_NAME = "le_af";
  private static final String ROOT = "root";
  private static final String TOP = "top";

  @Autowired
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @SpyBean
  private CategoryAugmentationHelper testling;

  @SpyBean(name = "categoryAugmentationService")
  private AugmentationService augmentationService;

  @SpyBean(name = "contentBackedPageGridService")
  private ContentBackedPageGridService pageGridService;

  @MockBean
  private MappedCatalogsProvider mappedCatalogsProvider;

  @Mock
  private Category rootCategory;

  @Mock
  private Category leafCategory;

  @Mock
  private Catalog catalog;

  @Mock
  private StoreContext storeContext;

  @SuppressWarnings("unused")
  @MockBean
  private InterceptService interceptService;

  @BeforeEach
  public void setUp() {
    Content rootCategoryContent = contentRepository.getContent("20");
    when(augmentationService.getContent(rootCategory)).thenReturn(rootCategoryContent);

    //mock category tree
    when(rootCategory.isRoot()).thenReturn(true);
    when(rootCategory.getDisplayName()).thenReturn(ROOT);
    Category topCategory = mock(Category.class);
    when(topCategory.getParent()).thenReturn(rootCategory);
    when(topCategory.getDisplayName()).thenReturn(TOP);
    leafCategory = mock(Category.class, RETURNS_DEEP_STUBS);
    when(leafCategory.getParent()).thenReturn(topCategory);
    when(leafCategory.getDisplayName()).thenReturn(CATEGORY_DISPLAY_NAME);
    when(leafCategory.getExternalId()).thenReturn(CATEGORY_EXTERNALID);
    when(leafCategory.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow(CATEGORY_ID));
    List<Category> breadcrumb = List.of(rootCategory, topCategory, leafCategory);
    when(leafCategory.getBreadcrumb()).thenReturn(breadcrumb);
    when(leafCategory.getContext().getSiteId()).thenReturn("theSiteId");
    doReturn(Optional.of(catalog)).when(testling).getCatalog(leafCategory);
    when(catalog.isDefaultCatalog()).thenReturn(true);

    when(rootCategory.getContext()).thenReturn(storeContext);
    when(storeContext.getSiteId()).thenReturn("theSiteId");
  }

  @Test
  public void testAugment() {
    testling.augment(leafCategory);

    Content externalChannel = contentRepository.getChild("/Sites/Content Test/" + DEFAULT_BASE_FOLDER_NAME + "/"
            + ROOT + "/" + TOP + "/" + ESCAPED_CATEGORY_DISPLAY_NAME + "/" + ESCAPED_CATEGORY_DISPLAY_NAME + " (" + CATEGORY_EXTERNALID + ")");
    assertThat(externalChannel).isNotNull();
    assertThat(externalChannel.getName()).isEqualTo(ESCAPED_CATEGORY_DISPLAY_NAME  + " (" + CATEGORY_EXTERNALID + ")");
    assertThat(externalChannel.getString(EXTERNAL_ID)).isEqualTo(CATEGORY_ID);
    assertThat(externalChannel.getString(TITLE)).isEqualTo(CATEGORY_DISPLAY_NAME);
    assertThat(externalChannel.getString(SEGMENT)).isEqualTo(CATEGORY_DISPLAY_NAME);

    // Assert the initialized layout for category pages.

    Struct categoryPageGridStruct = externalChannel.getStruct(CATEGORY_PAGEGRID_STRUCT_PROPERTY);
    assertThat(categoryPageGridStruct).isNotNull();

    Struct categoryPlacements2Struct = categoryPageGridStruct.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    assertThat(categoryPlacements2Struct).isNotNull();

    Content categoryLayout = (Content) categoryPlacements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(categoryLayout).isNotNull();
    assertThat(categoryLayout.getName()).isEqualTo("CategoryLayoutSettings");

    // Assert the initialized layout for product pages.

    Struct productPageGridStruct = externalChannel.getStruct(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);
    assertThat(productPageGridStruct).isNotNull();

    Struct productPlacements2Struct = productPageGridStruct.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    assertThat(productPlacements2Struct).isNotNull();

    Content productLayout = (Content) productPlacements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(productLayout).isNotNull();
    assertThat(productLayout.getName()).isEqualTo("ProductLayoutSettings");
  }

  @Test
  public void testAugmentationWithPropertyTooLong() {
    //length of string is 90 characters
    String propertyValueTooLong = "this-display-name-is-too-long-this-display-name-is-too-long-this-display-name-is-too-long";
    when(leafCategory.getDisplayName()).thenReturn(propertyValueTooLong);
    testling.augment(leafCategory);

    Content externalChannel = contentRepository.getChild("/Sites/Content Test/" + DEFAULT_BASE_FOLDER_NAME + "/"
            + ROOT + "/" + TOP + "/" + propertyValueTooLong + "/" + propertyValueTooLong + " (" + CATEGORY_EXTERNALID + ")");
    assertThat(externalChannel).isNotNull();

    //the segment property is limited to 64 characters by default
    assertThat(externalChannel.getString(SEGMENT)).isEqualTo(propertyValueTooLong.substring(0, 63));
  }

  @Test
  public void testLookupAugmentedRootCategoryInOtherCatalogsHit() {
    Category bCategory = mock(Category.class);
    List<Category> listOfRootCategories = new ArrayList<>();
    listOfRootCategories.add(0, rootCategory);
    listOfRootCategories.add(1, bCategory);
    Content bCategoryAugmentation = mock(Content.class);
    when(mappedCatalogsProvider.getConfiguredRootCategories(any(StoreContext.class))).thenReturn(listOfRootCategories);
    when(augmentationService.getContent(rootCategory)).thenReturn(null);
    when(augmentationService.getContent(bCategory)).thenReturn(bCategoryAugmentation);

    Optional<Content> content = testling.lookupAugmentedRootCategoryInOtherCatalogs(leafCategory);

    assertThat(content).isPresent();
    assertThat(content.get()).isEqualTo(bCategoryAugmentation);
  }

  @Test
  public void testInitializeLayoutSettingsWithInvalidState() {
    when(augmentationService.getContent(rootCategory)).thenReturn(null);

    assertThatThrownBy(() -> testling.initializeLayoutSettings(leafCategory, emptyMap()))
            .isInstanceOf(CommerceAugmentationException.class);
  }

  @Test
  public void testInitializeRootCategoryContent() {
    Content layoutFromSiteRoot = contentRepository.getContent("224");
    when(augmentationService.getContent(rootCategory)).thenReturn(null);
    doReturn(layoutFromSiteRoot).when(pageGridService).getLayout(any(Content.class), eq(PAGE_GRID_STRUCT_PROPERTY));

    Map<String, Object> properties = new HashMap<>();
    testling.initializeRootCategoryContent(rootCategory, properties);

    assertThat(properties).isNotEmpty();
    Struct placement = (Struct) properties.get("placement");
    Struct pdpPageGrid = (Struct) properties.get("pdpPagegrid");
    assertThat(placement).isNotNull();
    assertThat(pdpPageGrid).isNotNull();
    assertThat(placement).isEqualTo(pdpPageGrid);

    Content layoutPlacement = (Content) placement.getStruct(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME).get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
    assertThat(layoutPlacement).isNotNull();
    assertThat(layoutPlacement).isEqualTo(layoutFromSiteRoot);
  }

}
