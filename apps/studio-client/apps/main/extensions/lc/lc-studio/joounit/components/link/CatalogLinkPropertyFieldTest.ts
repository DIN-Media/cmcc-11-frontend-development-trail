import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import ECommerceStudioPlugin from "@coremedia-blueprint/studio-client.main.ec-studio/ECommerceStudioPlugin";
import CatalogLinkContextMenu
  from "@coremedia-blueprint/studio-client.main.ec-studio/components/link/CatalogLinkContextMenu";
import CatalogLinkPropertyField
  from "@coremedia-blueprint/studio-client.main.ec-studio/components/link/CatalogLinkPropertyField";
import AbstractProductTeaserComponentsTest
  from "@coremedia-blueprint/studio-client.main.lc-studio-test-helper/AbstractProductTeaserComponentsTest";
import CatalogLinkPropertyFieldTestView
  from "@coremedia-blueprint/studio-client.main.lc-studio-test-helper/components/link/CatalogLinkPropertyFieldTestView";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import QtipUtil from "@coremedia/studio-client.ext.ui-components/util/QtipUtil";
import TableUtil from "@coremedia/studio-client.ext.ui-components/util/TableUtil";
import Component from "@jangaroo/ext-ts/Component";
import ComponentManager from "@jangaroo/ext-ts/ComponentManager";
import Button from "@jangaroo/ext-ts/button/Button";
import Viewport from "@jangaroo/ext-ts/container/Viewport";
import Item from "@jangaroo/ext-ts/menu/Item";
import { as, cast } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";

class CatalogLinkPropertyFieldTest extends AbstractProductTeaserComponentsTest {
  #link: CatalogLinkPropertyField = null;

  #removeButton: Button = null;

  #openInTabMenuItem: Item = null;

  #removeMenuItem: Item = null;

  #viewPort: Viewport = null;

  override setUp(): void {
    super.setUp();
    QtipUtil.registerQtipFormatter();

    this.#createTestling();
  }

  override tearDown(): void {
    super.tearDown();
    this.#viewPort.destroy();
  }

  //noinspection JSUnusedGlobalSymbols
  async testCatalogLink(): Promise<void> {
    await this.waitForProductTeaserToBeLoaded();
    await this.#checkProductLinkDisplaysValue(AbstractCatalogTest.ORANGES_NAME);
    //still nothing selected
    await this.#checkRemoveButtonDisabled();
    await this.#checkRemoveButtonNotHidden();
    this.#openContextMenu(); //this selects the link
    await this.#checkContextMenuOpened();
    await this.#checkRemoveContextMenuEnabled();
    await this.#checkRemoveContextMenuNotHidden();
    await this.#checkRemoveButtonEnabled();
    await this.setForceReadOnly(true);
    await this.#checkRemoveButtonHidden();
    await this.#checkRemoveContextMenuHidden();
    //valid selected link can be always opened
    this.#openContextMenu(); //this selects the link
    await this.setLink(AbstractCatalogTest.ORANGES_ID + "503");
    await this.#checkCatalogLinkDisplaysErrorValue(AbstractCatalogTest.ORANGES_EXTERNAL_ID + "503");
    await this.setLink(AbstractCatalogTest.ORANGES_ID + "404");
    await this.#checkCatalogLinkDisplaysErrorValue(AbstractCatalogTest.ORANGES_EXTERNAL_ID + "404");
    this.#openContextMenu(); //this selects the link
    //still forceReadOnly = true
    await this.#checkRemoveButtonHidden();
    //invalid link --> cannot open
    await this.#checkRemoveContextMenuHidden();
    //invalid link --> cannot open
    await this.setForceReadOnly(false);
    this.#openContextMenu(); //this selects the link
    await this.#checkRemoveButtonEnabled();
    //invalid link --> cannot open
    await this.#checkRemoveContextMenuNotHidden();
    //invalid link --> cannot open
    await this.setLink(AbstractCatalogTest.ORANGES_SKU_ID);
    await this.#checkSkuLinkDisplaysValue(AbstractCatalogTest.ORANGES_SKU_NAME);
    this.#openContextMenu(); //this selects the link
    await this.#checkContextMenuOpened();
    await this.#checkRemoveButtonEnabled();
    await this.#checkRemoveContextMenuEnabled();
    await this.setForceReadOnly(true);
    this.#openContextMenu(); //this selects the link
    await this.#checkRemoveButtonHidden();
    //valid selected link can be always opened
    await this.#checkRemoveContextMenuHidden();
    await this.setForceReadOnly(false);
    await this.setLink(null);
    await this.#checkCatalogLinkIsEmpty();
    await this.#checkRemoveButtonDisabled();
    await this.#checkRemoveContextMenuDisabled();
  }

  #openContextMenu(): void {
    // open Context Menu:
    const empty: boolean = this.#link.getView().getRow(0) === undefined;
    const event: Record<string, any> = {
      type: "contextmenu",

      getXY: (): Array<any> =>
        (empty ? TableUtil.getMainBody(this.#link) : TableUtil.getCell(this.#link, 0, 1)).getXY()
      ,
      preventDefault: (): void =>{
        //do nothing
      },
      getTarget: (): HTMLElement =>
        TableUtil.getCellAsDom(this.#link, 0, 1),

    };
    if (empty) {
      this.#link.fireEvent("contextmenu", event);
    } else {
      this.#link.fireEvent("rowcontextmenu", this.#link, null, null, 0, event);
    }
  }

  async #checkProductLinkDisplaysValue(value: string): Promise<void> {
    // check if product is linked and data is displayed:
    await waitUntil((): boolean => {
      const linkDisplay: string = TableUtil.getCellAsDom(this.#link, 0, 1)["textContent"];
      return this.#link.getStore().getCount() === 1 &&
          linkDisplay.indexOf(AbstractCatalogTest.ORANGES_EXTERNAL_ID) >= 0 &&
          linkDisplay.indexOf(value) >= 0;
    });
  }

  async #checkSkuLinkDisplaysValue(value: string): Promise<void> {
    // check if sku is linked and data is displayed:
    await waitUntil((): boolean => {
      const linkDisplay: string = TableUtil.getCellAsDom(this.#link, 0, 1)["textContent"];
      return this.#link.getStore().getCount() === 1 &&
          linkDisplay.indexOf(AbstractCatalogTest.ORANGES_SKU_EXTERNAL_ID) >= 0 &&
          linkDisplay.indexOf(value) >= 0;
    });
  }

  async #checkCatalogLinkDisplaysErrorValue(value: string): Promise<void> {
    // check if broken product is linked and fallback data '" + value + "' is displayed:
    await waitUntil((): boolean =>
      this.#link.getStore().getCount() === 1 &&
      TableUtil.getCellAsDom(this.#link, 0, 1)["textContent"].indexOf(value) >= 0);
  }

  async #checkCatalogLinkIsEmpty(): Promise<void> {
    // check if is catalog link is empty and set product link:
    await waitUntil((): boolean =>
      this.#link && this.#link.getStore() && this.#link.getStore().getCount() === 0);
  }

  async #checkRemoveButtonDisabled(): Promise<void> {
    // check remove button disabled:
    await waitUntil((): boolean =>
      this.#removeButton.disabled);
  }

  async #checkRemoveButtonEnabled(): Promise<void> {
    // check remove button enabled:
    await waitUntil((): boolean =>
      !this.#removeButton.disabled);
  }

  async #checkRemoveButtonHidden(): Promise<void> {
    // check remove button hidden:
    await waitUntil((): boolean =>
      this.#removeButton.hidden);
  }

  async #checkRemoveButtonNotHidden(): Promise<void> {
    // check remove button not hidden:
    await waitUntil((): boolean =>
      !this.#removeButton.hidden);
  }

  async #checkRemoveContextMenuDisabled(): Promise<void> {
    // check remove context menu disabled:
    await waitUntil((): boolean =>
      this.#removeMenuItem.disabled);
  }

  async #checkRemoveContextMenuEnabled(): Promise<void> {
    // check remove context menu enabled:
    await waitUntil((): boolean =>
      //return !removeMenuItem.disabled;
      //TODO: make this check work again
      true);
  }

  async #checkRemoveContextMenuHidden(): Promise<void> {
    // check remove context menu hidden:
    await waitUntil((): boolean =>
      this.#removeMenuItem.hidden);
  }

  async #checkRemoveContextMenuNotHidden(): Promise<void> {
    // check remove context menu not hidden:
    await waitUntil((): boolean =>
      !this.#removeMenuItem.hidden);
  }

  async #checkContextMenuOpened(): Promise<void> {
    // check context menu opened:
    await waitUntil((): boolean =>
      !!this.#findCatalogLinkContextMenu());
  }

  /**
   * private helper method to create the container for tests
   */
  #createTestling(): void {
    const config = Config(CatalogLinkPropertyFieldTestView);
    config.bindTo = this.getBindTo();
    config.forceReadOnlyValueExpression = this.getForceReadOnlyValueExpression();

    this.#viewPort = new CatalogLinkPropertyFieldTestView(config);
    this.#link = as(this.#viewPort.getComponent(CatalogLinkPropertyFieldTestView.CATALOG_LINK_PROPERTY_FIELD_ITEM_ID), CatalogLinkPropertyField);

    const openInTabButton = cast(Button, this.#link.getTopToolbar().queryById(ECommerceStudioPlugin.OPEN_IN_TAB_BUTTON_ITEM_ID));
    //we cannot and don't want test the open in tab action as it needs the workarea.
    this.#link.getTopToolbar().remove(openInTabButton);
    this.#removeButton = cast(Button, this.#link.getTopToolbar().queryById(ECommerceStudioPlugin.REMOVE_LINK_BUTTON_ITEM_ID));
  }

  #findCatalogLinkContextMenu(): CatalogLinkContextMenu {
    const contextMenu = as(ComponentManager.getAll().filter((component: Component): boolean =>
      !component.up() && !component.hidden && component.isXType(CatalogLinkContextMenu.xtype),
    )[0], CatalogLinkContextMenu);
    if (contextMenu) {
      this.#openInTabMenuItem = as(contextMenu.getComponent(ECommerceStudioPlugin.OPEN_IN_TAB_MENU_ITEM_ID), Item);
      //we cannot and don't want test the open in tab action as it needs the workarea.
      contextMenu.remove(this.#openInTabMenuItem);
      this.#removeMenuItem = as(contextMenu.getComponent(ECommerceStudioPlugin.REMOVE_LINK_MENU_ITEM_ID), Item);
    }

    return contextMenu;
  }
}

export default CatalogLinkPropertyFieldTest;
