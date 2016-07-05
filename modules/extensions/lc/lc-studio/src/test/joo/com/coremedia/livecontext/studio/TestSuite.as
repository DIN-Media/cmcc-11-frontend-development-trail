package com.coremedia.livecontext.studio {

import com.coremedia.livecontext.studio.action.CollectionViewModelActionTest;
import com.coremedia.livecontext.studio.action.OpenCreateExternalPageDialogActionTest;
import com.coremedia.livecontext.studio.collectionview.CatalogCollectionViewTest;
import com.coremedia.livecontext.studio.components.link.CatalogLinkPropertyFieldTest;
import com.coremedia.livecontext.studio.components.link.CategoryAndProductLinksPropertyFieldTest;
import com.coremedia.livecontext.studio.components.product.ProductNameTextFieldTest;
import com.coremedia.livecontext.studio.library.ShowInCatalogTreeHelperTest;
import com.coremedia.livecontext.studio.forms.ProductTeaserDocumentFormTest;
import com.coremedia.livecontext.studio.forms.ProductTeaserSettingsFormTest;

import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var suite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();
    
    suite.addTestSuite(CatalogCollectionViewTest);
    suite.addTestSuite(CollectionViewModelActionTest);
    suite.addTestSuite(ProductNameTextFieldTest);
    suite.addTestSuite(CatalogLinkPropertyFieldTest);
    suite.addTestSuite(CategoryAndProductLinksPropertyFieldTest);
    suite.addTestSuite(ProductTeaserDocumentFormTest);
    suite.addTestSuite(ShowInCatalogTreeHelperTest);
    suite.addTestSuite(ProductTeaserSettingsFormTest);

    return suite;
  }
}
}
