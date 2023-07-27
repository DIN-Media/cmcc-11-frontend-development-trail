import CatalogHelper from "@coremedia-blueprint/studio-client.main.ec-studio/helper/CatalogHelper";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import Bean from "@coremedia/studio-client.client-core/data/Bean";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import CollectionViewModelAction from "@coremedia/studio-client.main.editor-components/sdk/actions/CollectionViewModelAction";
import CollectionViewModel from "@coremedia/studio-client.main.editor-components/sdk/collectionview/CollectionViewModel";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Store from "@jangaroo/ext-ts/data/Store";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { cast } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import { AnyFunction } from "@jangaroo/runtime/types";
import AbstractLiveContextStudioTest from "../AbstractLiveContextStudioTest";

class CollectionViewModelActionTest extends AbstractLiveContextStudioTest {
  static readonly #MODE_PROPERTY: string = CollectionViewModel.MODE_PROPERTY;

  static readonly #REPOSITORY_MODE: string = CollectionViewModel.REPOSITORY_MODE;

  static readonly #SEARCH_MODE: string = CollectionViewModel.SEARCH_MODE;

  #repositoryAction: CollectionViewModelAction = null;

  #searchAction: CollectionViewModelAction = null;

  #getPreferredSite: AnyFunction = null;

  #preferredSiteExpression: ValueExpression = null;

  override setUp(): void {
    super.setUp();

    this.#preferredSiteExpression = ValueExpressionFactory.create("site", beanFactory._.createLocalBean({ site: "HeliosSiteId" }));
    this.#getPreferredSite = editorContext._.getSitesService().getPreferredSiteId;
    editorContext._.getSitesService().getPreferredSiteId = ((): string =>
      this.#preferredSiteExpression.getValue()
    );

    this.#repositoryAction = new CollectionViewModelAction(
      Config(CollectionViewModelAction, {
        property: CollectionViewModel.MODE_PROPERTY,
        value: CollectionViewModelActionTest.#REPOSITORY_MODE,
      }));

    this.#searchAction = new CollectionViewModelAction(
      Config(CollectionViewModelAction, {
        property: CollectionViewModelActionTest.#MODE_PROPERTY,
        value: CollectionViewModelActionTest.#SEARCH_MODE,
      }));
  }

  override tearDown(): void {
    super.tearDown();
    editorContext._.getSitesService().getPreferredSiteId = this.#getPreferredSite;
  }

  //noinspection JSUnusedGlobalSymbols
  async testDefault(): Promise<void> {
    await this.#waitForDefault();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInCmsToSearch(): Promise<void> {
    this.#switchToSearch();
    await this.#waitForCmsSearch();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInCmsToRepository(): Promise<void> {
    this.#switchToSearch();
    await this.#waitForCmsSearch();
    this.#switchToRepository();
    await this.#waitForDefault();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInCatalogToSearch(): Promise<void> {
    this.#switchToCatalog();
    await this.#waitForCatalogRepository();
    this.#switchToSearch();
    this.#waitForCatalogSearch();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInCatalogToRepository(): Promise<void> {
    this.#switchToCatalog();
    this.#switchToSearch();
    await this.#waitForCatalogSearch();
    this.#switchToRepository();
    await this.#waitForCatalogRepository();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInRepositoryToCatalog(): Promise<void> {
    this.#switchToCatalog();
    await this.#waitForCatalogRepository();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInRepositoryToCms(): Promise<void> {
    this.#switchToCatalog();
    await this.#waitForCatalogRepository();
    await this.#waitForDefault();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInSearchToCatalog(): Promise<void> {
    this.#switchToSearch();
    await this.#waitForCmsSearch();
    this.#switchToCatalog();
    await this.#waitForCatalogSearch();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInSearchToCms(): Promise<void> {
    this.#switchToSearch();
    this.#switchToCatalog();
    await this.#waitForCatalogSearch();
    await this.#waitForCmsSearch();
  }

  /**
   *  Waiting and Testing Steps
   */

  async #waitForDefault(): Promise<void> {
    // wait for default repository cms and default mode repository:
    await waitUntil((): boolean =>
      CollectionViewModelActionTest.getCollectionViewState().get(CollectionViewModelActionTest.#MODE_PROPERTY) === CollectionViewModelActionTest.#REPOSITORY_MODE);

    Assert.assertTrue(this.#repositoryAction.isPressed());
    Assert.assertFalse(this.#searchAction.isPressed());
  }

  async #waitForCmsSearch(): Promise<void> {
    // wait for repository cms and search mode:
    await waitUntil((): boolean =>
      CollectionViewModelActionTest.getCollectionViewState().get(CollectionViewModelActionTest.#MODE_PROPERTY) === CollectionViewModelActionTest.#SEARCH_MODE);
    Assert.assertFalse(this.#repositoryAction.isPressed());
    Assert.assertTrue(this.#searchAction.isPressed());
  }

  async #waitForCatalogRepository(): Promise<void> {
    // wait for repository catalog and mode repository:
    await waitUntil((): boolean =>
      CollectionViewModelActionTest.getCollectionViewState().get(CollectionViewModelActionTest.#MODE_PROPERTY) === CollectionViewModelActionTest.#REPOSITORY_MODE);
    Assert.assertTrue(this.#repositoryAction.isPressed());
    Assert.assertFalse(this.#searchAction.isPressed());
  }

  async #waitForCatalogSearch(): Promise<void> {
    // wait for repository catalog and mode search:
    await waitUntil((): boolean =>
      CollectionViewModelActionTest.getCollectionViewState().get(CollectionViewModelActionTest.#MODE_PROPERTY) === CollectionViewModelActionTest.#SEARCH_MODE);
    Assert.assertFalse(this.#repositoryAction.isPressed());
    Assert.assertTrue(this.#searchAction.isPressed());
  }

  /**
   * Action Steps
   */

  #switchToRepository(): void {
    // switch to repository:
    this.#repositoryAction.execute();
  }

  #switchToSearch(): void {
    // switch to search:
    this.#searchAction.execute();
  }

  #switchToCatalog(): void {
    // switch to catalog:
    const store: Store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
    CollectionViewModelActionTest.getCollectionViewState().set(CollectionViewModel.FOLDER_PROPERTY, store);
  }

  static getCollectionViewState(): Bean {
    return cast(EditorContextImpl, editorContext._).getCollectionViewModel().getMainStateBean();
  }

}

export default CollectionViewModelActionTest;
