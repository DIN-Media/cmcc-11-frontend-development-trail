import Category from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Category";
import Store from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Store";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import Bean from "@coremedia/studio-client.client-core/data/Bean";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import CollectionViewManager
  from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionViewManager";
import CompoundChildTreeModel
  from "@coremedia/studio-client.main.editor-components/sdk/collectionview/tree/CompoundChildTreeModel";
import ContentTreeModel from "@coremedia/studio-client.main.editor-components/sdk/collectionview/tree/ContentTreeModel";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { as, is } from "@jangaroo/runtime";
import CatalogTreeModel from "../../../src/components/tree/impl/CatalogTreeModel";
import CatalogHelper from "../../../src/helper/CatalogHelper";
import ShowInLibraryHelper from "../../../src/library/ShowInLibraryHelper";
import AbstractCatalogStudioTest from "../../AbstractCatalogStudioTest";

class ShowInLibraryHelperTest extends AbstractCatalogStudioTest {

  static PREFERENCE_SHOW_CATALOG_KEY: string = "showCatalogContent";

  #entities: Array<any> = null;

  #treeModel: CompoundChildTreeModel = null;

  #showInLibraryHelper: ShowInLibraryHelper = null;

  #category: Category = null;

  #augmentedCategory: Content = null;

  #functionArguments: Array<any> = null;

  #preferences: Bean = null;

  override setUp(): void {
    super.setUp();
    this.#preferences = beanFactory._.createLocalBean();
    editorContext._["setPreferences"](this.#preferences);
  }

  #setUpCatalog(): void {
    this.#treeModel = new CatalogTreeModel();
    this.#category = as(beanFactory._.getRemoteBean("livecontext/category/HeliosSiteId/catalog/Fruit"), Category);
    this.#entities = new Array(this.#category);
    this.#showInLibraryHelper = new ShowInLibraryHelper(this.#entities, this.#treeModel);
  }

  #setUpRepository(): void {
    this.#treeModel = new ContentTreeModel();
    this.#augmentedCategory = as(beanFactory._.getRemoteBean("content/500"), Content); // Catalog Root
    this.#entities = new Array(this.#augmentedCategory);
    this.#showInLibraryHelper = new ShowInLibraryHelper(this.#entities, this.#treeModel);
  }

  checkOpenedInCatalog(): void {
    editorContext._.getCollectionViewManager = ((): CollectionViewManager =>
      Object.setPrototypeOf({
        showInRepository: (entity, view, treeModelId) => {
          this.#functionArguments = [entity, view, treeModelId];
        },
      }, CollectionViewManager.prototype)
    );
    // do not allow the catalog contents to be shown in content repository
    this.#preferences.set(ShowInLibraryHelperTest.PREFERENCE_SHOW_CATALOG_KEY, false);

    // test for the preferences: no catalog content visible in the repository tree
    this.#showInLibraryHelper.showItems(this.#treeModel.getTreeId());
    Assert.assertNotNull(this.#functionArguments);
    Assert.assertEquals(this.#entities[0], this.#functionArguments[0]);
    Assert.assertEquals(null, this.#functionArguments[1]);
    Assert.assertEquals(this.#treeModel.getTreeId(), this.#functionArguments[2]);
  }

  checkOpenInContentRepository(): void {
    editorContext._.getCollectionViewManager = ((): CollectionViewManager =>
      Object.setPrototypeOf({
        showInRepository: (entity, view, treeModelId) => {
          this.#functionArguments = [entity, view, treeModelId];
        },
      }, CollectionViewManager.prototype)
    );
    // allow the catalog contents to be shown in content repository
    this.#preferences.set(ShowInLibraryHelperTest.PREFERENCE_SHOW_CATALOG_KEY, true);

    // test for the preferences: no catalog content visible in the repository tree
    this.#showInLibraryHelper.showItems(this.#treeModel.getTreeId());
    Assert.assertNotNull(this.#functionArguments);
    Assert.assertEquals(this.#entities[0], this.#functionArguments[0]);
    Assert.assertEquals(null, this.#functionArguments[1]);
    Assert.assertEquals(this.#treeModel.getTreeId(), this.#functionArguments[2]);
  }

  #makeSiteInvalid(): void {
    // make shop invalid and still open the item in the given tree
    editorContext._.getSitesService().getPreferredSiteIdExpression().setValue("TestSiteId");
  }

  async #waitForActiveStoreLoad(): Promise<void> {
    // wait for store to load:
    await waitUntil((): boolean =>
      (is(CatalogHelper.getInstance().getActiveStoreExpression().getValue(), Store)));
  }

  async #waitForCategoryLoad(): Promise<void> {
    // wait for category to load:
    // wait for the complete path to be loaded otherwise it can not be opened in the catalog tree
    await waitUntil((): boolean => !!this.#treeModel.getIdPathFromModel(this.#category));
  }

  // noinspection JSUnusedGlobalSymbols
  async testShowInCatalogTree(): Promise<void> {
    this.#setUpCatalog();
    await this.#waitForActiveStoreLoad();
    await this.#waitForCategoryLoad();
    this.checkOpenedInCatalog();
  }

  // noinspection JSUnusedGlobalSymbols
  async testShowInContentRepositoryTree(): Promise<void> {
    this.#setUpRepository();
    await this.#waitForActiveStoreLoad();
    this.checkOpenInContentRepository();
  }

  // noinspection JSUnusedGlobalSymbols
  async testMyWrongSite(): Promise<void> {
    this.#setUpCatalog();
    await this.#waitForActiveStoreLoad();
    await this.#waitForCategoryLoad();
    this.#makeSiteInvalid();
    await this.#waitForActiveStoreLoad();
    this.checkOpenedInCatalog();
  }
}

export default ShowInLibraryHelperTest;
