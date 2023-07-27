import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { as } from "@jangaroo/runtime";
import ECommerceStudioPlugin_properties from "../../../../src/ECommerceStudioPlugin_properties";
import CatalogTreeModel from "../../../../src/components/tree/impl/CatalogTreeModel";
import AbstractCatalogStudioTest from "../../../AbstractCatalogStudioTest";

class CatalogTreeModelTest extends AbstractCatalogStudioTest {

  #catalogTreeModel: CatalogTreeModel = null;

  override setUp(): void {
    super.setUp();

    EditorContextImpl.initEditorContext();

    //noinspection BadExpressionStatementJS
    this.#catalogTreeModel = new CatalogTreeModel();
    this.#catalogTreeModel.getSortCategoriesByName = ((): boolean =>
      true
    );
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetStoreText(): Promise<void> {
    // wait for store text:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getText(AbstractCatalogStudioTest.STORE_ID));
    Assert.assertEquals(this.#catalogTreeModel.getText(AbstractCatalogStudioTest.STORE_ID), AbstractCatalogStudioTest.STORE_NAME);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetTopCategoryText(): Promise<void> {
    // wait for the top category loaded:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getText(AbstractCatalogStudioTest.TOP_CATEGORY_ID));
    Assert.assertEquals(this.#catalogTreeModel.getText(AbstractCatalogStudioTest.TOP_CATEGORY_ID), AbstractCatalogStudioTest.TOP_CATEGORY_ID);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetLeafCategoryText(): Promise<void> {
    // wait for the leaf category loaded:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getText(AbstractCatalogStudioTest.LEAF_CATEGORY_ID));
    Assert.assertEquals(this.#catalogTreeModel.getText(AbstractCatalogStudioTest.LEAF_CATEGORY_ID), AbstractCatalogStudioTest.LEAF_CATEGORY_ID);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetTopCategoryIdPath(): Promise<void> {
    // wait for the top categories loaded:
    await waitUntil((): boolean => {
      const idPaths = as(this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.TOP_CATEGORY_ID), Array);
      return idPaths && idPaths.length === 3;
    });
    const idPaths = as(this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.TOP_CATEGORY_ID), Array);
    Assert.assertEquals(idPaths[0], AbstractCatalogStudioTest.STORE_ID);
    Assert.assertEquals(idPaths[1], AbstractCatalogStudioTest.ROOT_CATEGORY_ID);
    Assert.assertEquals(idPaths[2], AbstractCatalogStudioTest.TOP_CATEGORY_ID);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetLeafCategoryIdPath(): Promise<void> {
    // wait for leaf category id path:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.LEAF_CATEGORY_ID));
    const idPaths = as(this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.LEAF_CATEGORY_ID), Array);
    Assert.assertEquals(4, idPaths.length);
    Assert.assertEquals(AbstractCatalogStudioTest.STORE_ID, idPaths[0]);
    Assert.assertEquals(AbstractCatalogStudioTest.ROOT_CATEGORY_ID, idPaths[1]);
    Assert.assertEquals(AbstractCatalogStudioTest.TOP_CATEGORY_ID, idPaths[2]);
    Assert.assertEquals(AbstractCatalogStudioTest.LEAF_CATEGORY_ID, idPaths[3]);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetTopCategoryChildren(): Promise<void> {
    // wait for top category children:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.TOP_CATEGORY_ID));
    const nodeChildren = this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.TOP_CATEGORY_ID);
    Assert.assertEquals(nodeChildren.getChildIds().length, 2);
  }

  // noinspection JSUnusedGlobalSymbols
  async testIdsAreConcatenatedWithLinkPrefix(): Promise<void> {
    // wait for top category children:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.LINK_CATEGORY_ID));
    const nodeChildren = this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.LINK_CATEGORY_ID);
    Assert.assertEquals(1, nodeChildren.getChildIds().length);
    Assert.assertEquals(CatalogTreeModel.HYPERLINK_PREFIX + AbstractCatalogStudioTest.LINK_CATEGORY_ID + CatalogTreeModel.HYPERLINK_SEPARATOR + AbstractCatalogStudioTest.LEAF_CATEGORY_ID, nodeChildren.getChildIds()[0]);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetLeafCategoryChildren(): Promise<void> {
    // wait for leaf category children:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.LEAF_CATEGORY_ID));
    const nodeChildren = this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.LEAF_CATEGORY_ID);
    Assert.assertEquals(nodeChildren.getChildIds().length, 0);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetStoreChildren(): Promise<void> {
    // wait for store children:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.STORE_ID));
    const topLevelIds = this.#catalogTreeModel.getChildren(AbstractCatalogStudioTest.STORE_ID).getChildIds();
    Assert.assertEquals(topLevelIds.length, 2);
    Assert.assertEquals(topLevelIds[0], AbstractCatalogStudioTest.MARKETING_ID);
    Assert.assertEquals(topLevelIds[1], AbstractCatalogStudioTest.ROOT_CATEGORY_ID);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetMarketingSpotsText(): Promise<void> {
    // wait for tree to be built:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getText(AbstractCatalogStudioTest.MARKETING_ID));
    Assert.assertEquals(this.#catalogTreeModel.getText(AbstractCatalogStudioTest.MARKETING_ID), ECommerceStudioPlugin_properties.StoreTree_marketing_root);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetRootId(): Promise<void> {
    // wait for root id to be loaded:
    await waitUntil((): boolean => this.#catalogTreeModel.getRootId() === AbstractCatalogStudioTest.STORE_ID);
    Assert.assertEquals(this.#catalogTreeModel.getRootId(), AbstractCatalogStudioTest.STORE_ID);
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetStoreIdPath(): Promise<void> {
    // wait for store id path:
    await waitUntil((): boolean => !!this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.STORE_ID));
    const idPaths = this.#catalogTreeModel.getIdPath(AbstractCatalogStudioTest.STORE_ID);
    Assert.assertEquals(idPaths.length, 1);
    Assert.assertEquals(idPaths[0], AbstractCatalogStudioTest.STORE_ID);
  }
}

export default CatalogTreeModelTest;
