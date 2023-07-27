import Category from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Category";
import Store from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Store";
import CatalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/helper/CatalogHelper";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import CompoundChildTreeModel from "@coremedia/studio-client.main.editor-components/sdk/collectionview/tree/CompoundChildTreeModel";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { as, bind } from "@jangaroo/runtime";
import ShowInCatalogTreeHelper from "../../src/library/ShowInCatalogTreeHelper";
import AbstractLiveContextStudioTest from "../AbstractLiveContextStudioTest";

class ShowInCatalogTreeHelperTest extends AbstractLiveContextStudioTest {

  static PREFERENCE_SHOW_CATALOG_KEY: string = "showCatalogContent";

  #entities: Array<any> = null;

  #treeModel: CompoundChildTreeModel = null;

  #showInCatalogTreeHelper: ShowInCatalogTreeHelper = null;

  #category: Category = null;

  #functionArguments: Array<any> = null;

  #setUpCatalog(): void {
    this.#category = as(beanFactory._.getRemoteBean("livecontext/category/HeliosSiteId/catalog/Fruit"), Category);
    this.#entities = new Array(this.#category);
    this.#showInCatalogTreeHelper = new ShowInCatalogTreeHelper(this.#entities);
    this.#treeModel = ShowInCatalogTreeHelper.TREE_MODEL;
  }

  // noinspection JSUnusedGlobalSymbols
  async testSwitchSite(): Promise<void> {
    this.#setUpCatalog();

    // wait for store to load:
    await waitUntil((): boolean =>
      !!(as(CatalogHelper.getInstance().getActiveStoreExpression().getValue(), Store)));

    // wait for category to load:
    await waitUntil((): boolean =>
      // wait for the complete path to be loaded otherwise it can not be opened in the catalog tree
      !!this.#treeModel.getIdPathFromModel(this.#category));

    (this.#showInCatalogTreeHelper as unknown)["adjustSettings"] = ((entity, callback, msg): void => {
      this.#functionArguments = [entity, callback, msg];
    });
    this.preferences.set(ShowInCatalogTreeHelperTest.PREFERENCE_SHOW_CATALOG_KEY, false); // do not allow the catalog contents to be shown in content repository
    // configure wrong site
    editorContext._.getSitesService().getPreferredSiteIdExpression().setValue("TestSiteId");

    // test for the preferences: no catalog content visible in the repository tree
    this.#showInCatalogTreeHelper.showItems(this.#treeModel.getTreeId());
    Assert.assertNotNull(this.#functionArguments);
    Assert.assertEquals(this.#entities[0], this.#functionArguments[0]);
    Assert.assertEquals(bind(this.#showInCatalogTreeHelper, this.#showInCatalogTreeHelper.showInCatalogTree), this.#functionArguments[1]);
  }

}

export default ShowInCatalogTreeHelperTest;
