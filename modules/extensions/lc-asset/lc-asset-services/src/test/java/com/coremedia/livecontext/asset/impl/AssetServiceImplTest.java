package com.coremedia.livecontext.asset.impl;


import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceConnection;
import com.coremedia.blueprint.base.livecontext.util.CommerceReferenceHelper;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.ecommerce.test.MockCommerceEnvBuilder;
import com.coremedia.livecontext.asset.AssetSearchService;
import com.coremedia.livecontext.ecommerce.asset.AssetUrlProvider;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetServiceImplTest{

  private static final String EXTERNAL_ID_1 = "externalId1";
  private static final String EXTERNAL_ID_2 = "externalId2";
  private static final String EXTERNAL_ID_SKU = "externalIdSKU";
  private static final String COMMERCE_ID_1 = "vendor:///catalog/product/" + EXTERNAL_ID_1;
  private static final String COMMERCE_ID_2 = "vendor:///catalog/product/" + EXTERNAL_ID_2;
  private static final String COMMERCE_ID_SKU = "vendor:///catalog/sku/" + EXTERNAL_ID_SKU;
  private static final String COMMERCE_ID_SKU_AS_PRODUCT = "vendor:///catalog/product/" + EXTERNAL_ID_SKU;
  private static final String LINKED_URL = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/" +
          EXTERNAL_ID_1 + ".jpg";
  private static final String LINKED_URL_SKU = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/" +
          EXTERNAL_ID_SKU + ".jpg";
  private static final String NOT_LINKED_URL = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/" +
          EXTERNAL_ID_2 + ".jpg";
  private static final String COMMERCE_URL =
          "http://shop-preview-production-helios.blueprint-box.vagrant/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/boys/bcl014_tops/646x1000/bcl014_1417.jpg";

  private static final String CMPICTURE_DOCTYPE_NAME = "CMPicture";
  
  @InjectMocks
  private TestAssetServiceImpl testling = new TestAssetServiceImpl();

  @Mock
  private AssetUrlProvider assetUrlProvider;
  @Mock
  private SitesService sitesService;
  @Mock
  private SettingsService settingsService;
  @Mock
  private AssetSearchService assetSearchService;
  @Mock
  private Site site1;
  @Mock
  private Site site2;
  @Mock
  private StoreContext storeContext;
  @Mock
  private ContentType pictureContentType;
  @Mock
  private ContentSiteAspect contentSiteAspect;

  @Mock
  private Content picture1;
  @Mock
  private Struct localSettingsStruct1;
  @Mock
  private CapType localSettingsStruct1Type;
  @Mock
  private CapPropertyDescriptor localSettings1PropertyDescriptor;
  @Mock
  private Struct commerceStruct1;
  @Mock
  private CapType commerceStruct1Type;
  @Mock
  private CapPropertyDescriptor commerce1PropertyDescriptor;

  @Mock
  private Content picture2;

  @Mock
  private Struct localSettingsStruct2;
  @Mock
  private CapType localSettingsStruct2Type;
  @Mock
  private CapPropertyDescriptor localSettings2PropertyDescriptor;
  @Mock
  private Struct commerceStruct2;
  @Mock
  private CapType commerceStruct2Type;
  @Mock
  private CapPropertyDescriptor commerce2PropertyDescriptor;

  @Mock
  private Content pictureSKU;

  @Mock
  private Struct localSettingsStructSKU;
  @Mock
  private CapType localSettingsStructSKUType;
  @Mock
  private CapPropertyDescriptor localSettingsSKUPropertyDescriptor;
  @Mock
  private Struct commerceStructSKU;
  @Mock
  private CapType commerceStructSKUType;
  @Mock
  private CapPropertyDescriptor commerceSKUPropertyDescriptor;

  @Mock
  private Content pictureWithoutSite;
  @Mock
  private Content defaultPicture1;
  @Mock
  private Product product;
  @Mock
  private ProductVariant productVariant;
  @Mock
  private Content defaultPicture;

  private TestAssetChanges assetChanges;

  private BaseCommerceConnection commerceConnection;

  @Before
  public void setUp() throws Exception {
    testling.setSitesService(sitesService);
    testling.setAssetSearchService(assetSearchService);
    testling.setSettingsService(settingsService);
    assetChanges = new TestAssetChanges();
    assetChanges.setSitesService(sitesService);
    assetChanges.afterPropertiesSet();
    testling.setAssetChanges(assetChanges);

    commerceConnection = MockCommerceEnvBuilder.create().setupEnv();
    commerceConnection.setAssetService(testling);

    when(sitesService.getContentSiteAspect(picture1)).thenReturn(contentSiteAspect);
    when(sitesService.getContentSiteAspect(picture2)).thenReturn(contentSiteAspect);
    when(sitesService.getContentSiteAspect(defaultPicture1)).thenReturn(contentSiteAspect);
    when(sitesService.getContentSiteAspect(pictureWithoutSite)).thenReturn(contentSiteAspect);

    when(contentSiteAspect.getSite()).thenReturn(site1);

    when(pictureContentType.isSubtypeOf(CMPICTURE_DOCTYPE_NAME)).thenReturn(true);

    when(picture1.getName()).thenReturn("picture1");
    when(picture1.getStruct(CommerceReferenceHelper.STRUCT_PROPERTY_NAME)).thenReturn(localSettingsStruct1);
    when(localSettingsStruct1.getType()).thenReturn(localSettingsStruct1Type);
    when(localSettingsStruct1Type.getDescriptor(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(localSettings1PropertyDescriptor);
    when(localSettingsStruct1.getStruct(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(commerceStruct1);
    when(commerceStruct1.getType()).thenReturn(commerceStruct1Type);
    when(commerceStruct1Type.getDescriptor(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(commerce1PropertyDescriptor);
    when(commerceStruct1.getStrings(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(Collections.singletonList(COMMERCE_ID_1));
    when(picture1.getType()).thenReturn(pictureContentType);

    when(picture2.getName()).thenReturn("picture2");
    when(picture2.getStruct(CommerceReferenceHelper.STRUCT_PROPERTY_NAME)).thenReturn(localSettingsStruct2);
    when(localSettingsStruct2.getType()).thenReturn(localSettingsStruct2Type);
    when(localSettingsStruct2Type.getDescriptor(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(localSettings2PropertyDescriptor);
    when(localSettingsStruct2.getStruct(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(commerceStruct2);
    when(commerceStruct2.getType()).thenReturn(commerceStruct2Type);
    when(commerceStruct2Type.getDescriptor(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(commerce2PropertyDescriptor);
    when(commerceStruct2.getStrings(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(Collections.singletonList(COMMERCE_ID_2));
    when(picture2.getType()).thenReturn(pictureContentType);

    when(pictureSKU.getName()).thenReturn("pictureSKU");
    when(pictureSKU.getStruct(CommerceReferenceHelper.STRUCT_PROPERTY_NAME)).thenReturn(localSettingsStructSKU);
    when(localSettingsStructSKU.getType()).thenReturn(localSettingsStructSKUType);
    when(localSettingsStructSKUType.getDescriptor(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(localSettingsSKUPropertyDescriptor);
    when(localSettingsStructSKU.getStruct(CommerceReferenceHelper.COMMERCE_SUBSTRUCT_NAME)).thenReturn(commerceStructSKU);
    when(commerceStructSKU.getType()).thenReturn(commerceStructSKUType);
    when(commerceStructSKUType.getDescriptor(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(commerceSKUPropertyDescriptor);
    when(commerceStructSKU.getStrings(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(Collections.singletonList(COMMERCE_ID_SKU));
    when(pictureSKU.getType()).thenReturn(pictureContentType);

    when(defaultPicture1.getName()).thenReturn("defaultPicture1");
    when(defaultPicture1.getType()).thenReturn(pictureContentType);
    when(pictureWithoutSite.getName()).thenReturn("pictureWithoutSite");
    when(pictureWithoutSite.getType()).thenReturn(pictureContentType);

    when(assetUrlProvider.getImageUrl(anyString())).thenReturn("http://an/asset/url.jpg");
  }

  @Test
  public void testGetCatalogPicture() throws Exception {
    when(sitesService.getSite(anyString())).thenReturn(site1);
    testling.setAssets(new Content[]{picture1});
    CatalogPicture catalogPicture = testling.getCatalogPicture(LINKED_URL);
    assertNotNull(catalogPicture);
    assertEquals(picture1, catalogPicture.getPicture());
  }

  @Test
  public void testGetCatalogPictureSKU() throws Exception {
    when(sitesService.getSite(anyString())).thenReturn(site1);
    when(commerceConnection.getCatalogService().findProductById(COMMERCE_ID_SKU_AS_PRODUCT)).thenReturn(productVariant);
    when(productVariant.getExternalId()).thenReturn(EXTERNAL_ID_SKU);
    when(productVariant.getReference()).thenReturn(COMMERCE_ID_SKU);
    testling.setAssets(new Content[]{pictureSKU});
    CatalogPicture catalogPicture = testling.getCatalogPicture(LINKED_URL_SKU);
    assertNotNull(catalogPicture);
    assertEquals(pictureSKU, catalogPicture.getPicture());
  }

  @Test
  public void testGetCatalogPicture2() throws Exception {
    when(sitesService.getSite(anyString())).thenReturn(site1);
    CatalogPicture catalogPicture = testling.getCatalogPicture(COMMERCE_URL);
    assertNotNull(catalogPicture);
    assertNull(catalogPicture.getPicture());
    assertNotNull(catalogPicture.getUrl());
  }

  @Test
  public void testGetCatalogPictureSiteDefault() throws Exception {
    when(sitesService.getSite(anyString())).thenReturn(site1);
    when(settingsService.setting(anyString(), eq(Content.class), any(Site.class))).thenReturn(defaultPicture);

    CatalogPicture catalogPicture = testling.getCatalogPicture(NOT_LINKED_URL);
    assertEquals(defaultPicture, catalogPicture.getPicture());
  }

  @Test
  public void testFindAssetsForProducts() throws Exception {
    testling.setAssets(new Content[]{picture1});
    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(1, pictures.size());
    testling.setAssets(new Content[]{});
    pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertTrue(pictures.isEmpty());
  }

  @Test
  public void testFindAssetsForVariants() throws Exception {
    when(commerceConnection.getCatalogService().findProductById(COMMERCE_ID_1)).thenReturn(productVariant);
    when(productVariant.getParent()).thenReturn(product);
    when(product.getExternalId()).thenReturn(EXTERNAL_ID_1);
    testling.setAssets(new Content[]{picture1});
    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(1, pictures.size());
  }

  @Test
  public void testFindAssetsForSKUs() throws Exception {
    when(commerceConnection.getCatalogService().findProductById(COMMERCE_ID_SKU)).thenReturn(productVariant);
    when(productVariant.getParent()).thenReturn(product);
    when(productVariant.getExternalId()).thenReturn(EXTERNAL_ID_SKU);
    testling.setAssets(new Content[]{pictureSKU});
    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_SKU, site1);
    assertEquals(1, pictures.size());
  }

  @Test
  public void testDefaultPicture() throws Exception {
    when(settingsService.setting(anyString(), eq(Content.class), any(Site.class))).thenReturn(defaultPicture);
    when(sitesService.getSite(anyString())).thenReturn(site1);
    List<Content> pictures = testling.findPictures(COMMERCE_ID_1);
    assertEquals(defaultPicture, pictures.iterator().next());
  }

  @Test
  public void testFindAssetsAfterRemoveLastLink() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1, COMMERCE_ID_2}); //simulate repository event for content with a new list of two product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);

    // but the solr still contains a list with one reference
    testling.setAssets(new Content[]{picture1});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertTrue(assets.isEmpty());
  }

  @Test
  public void testFindAssetsAfterRemoveOneLink() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1, COMMERCE_ID_2}); //simulate repository event for content with a new list of two product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    // but the solr still contains a list with two references
    testling.setAssets(new Content[]{picture1, picture2});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_2, site1);
    assertEquals(1, assets.size());
    assertEquals("picture2", ((Content) assets.get(0)).getName());
  }

  @Test
  public void testFindAssetsAfterAddedFirstLink() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1, COMMERCE_ID_2}); //simulate repository event for content with a new list of two product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    // but the solr still contains an empty list with no references
    testling.setAssets(new Content[]{});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(1, assets.size());
    assertEquals("picture1", ((Content) assets.get(0)).getName());
  }

  @Test
  public void testFindAssetsAfterAddedSecondLink() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture2);
    when(commerceStruct2.getStrings(CommerceReferenceHelper.REFERENCES_LIST_NAME)).thenReturn(Collections.singletonList(COMMERCE_ID_1));

    // but the solr still contains a list with one reference
    testling.setAssets(new Content[]{picture1});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(2, assets.size());
    assertEquals("picture1", ((Content) assets.get(0)).getName());
    assertEquals("picture2", ((Content) assets.get(1)).getName());
  }

  @Test
  public void testFindAssetsAfterMoveToOtherSite() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    when(contentSiteAspect.getSite()).thenReturn(site2);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1, COMMERCE_ID_2}); //simulate repository event for content with a new list of two product references
    assetChanges.update(picture1);

    // but the solr still contains a list with one reference
    testling.setAssets(new Content[]{picture1});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(0, assets.size());
  }

  @Test
  public void testFindAssetsAfterMoveToOutOfSites() {

    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{}); //simulate repository event no more product ids
    assetChanges.update(picture1);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1}); //simulate repository event for content with a new list of one product references
    assetChanges.update(picture1);

    when(contentSiteAspect.getSite()).thenReturn(null);
    assetChanges.setExternalReferences(new String[]{COMMERCE_ID_1, COMMERCE_ID_2}); //simulate repository event for content with a new list of two product references
    assetChanges.update(picture1);

    // but the solr still contains a list with one reference
    testling.setAssets(new Content[]{picture1});
    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_1, site1);
    assertEquals(0, assets.size());
  }

  /**
   * Mocks the solr state
   */
  private class TestAssetServiceImpl extends AssetServiceImpl {
    public void setAssets(final Content[] assets) {
      setAssetSearchService(new AssetSearchService() {
        @Nonnull
        @Override
        public List<Content> searchAssets(@Nonnull String contentType, @Nonnull String externalId, @Nonnull Site site) {
          return assets != null ? Arrays.asList(assets) : Collections.<Content>emptyList();
        }
      });
    }
  }

  private class TestAssetChanges extends AssetChanges {
    private String[] references;
    public void setExternalReferences(String[] references) {
      this.references = references;
    }
    @Override
    List<String> getExternalReferences(Content content) {
      return references != null ? Arrays.asList(references) : Collections.<String>emptyList();
    }
  }

}