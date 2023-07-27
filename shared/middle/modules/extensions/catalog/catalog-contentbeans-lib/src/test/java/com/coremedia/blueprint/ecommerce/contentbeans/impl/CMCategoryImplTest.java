package com.coremedia.blueprint.ecommerce.contentbeans.impl;

import com.coremedia.blueprint.base.ecommerce.catalog.content.CatalogContentHelper;
import com.coremedia.blueprint.ecommerce.contentbeans.CMCategory;
import com.coremedia.blueprint.ecommerce.contentbeans.CMProduct;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMCategoryImplTest {

  @Mock
  private CatalogContentHelper catalogContentHelper;

  @Mock
  private Content content;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  private CMCategoryImpl category = new CMCategoryImpl();

  @Before
  public void setUp() throws Exception {
    category = Mockito.spy(new CMCategoryImpl());
    category.setCatalogContentHelper(catalogContentHelper);
    doReturn(content).when(category).getContent();
    doReturn(contentBeanFactory).when(category).getContentBeanFactory();
  }

  @Test
  public void testGetSubcategoriesNoCmsCategoryFound() {
    assertEquals(List.of(), List.copyOf(category.getSubcategories()));
  }

  @Test
  public void testGetSubcategories() {
    Content contentChild1 = mock(Content.class, "child1");
    Content contentChild2 = mock(Content.class, "child2");
    List<Content> contentChildren = List.of(contentChild1, contentChild2);
    when(catalogContentHelper.getSubCategories(content)).thenReturn(contentChildren);

    CMCategory cmCategory1 = mock(CMCategory.class);
    CMCategory cmCategory2 = mock(CMCategory.class);
    List<CMCategory> expectedChildren = List.of(cmCategory1, cmCategory2);
    when(contentBeanFactory.createBeansFor(contentChildren, CMCategory.class)).thenReturn(expectedChildren);

    assertEquals(expectedChildren, category.getSubcategories());
  }

  @Test
  public void testGetProductsNoCmsCategoryFound() {
    assertEquals(List.of(), List.copyOf(category.getProducts()));
  }

  @Test
  public void testGetProducts() {
    Content contentChild1 = mock(Content.class, "child1");
    Content contentChild2 = mock(Content.class, "child2");
    List<Content> contentChildren = List.of(contentChild1, contentChild2);

    when(catalogContentHelper.getProductsForCategory(content)).thenReturn(contentChildren);

    CMProduct cmProduct1 = mock(CMProduct.class);
    CMProduct cmProduct2 = mock(CMProduct.class);
    List<CMProduct> expectedChildren = List.of(cmProduct1, cmProduct2);
    when(contentBeanFactory.createBeansFor(contentChildren, CMProduct.class)).thenReturn(expectedChildren);

    assertEquals(expectedChildren, category.getProducts());
  }

}
