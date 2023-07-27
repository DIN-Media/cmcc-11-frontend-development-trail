import CategoryImpl from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CategoryImpl";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import SitesService from "@coremedia/studio-client.multi-site-models/SitesService";
import { bind, cast } from "@jangaroo/runtime";
import augmentedCategoryTreeRelation from "../../../src/tree/augmentedCategoryTreeRelation";
import AbstractCatalogStudioTest from "../../AbstractCatalogStudioTest";

class AugmentedCategoryTreeRelationTest extends AbstractCatalogStudioTest {

  #siteRootDocument: Content = null;

  #rootCategoryDocument: Content = null;

  #topCategory: CategoryImpl = null;

  #topCategoryDocument: Content = null;

  #leafCategoryDocument: Content = null;

  editorContext_getSitesService: () => SitesService = null;

  override setUp(): void {
    super.setUp();

    EditorContextImpl.initEditorContext();

    const editorContext = EditorContextImpl.getInstance();
    this.editorContext_getSitesService = bind(editorContext, editorContext.getSitesService);
    editorContext.getSitesService = bind(this, this.#getSitesService);

    this.#siteRootDocument = cast(Content, beanFactory._.getRemoteBean(AbstractCatalogStudioTest.SITE_ROOT_DOCUMENT_ID));
    this.#rootCategoryDocument = cast(Content, beanFactory._.getRemoteBean(AbstractCatalogStudioTest.ROOT_CATEGORY_DOCUMENT_ID));
    this.#topCategory = cast(CategoryImpl, beanFactory._.getRemoteBean(AbstractCatalogStudioTest.TOP_CATEGORY_ID));
    this.#topCategoryDocument = cast(Content, beanFactory._.getRemoteBean(AbstractCatalogStudioTest.TOP_CATEGORY_DOCUMENT_ID));
    this.#leafCategoryDocument = cast(Content, beanFactory._.getRemoteBean(AbstractCatalogStudioTest.LEAF_CATEGORY_DOCUMENT_ID));
  }

  override tearDown(): void {
    super.tearDown();
    editorContext._.getSitesService = this.editorContext_getSitesService;
  }

  // noinspection JSUnusedGlobalSymbols
  testGetChildrenOfLeafCategoryDocument(): void {
    //TODO: nothing to test as augmentedCategoryTreeRelation#getChildrenOf is not implemented yet.
  }

  // noinspection JSUnusedGlobalSymbols
  async testGetParentUncheckedOfLeafCategoryDocument(): Promise<void> {
    await this.#waitForTheUncheckedParentOfLeafCategoryDocumentToBeRootCategoryDocument();
    this.#augmentTopCategory();
    await this.#waitForTheUncheckedParentOfLeafCategoryDocumentToBeTopCategoryDocument();
  }

  async #waitForTheUncheckedParentOfLeafCategoryDocumentToBeRootCategoryDocument(): Promise<void> {
    // wait for the unchecked parent of the leaf category document to be the category root document:
    await waitUntil((): boolean =>
      this.#rootCategoryDocument === augmentedCategoryTreeRelation.getParentUnchecked(this.#leafCategoryDocument));
  }

  #augmentTopCategory(): void {
    // Augment the top category
    this.#topCategory.getContent = ((): Content =>
      this.#topCategoryDocument
    );
  }

  async #waitForTheUncheckedParentOfLeafCategoryDocumentToBeTopCategoryDocument(): Promise<void> {
    // wait for the unchecked parent of the leaf category document to be the top category document:
    await waitUntil((): boolean =>
      this.#topCategoryDocument === augmentedCategoryTreeRelation.getParentUnchecked(this.#leafCategoryDocument));
  }

  // noinspection JSUnusedGlobalSymbols
  async testIsRootOfSiteRootDocument(): Promise<void> {
    // wait for the site root document to be evaluated to be root:
    await waitUntil((): boolean =>
      augmentedCategoryTreeRelation.isRoot(this.#siteRootDocument));
  }

  // noinspection JSUnusedGlobalSymbols
  async testIsNotRootOfRootCategoryDocument(): Promise<void> {
    // wait for the root category document to be evaluated not to be root:
    await waitUntil((): boolean =>
      !augmentedCategoryTreeRelation.isRoot(this.#rootCategoryDocument));
  }

  #getSitesService(): SitesService {
    return Object.setPrototypeOf({
      getSiteRootDocument: (_siteId: string): any =>
        this.#siteRootDocument
      ,
      getSiteIdFor(_content: Content): string {
        return AbstractCatalogStudioTest.HELIOS_SITE_ID;
      },
    }, SitesService.prototype);
  }

}

export default AugmentedCategoryTreeRelationTest;
