import ECommerceStudioPlugin from "@coremedia-blueprint/studio-client.main.ec-studio/ECommerceStudioPlugin";
import CatalogLinkContextMenu
  from "@coremedia-blueprint/studio-client.main.ec-studio/components/link/CatalogLinkContextMenu";
import CatalogLinkPropertyField
  from "@coremedia-blueprint/studio-client.main.ec-studio/components/link/CatalogLinkPropertyField";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import StructRemoteBean from "@coremedia/studio-client.cap-rest-client/struct/StructRemoteBean";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import StatefulQuickTip from "@coremedia/studio-client.ext.ui-components/components/StatefulQuickTip";
import ReadOnlyStateMixin from "@coremedia/studio-client.ext.ui-components/mixins/ReadOnlyStateMixin";
import ContextMenuEventAdapter from "@coremedia/studio-client.ext.ui-components/util/ContextMenuEventAdapter";
import QtipUtil from "@coremedia/studio-client.ext.ui-components/util/QtipUtil";
import TableUtil from "@coremedia/studio-client.ext.ui-components/util/TableUtil";
import Ext from "@jangaroo/ext-ts";
import Component from "@jangaroo/ext-ts/Component";
import ComponentManager from "@jangaroo/ext-ts/ComponentManager";
import Button from "@jangaroo/ext-ts/button/Button";
import Item from "@jangaroo/ext-ts/menu/Item";
import QuickTipManager from "@jangaroo/ext-ts/tip/QuickTipManager";
import { as, asConfig, bind, cast } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import { AnyFunction } from "@jangaroo/runtime/types";
import InheritReferencesAction from "../src/action/InheritReferencesAction";
import InheritReferencesButton from "../src/components/InheritReferencesButton";
import AbstractCatalogAssetTest from "./AbstractCatalogAssetTest";
import InheritReferencesTestView from "./InheritReferencesTestView";

class InheritReferencesTest extends AbstractCatalogAssetTest {
  #bindTo: ValueExpression = null;

  #forceReadOnlyValueExpression: ValueExpression = null;

  #contentReadOnlyExpression: ValueExpression = null;

  #register: AnyFunction = null;

  #getQuickTip: AnyFunction = null;

  #viewport: InheritReferencesTestView = null;

  #inheritButton: InheritReferencesButton = null;

  #myCatalogLink: CatalogLinkPropertyField = null;

  #removeMenuItem: Item = null;

  #removeButton: Button = null;

  #inheritExpression: ValueExpression = null;

  #referencesExpression: ValueExpression = null;

  #inheritAction: InheritReferencesAction = null;

  override setUp(): void {
    super.setUp();

    this.#bindTo = ValueExpressionFactory.createFromValue();
    this.#forceReadOnlyValueExpression = ValueExpressionFactory.createFromValue(false);
    this.#contentReadOnlyExpression = ValueExpressionFactory.createFromValue(false);

    this.#contentReadOnlyExpression.addChangeListener((ve: ValueExpression): void => {
      this.#forceReadOnlyValueExpression.setValue(ve.getValue());
    });

    //Obviously the inherit toggle button in the test setup has a problem with QuickTips...
    this.#register = bind(QuickTipManager, QuickTipManager.register);
    QuickTipManager.register = ((): void => {});

    //We have to mock QuickTips.getQuickTip as this returns undefined
    this.#getQuickTip = bind(QuickTipManager, QuickTipManager.getQuickTip);
    QuickTipManager.getQuickTip = ((): StatefulQuickTip =>
      Ext.create(StatefulQuickTip, {})
    );

    QtipUtil.registerQtipFormatter();
  }

  #setBindTo(path: string): void {
    const picture = as(beanFactory._.getRemoteBean(path), Content);
    //we need to mock the write access
    picture.getRepository().getAccessControl().mayWrite = ((): boolean =>
      !this.#contentReadOnlyExpression.getValue()
    );
    const localSettings = as(beanFactory._.getRemoteBean(path + "/structs/localSettings"), StructRemoteBean);
    //PUT should cause no trouble
    localSettings["doWriteChanges"] = ((): void => {
      //ignore
    });

    this.#bindTo.setValue(picture);

  }

  override tearDown(): void {
    super.tearDown();
    this.#viewport && this.#viewport.destroy();
    this.#register && (QuickTipManager.register = this.#register);
    this.#getQuickTip && (QuickTipManager.getQuickTip = this.#getQuickTip);
  }

  //noinspection JSUnusedGlobalSymbols
  async testDisableStateWhenNoInherit(): Promise<void> {
    //open the grid with the content inherit=false
    this.#createTestling("content/200");

    await this.#waitForInheritButtonVisible();
    await this.#waitForInheritButtonUnpressed();
    await this.#waitForGridWritable();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonEnabled();
    await this.#waitRemoveButtonNotHidden();

    this.#forceReadOnly();

    await this.#waitForInheritButtonDisabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonHidden();

    this.#forceWritable();

    await this.#waitForInheritButtonEnabled();
    await this.#waitForGridWritable();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonEnabled();

    this.#makeContentReadOnly();

    await this.#waitForInheritButtonDisabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonHidden();

    this.#makeContentWritable();

    await this.#waitForInheritButtonEnabled();
    await this.#waitForGridWritable();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonEnabled();
  }

  //noinspection JSUnusedGlobalSymbols
  async testDisableStateWhenInherit(): Promise<void> {
    //open the grid with the content inherit=true
    this.#createTestling("content/202");

    await this.#waitForInheritButtonVisible();
    await this.#waitForInheritButtonPressed();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonDisabled();

    this.#forceReadOnly();

    await this.#waitForInheritButtonDisabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonDisabled();

    this.#forceWritable();

    await this.#waitForInheritButtonEnabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonDisabled();

    this.#makeContentReadOnly();

    await this.#waitForInheritButtonDisabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonDisabled();

    this.#makeContentWritable();

    await this.#waitForInheritButtonEnabled();
    await this.#waitForGridReadonly();
    await this.#openContextMenu();
    await this.#waitContextMenuOpened();
    await this.#waitRemoveButtonDisabled();
  }

  //noinspection JSUnusedGlobalSymbols
  async testInheritAction(): Promise<void> {
    this.#inheritExpression = ValueExpressionFactory.createFromValue(true);
    const originalList = ["A", "B"];
    this.#referencesExpression = ValueExpressionFactory.createFromValue(originalList);
    const originReferencesExpression = ValueExpressionFactory.createFromValue(originalList);
    this.#inheritAction = new InheritReferencesAction(
      Config(InheritReferencesAction, {
        bindTo: ValueExpressionFactory.createFromValue(),
        inheritExpression: this.#inheritExpression,
        referencesExpression: this.#referencesExpression,
        originReferencesExpression: originReferencesExpression,
      }));

    const changedList = ["C", "D"];
    this.#toggleInheritAction();
    await this.#waitInheritFalse(); //inherit is now false: we can edit the list manually
    this.#changeProductList(changedList);
    this.#toggleInheritAction();
    await this.#waitInheritTrue(); //inherit is now true: the list must be original again
    await this.#waitProductListEqual(originalList);
    this.#toggleInheritAction();
    await this.#waitInheritFalse(); //inherit is now false the list must be the previously changed one
    await this.#waitProductListEqual(changedList);
  }

  #toggleInheritAction(): void {
    // Toggle inherit:
    this.#inheritAction.execute();
  }

  #changeProductList(list: Array<any>): void {
    // Change the product list to <list>:
    this.#referencesExpression.setValue(list);
  }

  async #waitInheritFalse(): Promise<void> {
    // Wait Inherit False:
    await waitUntil((): boolean =>
      !this.#inheritExpression.getValue(),
    );
  }

  async #waitInheritTrue(): Promise<void> {
    // Wait Inherit True:
    await waitUntil((): boolean =>
      this.#inheritExpression.getValue());
  }

  async #waitProductListEqual(list: Array<any>): Promise<void> {
    // Wait for the Product list to be equal to <list>:
    await waitUntil((): boolean =>
      this.#referencesExpression.getValue() === list);
  }

  async #waitForGridReadonly(): Promise<void> {
    // Wait for grid is read-only:
    await waitUntil((): boolean =>
      this.#myCatalogLink && this.#isReadOnly(this.#myCatalogLink));
  }

  async #waitForGridWritable(): Promise<void> {
    // Wait for grid is writable:
    await waitUntil((): boolean =>
      this.#myCatalogLink && !this.#isReadOnly(this.#myCatalogLink),
    );
  }

  #isReadOnly(link: CatalogLinkPropertyField): boolean {
    return cast(ReadOnlyStateMixin, link.getView()).readOnly;
  }

  #createTestling(path: string): void {
    // Create the testling:
    this.#setBindTo(path);
    const conf = Config(InheritReferencesTestView);
    conf.bindTo = this.#bindTo;
    conf.forceReadOnlyValueExpression = this.#forceReadOnlyValueExpression;
    this.#viewport = new InheritReferencesTestView(conf);
    this.#myCatalogLink = this.#findCatalogLink();
  }

  async #waitForInheritButtonVisible(): Promise<void> {
    // Wait for the inherit button to be visible:
    await waitUntil((): boolean =>
      this.#findInheritButton(),
    );
  }

  async #waitForInheritButtonUnpressed(): Promise<void> {
    // Wait for the inherit button to be unpressed:
    await waitUntil((): boolean =>
      !this.#inheritButton.pressed);
  }

  async #waitForInheritButtonPressed(): Promise<void> {
    // Wait for the inherit button to be pressed:
    await waitUntil((): boolean =>
      this.#inheritButton.pressed);
  }

  #forceReadOnly(): void {
    // Force to read only:
    this.#forceReadOnlyValueExpression.setValue(true);
  }

  #forceWritable(): void {
    // Force to writable:
    this.#forceReadOnlyValueExpression.setValue(false);
  }

  #makeContentReadOnly(): void {
    // Make Content read only:
    this.#contentReadOnlyExpression.setValue(true);
  }

  #makeContentWritable(): void {
    // Make Content writable:
    this.#contentReadOnlyExpression.setValue(false);
  }

  async #waitForInheritButtonDisabled(): Promise<void> {
    // Wait for the inherit button to be disabled:
    await waitUntil((): boolean =>
      this.#inheritButton.disabled);
  }

  async #waitForInheritButtonEnabled(): Promise<void> {
    // Wait for the inherit button to be enabled:
    await waitUntil((): boolean =>
      !this.#inheritButton.disabled);
  }

  #findInheritButton(): boolean {
    this.#inheritButton = as(ComponentManager.getAll().filter((component: Component): boolean =>
      component.isXType(InheritReferencesButton.xtype),
    )[0], InheritReferencesButton);

    return this.#inheritButton && this.#inheritButton.isVisible(true);
  }

  #findCatalogLink(): CatalogLinkPropertyField {
    this.#myCatalogLink = as(ComponentManager.getAll().filter((component: Component): boolean =>
      component.isXType(CatalogLinkPropertyField.xtype),
    )[0], CatalogLinkPropertyField);
    this.#removeButton = as(this.#myCatalogLink.getTopToolbar().queryById(ECommerceStudioPlugin.REMOVE_LINK_BUTTON_ITEM_ID), Button);
    return this.#myCatalogLink;
  }

  async #openContextMenu(): Promise<void> {
    // open Context Menu:
    await waitUntil((): boolean =>
    //wait for the list filled
      asConfig(this.#myCatalogLink.getStore()).data.length > 0);
    const event: Record<string, any> = {
      getXY: (): Array<any> =>
        TableUtil.getCell(this.#myCatalogLink, 0, 1).getXY()
      ,
      preventDefault: (): void => {
        //do nothing
      },
      getTarget: (): HTMLElement =>
        TableUtil.getCellAsDom(this.#myCatalogLink, 0, 1)
      ,
      type: ContextMenuEventAdapter.EVENT_NAME,
    };
    this.#myCatalogLink.fireEvent("rowcontextmenu", this.#myCatalogLink, null, null, 0, event);
  }

  async #waitRemoveButtonDisabled(): Promise<void> {
    // Wait remove button disabled:
    await waitUntil((): boolean =>
      this.#removeButton.disabled);
  }

  async #waitRemoveButtonHidden(): Promise<void> {
    // Wait remove button hidden:
    await waitUntil((): boolean =>
      this.#removeButton.hidden);
  }

  async #waitRemoveButtonNotHidden(): Promise<void> {
    // Wait remove button disabled:
    await waitUntil((): boolean =>
      !this.#removeButton.hidden);
  }

  async #waitRemoveButtonEnabled(): Promise<void> {
    // Wait remove button enabled:
    await waitUntil((): boolean =>
      !this.#removeButton.disabled);
  }

  async #waitContextMenuOpened(): Promise<void> {
    // Wait context menu opened:
    await waitUntil((): boolean =>
      !!this.#findContextMenu());
  }

  #findContextMenu(): CatalogLinkContextMenu {
    const contextMenu = as(ComponentManager.getAll().filter((component: Component): boolean =>
      !component.up() && !component.hidden && component.isXType(CatalogLinkContextMenu.xtype),
    )[0], CatalogLinkContextMenu);
    if (contextMenu) {
      this.#removeMenuItem = as(contextMenu.getComponent(ECommerceStudioPlugin.REMOVE_LINK_MENU_ITEM_ID), Item);
    }

    return contextMenu;
  }

}

export default InheritReferencesTest;
