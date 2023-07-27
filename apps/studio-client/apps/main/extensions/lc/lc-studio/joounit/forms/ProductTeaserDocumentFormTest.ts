import { createDefaultCKEditor } from "@coremedia-blueprint/studio-client.ckeditor5/";
import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import AbstractProductTeaserComponentsTest
  from "@coremedia-blueprint/studio-client.main.lc-studio-test-helper/AbstractProductTeaserComponentsTest";
import CKEditorTypes from "@coremedia/studio-client.ckeditor-common/CKEditorTypes";
import RichTextAreaConstants from "@coremedia/studio-client.ckeditor-common/RichTextAreaConstants";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import CKEditor5RichTextArea from "@coremedia/studio-client.ext.ckeditor5-components/CKEditor5RichTextArea";
import richTextAreaRegistry
  from "@coremedia/studio-client.ext.richtext-components-toolkit/richtextArea/richTextAreaRegistry";
import TeaserOverlayContainer from "@coremedia/studio-client.main.teaser-overlay-components/TeaserOverlayContainer";
import Component from "@jangaroo/ext-ts/Component";
import Viewport from "@jangaroo/ext-ts/container/Viewport";
import TextField from "@jangaroo/ext-ts/form/field/Text";
import TextArea from "@jangaroo/ext-ts/form/field/TextArea";
import QuickTipManager from "@jangaroo/ext-ts/tip/QuickTipManager";
import { as } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ProductTeaserDocumentFormTestView from "./ProductTeaserDocumentFormTestView";

class ProductTeaserDocumentFormTest extends AbstractProductTeaserComponentsTest {
  #viewPort: Viewport = null;

  #productTeaserTitleField: TextField = null;

  #productTeaserTextArea: TextArea = null;

  override setUp(): void {
    super.setUp();
    QuickTipManager.init(true);
  }

  override tearDown(): void {
    super.tearDown();
    this.#viewPort && this.#viewPort.destroy();
  }

  //noinspection JSUnusedGlobalSymbols
  async testProductTeaserDocumentForm(): Promise<void> {
    this.#createTestling();
    await this.waitForContentRepositoryLoaded();
    await this.waitForContentTypesLoaded();
    await this.waitForProductTeaserToBeLoaded();
    await this.waitForProductTeaserContentTypeToBeLoaded();
    await this.#waitForTeaserTitleFieldValue(AbstractCatalogTest.ORANGES_NAME);
    //this test step is ignored for ckeditor 5 for now, as we do currently not support the delegate to another property field.
    await this.#waitForTeaserTextAreaValue(AbstractCatalogTest.ORANGES_SHORT_DESC);
  }

  async #waitForTeaserTitleFieldValue(value: string): Promise<void> {
    // Wait for the product teaser title field to be <value>:
    await waitUntil((): boolean =>
      this.#productTeaserTitleField.getValue() === value);
  }

  async #waitForTeaserTextAreaValue(value: string): Promise<void> {
    if (this.#isCKEditor5Active()) {
      // skip for CKEditor 5:
      return;
    }
    // Wait for the product teaser text area to be <value>:
    await waitUntil((): boolean =>
      this.#productTeaserTextArea.getValue() && this.#productTeaserTextArea.getValue().indexOf(value) >= 0);
  }

  #isCKEditor5Active(): boolean {
    const viewPort = this.#getViewPort();
    const teaserOverlayContainers = viewPort.queryBy((component): Component | false => {
      if (component instanceof TeaserOverlayContainer) {
        return component;
      }
      return false;
    });
    return teaserOverlayContainers.length > 0;
  }

  #getViewPort(): Viewport {
    return this.#viewPort;
  }
  #createTestling(): void {
    const config = Config(ProductTeaserDocumentFormTestView);
    config.bindTo = this.getBindTo();
    this.#initCKEditor5();
    this.#viewPort = new ProductTeaserDocumentFormTestView(config);
    this.#productTeaserTitleField = as(this.#viewPort.queryById("stringPropertyField"), TextField);
    this.#productTeaserTextArea = as(this.#viewPort.queryById("textAreaPropertyField"), TextArea);
  }

  #initCKEditor5(): void {
    richTextAreaRegistry.registerRichTextArea(RichTextAreaConstants.CKE5_EDITOR, Config(CKEditor5RichTextArea, {
      editorTypeMap: new Map([
        [CKEditorTypes.DEFAULT_EDITOR_TYPE, createDefaultCKEditor],
      ]),
    }));
  }
}

export default ProductTeaserDocumentFormTest;
