import session from "@coremedia/studio-client.cap-rest-client/common/session";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import createComponentSelector from "@coremedia/studio-client.ext.ui-components/util/createComponentSelector";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import StringPropertyField
  from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import MessageBoxUtil from "@coremedia/studio-client.main.editor-components/sdk/util/MessageBoxUtil";
import Ext from "@jangaroo/ext-ts";
import Component from "@jangaroo/ext-ts/Component";
import StringUtil from "@jangaroo/ext-ts/String";
import Button from "@jangaroo/ext-ts/button/Button";
import Container from "@jangaroo/ext-ts/container/Container";
import TextField from "@jangaroo/ext-ts/form/field/Text";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import TabPanel from "@jangaroo/ext-ts/tab/Panel";
import MessageBoxWindow from "@jangaroo/ext-ts/window/MessageBox";
import { as, asConfig, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import int from "@jangaroo/runtime/int";
import { AnyFunction } from "@jangaroo/runtime/types";
import TaxonomyStudioPluginSettings_properties from "../../TaxonomyStudioPluginSettings_properties";
import TaxonomyNode from "../TaxonomyNode";
import TaxonomyNodeFactory from "../TaxonomyNodeFactory";
import TaxonomyNodeList from "../TaxonomyNodeList";
import TaxonomyStudioPlugin_properties from "../TaxonomyStudioPlugin_properties";
import TaxonomyUtil from "../TaxonomyUtil";
import TaxonomySearchField from "../selection/TaxonomySearchField";
import TaxonomyEditor from "./TaxonomyEditor";
import TaxonomyExplorerColumn from "./TaxonomyExplorerColumn";
import TaxonomyExplorerPanel from "./TaxonomyExplorerPanel";
import TaxonomyStandAloneDocumentView from "./TaxonomyStandAloneDocumentView";
import TaxonomyL10nUtil from "../l10n/TaxonomyL10nUtil";
import joo from "@jangaroo/runtime/joo";
import sitesService from "@coremedia/studio-client.multi-site-models/global/sitesService";

interface TaxonomyExplorerPanelBaseConfig extends Config<Panel> {
}

/**
 * Handles the adding and removing of the level columns.
 */
class TaxonomyExplorerPanelBase extends Panel {
  declare Config: TaxonomyExplorerPanelBaseConfig;

  #selectedValueExpression: ValueExpression = null;

  #displayedTaxonomyContentExpression: ValueExpression = null;

  #displayedTaxonomyNodeExpression: ValueExpression = null;

  #siteSelectionExpression: ValueExpression = null;

  #searchResultExpression: ValueExpression = null;

  #forceReadOnleValueExpression: ValueExpression = null;

  #columnsContainer: Container = null;

  #clipboardValueExpression: ValueExpression = null;

  #nodeNameDirtyExpression: ValueExpression<boolean>;

  #currentNodeChangeExpr: ValueExpression;

  constructor(config: Config<TaxonomyExplorerPanel> = null) {
    super(config);
    this.#searchResultExpression = config.searchResultExpression;
    this.#siteSelectionExpression = config.siteSelectionExpression;

    this.#searchResultExpression.addChangeListener(bind(this, this.#searchSelectionChanged));
    this.#siteSelectionExpression.addChangeListener(bind(this, this.#siteSelectionChanged));
  }

  protected override afterRender(): void {
    super.afterRender();
    const formDispatcher = TaxonomyExplorerPanelBase.#getDocumentForm().getTabbedDocumentFormDispatcher();
    formDispatcher.bindTo.addChangeListener(bind(this, this.#updateTabs));
  }

  getSelectedValueExpression(): ValueExpression {
    if (!this.#selectedValueExpression) {
      const selectedValuesBean = beanFactory._.createLocalBean();
      this.#selectedValueExpression = ValueExpressionFactory.create("value", selectedValuesBean);
      this.#selectedValueExpression.setValue([]);
      this.#selectedValueExpression.addChangeListener(bind(this, this.#selectedNodeChanged));
    }
    return this.#selectedValueExpression;
  }

  protected getClipboardValueExpression(): ValueExpression {
    if (!this.#clipboardValueExpression) {
      const selectedValuesBean = beanFactory._.createLocalBean();
      this.#clipboardValueExpression = ValueExpressionFactory.create("copy", selectedValuesBean);
    }
    return this.#clipboardValueExpression;
  }

  getDisplayedTaxonomyContentExpression(): ValueExpression {
    if (!this.#displayedTaxonomyContentExpression) {
      const bean = beanFactory._.createLocalBean();
      this.#displayedTaxonomyContentExpression = ValueExpressionFactory.create("content", bean);
    }
    return this.#displayedTaxonomyContentExpression;
  }

  getDisplayedTaxonomyNodeExpression(): ValueExpression {
    if (!this.#displayedTaxonomyNodeExpression) {
      this.#displayedTaxonomyNodeExpression = ValueExpressionFactory.create("node", beanFactory._.createLocalBean());
    }
    return this.#displayedTaxonomyNodeExpression;
  }

  protected getForceReadOnlyValueExpression(): ValueExpression {
    if (!this.#forceReadOnleValueExpression) {
      this.#forceReadOnleValueExpression = ValueExpressionFactory.createFromValue(false);
    }
    return this.#forceReadOnleValueExpression;
  }

  /**
   * Handler implementation of the 'Add child node' button.
   */
  protected createChildNode(): void {
    this.setBusy(true);
    const selections: Array<any> = this.getSelectedValueExpression().getValue();
    const parent: TaxonomyNode = selections[0];
    parent.createChild((newChild: TaxonomyNode): void =>
      parent.invalidate((): void => {
        this.#refreshNode(parent);
        this.updateColumns(parent);

        this.getSelectedValueExpression().setValue([newChild]);

        //callback is called after the grid selection was made
        this.#selectNode(newChild, (): void =>
          this.#waitForDocumentForm(newChild, (): void => {
            this.updateTaxonomyNodeForm(newChild, true);
            // Preset location latitude/longitude for location taxonomy nodes
            this.#setInitialLocation(newChild, parent);
          }),
        );
      }),
    );
  }

  /**
   * Ensures that the content form for the given taxonomy node has been rendered.
   * @param newChild the taxonomy child that has been selected/created
   * @param callback the callback method to invoke afterwards, no parameters required
   */
  #waitForDocumentForm(newChild: TaxonomyNode, callback: AnyFunction): void {
    const bindTo = this.getDisplayedTaxonomyContentExpression();
    const restId = TaxonomyUtil.parseRestId(bindTo.getValue());
    if (newChild.getRef() === restId) {
      callback.call(null);
    } else {
      EventUtil.invokeLater((): void =>
        this.#waitForDocumentForm(newChild, callback),
      );
    }
  }

  /**
   * Handler implementation of the delete button.
   */
  deleteNodes(): void {
    const selection: Array<any> = this.getSelectedValueExpression().getValue();

    //check if the given node is deleteable at all
    TaxonomyUtil.bulkStrongLinks(selection, (result: Array<any>): void => {
      if (result.length > 0) {
        const title = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_title;
        let msg = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_blocked_text;
        msg = StringUtil.format(msg, result.length);
        MessageBoxUtil.showError(title, msg);
        return;
      }

      //next check referrers
      TaxonomyUtil.bulkLinks(selection, (result: Array<any>): void =>
        this.#doDeletion(selection, result),
      );
    });
  }

  #doDeletion(selection: Array<any>, referrers: Array<any>): void {
    let message = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_text;
    let icon: string = MessageBoxWindow.INFO;
    if (referrers.length > 0) {
      message = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_text_referrer_warning;
      message = StringUtil.format(message, referrers.length);
      icon = MessageBoxWindow.ERROR;
    }

    MessageBoxWindow.getInstance().show({
      title: TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_title,
      msg: message,
      icon: icon,
      minWidth: 300,
      buttons: MessageBoxWindow.OKCANCEL,
      fn: (btn: any): void => {
        if (btn === "ok") {
          this.setBusy(true);
          TaxonomyUtil.bulkDelete(selection, (parent: TaxonomyNode): void => {
            for (const sel of selection as TaxonomyNode[]) {
              const parentContent = session._.getConnection().getContentRepository().getContent(sel.getRef()).getParent();
              if (parentContent) {
                parentContent.invalidate();
              }
            }

            //checks if return value is defined, otherwise the node could not be deleted.
            if (parent) {
              parent.invalidate((): void => { //reload the inner content too!!
                const parentCC = this.getColumnContainer(parent);
                if (parentCC) {
                  parentCC.updateNode(parent);
                  this.updateColumns(parent);
                }
                this.getSelectedValueExpression().setValue([parent]);
              });
            } else {
              this.setBusy(false);
              const msg = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_failed_text;
              MessageBoxWindow.getInstance().alert(TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_failed_title, msg);
            }

            this.setBusy(false);
          });
        }
      },
    });
  }

  /**
   * Reloads this panel, applies for site selection changes
   * and reload taxonomies action.
   */
  protected reload(): void {
    this.setBusy(true);
    this.#selectedValueExpression.setValue([]);
    this.#getRootColumnPanel().reload();
    this.setBusy(false);
  }

  /**
   * Sets initial latitude/longitude value from parent if the node type is 'CMLocTaxonomy'.
   */
  #setInitialLocation(taxonomyNode: TaxonomyNode, parentNode: TaxonomyNode): void {
    const parentNodeContent = as(beanFactory._.getRemoteBean(parentNode.getRef()), Content);
    const newNodeContent = as(beanFactory._.getRemoteBean(taxonomyNode.getRef()), Content);

    newNodeContent.load((): void => {
      if (newNodeContent.getType().isSubtypeOf(TaxonomyStudioPluginSettings_properties.taxonomy_location_doctype)) {
        const property = TaxonomyStudioPluginSettings_properties.taxonomy_location_latLong_property_name;
        const parentLocation = ValueExpressionFactory.create(property, parentNodeContent);
        const newContentLocation = ValueExpressionFactory.create(property, newNodeContent);

        if (parentNodeContent.isFolder()) {
          newContentLocation.setValue(TaxonomyStudioPluginSettings_properties.taxonomy_location_default_value);
        } else {
          parentLocation.loadValue((location: string): void =>
            newContentLocation.loadValue((): void => {
              newContentLocation.setValue(location);
            }),
          );
        }
      }
    });
  }

  /**
   * Fired when the user has selected a search result. All column panels are removed then,
   * the new column tree is rebuild and the leaf selected.
   */
  #searchSelectionChanged(): void {
    const list: TaxonomyNodeList = this.#searchResultExpression.getValue();
    if (list) {
      this.#selectNodePath(list);
    }
  }

  /**
   * Shows the columns, selection and leaf formular for the given node list.
   * @param list
   */
  #selectNodePath(list: TaxonomyNodeList): void {
    this.setBusy(true);
    const cc = this.getColumnsContainer();
    cc.removeAll(true);

    const nodes = list.getNodes();
    for (let i = 0; i < nodes.length; i++) {
      const node: TaxonomyNode = nodes[i];
      if (i === 0) {
        this.#getRootColumnPanel().selectNode(node);
      } else {
        this.#addColumn(nodes[i - 1]);
        if (i === nodes.length - 1) {
          this.#selectedValueExpression.setValue([node]);
        }
      }
    }
    for (let j = 1; j < nodes.length; j++) {
      const selectNode: TaxonomyNode = nodes[j];
      const column = this.getColumnContainer(selectNode);
      if (column) {
        column.selectNode(selectNode);
      }
    }
  }

  /**
   * Returns the panel that contains the root taxonomy nodes.
   * @return The taxonomy grid panel.
   */
  #getRootColumnPanel(): TaxonomyExplorerColumn {
    const rootContainer = as(this.queryById("taxonomyRootsColumn"), TaxonomyExplorerColumn);
    return rootContainer;
  }

  /**
   * Notifies a change in the active selection. If the value expression is empty,
   * the view is set to the root nodes.
   */
  #selectedNodeChanged(): void {
    this.#commitTaxonomyNodeForm(() => {
      const selection: Array<any> = this.#selectedValueExpression.getValue();
      this.#changeSelectedNode(selection);

      //only remove the tabs for the root nodes, all others have a tabbed form
      if (selection.length === 0 || selection[0].isRoot()) {
        TaxonomyExplorerPanelBase.#clearTabs();
      }
    });

  }

  /**
   * Updates the ui depending on the selected node.
   */
  #changeSelectedNode(newNodes: Array<any>): void {
    if (newNodes.length > 0) {
      const newNode: TaxonomyNode = newNodes[0];
      TaxonomyUtil.setLatestSelection(newNode);
      this.#updateActions(newNode);
      this.updateTaxonomyNodeForm(newNode, false);
      this.updateColumns(newNode);
    } else {
      this.#getRootColumnPanel().selectNode(null);
      this.getColumnsContainer().removeAll(true);
    }
  }

  /**
   * Enabling/Disabling of the toolbar buttons.
   * @param newNode The selected node.
   */
  #updateActions(newNode: TaxonomyNode): void {
    const addButton = as(this.queryById("add"), Button);
    addButton.setDisabled(!newNode || !newNode.isExtendable());//disable add button if node is not extendable

    const deleteButton = as(this.queryById("delete"), Button);
    deleteButton.setDisabled(!newNode || newNode.isRoot());
  }

  /**
   * Updates after the selected node has been changed. The content form dispatcher is updated afterwards
   * with the selected content or hidden, if no content is selected.
   * @param node The node to display the Content Form Dispatcher for.
   * @param titleFocus True to focus the title field after selection
   */
  updateTaxonomyNodeForm(node: TaxonomyNode, titleFocus: boolean): void {
    this.setBusy(true);
    this.getDisplayedTaxonomyNodeExpression().setValue(node);

    this.#currentNodeChangeExpr && this.#currentNodeChangeExpr.removeChangeListener(bind(this, this.#selectedTaxonomyNameChanged));

    const dfd = as(this.queryById("documentFormDispatcher"), Container);
    if (node && !node.isRoot()) {
      const content = session._.getConnection().getContentRepository().getContent(node.getRef());
      if (content) {//null after content deletion
        editorContext._.getApplicationContext().set("taxonomy_node_level", node.getLevel());
        content.invalidate((): void => {
          Ext.suspendLayouts();
          this.#createNodePathExpression(content).loadValue(propertyPath => {
            this.#currentNodeChangeExpr = ValueExpressionFactory.create(propertyPath, content);
            this.#currentNodeChangeExpr.addChangeListener(bind(this, this.#selectedTaxonomyNameChanged));
            this.getDisplayedTaxonomyContentExpression().setValue(content);
            dfd.show();
            this.setBusy(false);

            this.#ensureExpandState(content);
            if (titleFocus) {
              this.#focusTitle();
            }
            Ext.resumeLayouts(true);
          });
        });
      } else {
        dfd.hide();
        this.setBusy(false);
      }
    } else {
      //hide the content dispatcher panel!
      dfd.hide();
      this.setBusy(false);
    }
  }

  /**
   * Calculates the name change property that is responsible for updating the name of the selected
   * taxonomy in the visible explorer column.
   * Depending on the l10n settings, the actual value has to be calculated.
   *
   * @param content the content to calculate the name path for
   */
  #createNodePathExpression(content: Content): ValueExpression<string> {
    return ValueExpressionFactory.createFromFunction(() => {
      const l10nEnabled = TaxonomyL10nUtil.isL10NEnabledExpression().getValue();
      if (l10nEnabled === undefined) {
        return undefined;
      }

      const studioLocale = joo.localeSupport.getLocale();
      const defaultLanguage = TaxonomyL10nUtil.getDefaultLanguageExpression().getValue();
      const site = sitesService._.getSiteFor(content);
      let propertyName = "properties." + TaxonomyStudioPluginSettings_properties.taxonomy_display_property;

      //l10n must be enabled AND the taxonomy node must be global AND the display value is located inside the struct
      //if the studioLocale matches the defaultValue, we will display the "value" property which is the default.
      if (l10nEnabled && !site && studioLocale !== defaultLanguage) {
        propertyName = "properties." + TaxonomyStudioPluginSettings_properties.taxonomy_l10n_property + "." + studioLocale;
      }
      return propertyName;
    });
  }

  #ensureExpandState(content: Content): void {
    const dfd = as(this.queryById("documentFormDispatcher"), Container);
    EventUtil.invokeLater((): void => {
      const collapsable = as(dfd.query(createComponentSelector()._xtype(PropertyFieldGroup.xtype).build())[0], Panel);
      if (collapsable && asConfig(collapsable).collapsed) {
        collapsable.expand(false);
      }
    });
  }

  #focusTitle(): void {
    const dfd = as(this.queryById("documentFormDispatcher"), Container);
    const collapsable = as(dfd.query(createComponentSelector()._xtype(PropertyFieldGroup.xtype).build())[0], Panel);
    const fieldId = TaxonomyStudioPluginSettings_properties.taxonomy_display_property;
    const stringPropertyFields: Array<any> = collapsable.query(createComponentSelector()._xtype(StringPropertyField.xtype).build());

    for (const field of stringPropertyFields as StringPropertyField[]) {
      if (field.propertyName === fieldId) {
        const nameField = as(field.query(createComponentSelector()._xtype("textfield").build())[0], TextField);
        nameField.selectOnFocus = true;
        nameField.focus(true);
        return;
      }
    }
  }

  /**
   * Fired for the name change of the current selected node, updates
   * the corresponding column afterwards.
   */
  #selectedTaxonomyNameChanged(contentDisplayNameVE: ValueExpression): void {
    const newName: string = contentDisplayNameVE.getValue();
    //A regular reload is fired once the selected node changes. So we only have too
    //update the node name without reloading the complete node.
    const selections: Array<any> = this.getSelectedValueExpression().getValue();
    const node: TaxonomyNode = selections[0];
    if (node && node.getName() !== newName && newName && newName.length > 0) {
      node.setName(newName);
      const column = this.getColumnContainer(node);
      column.updateNode(node);

      this.#getNodeNameDirtyExpression().setValue(true);
    }
  }

  /**
   * Commits the changes on a node and refreshes the UI afterwards.
   */
  #commitTaxonomyNodeForm(callback: () => void): void {
    const node = as(this.getDisplayedTaxonomyNodeExpression().getValue(), TaxonomyNode);
    const content = as(this.getDisplayedTaxonomyContentExpression().getValue(), Content);

    if (node && !node.isRoot() && (!content.isCheckedOut() || content.isCheckedOutByCurrentSession())) {
      const nodeColumn = this.getColumnContainer(node);
      node.commitNode((): void =>
        content.invalidate((): void => {
          this.#refreshNode(node);
          if (this.#getNodeNameDirtyExpression().getValue()) {
            this.#getNodeNameDirtyExpression().setValue(false);
            nodeColumn && nodeColumn.parentNode.loadChildren(true, () => {});
          }
          callback();
        }),
      );
    } else {
      callback();
    }
  }

  /**
   * Executes the column update (adding/removing) depending on the type of the node.
   * @param node The selected node.
   */
  updateColumns(node: TaxonomyNode): void {
    const cc = this.getColumnsContainer();
    if (node) {
      const level = node.getLevel();
      if (cc.items.length > level) {
        const columnsToBeRemoved = cc.items.getRange(level);
        columnsToBeRemoved.forEach((column: TaxonomyExplorerColumn): void => {
          cc.remove(column, true);
        });
      }
      if (node.isExtendable() && !node.isLeaf()) {
        this.#addColumn(node);
      }
    } else {
      cc.removeAll(true);
    }
  }

  /**
   * Adds a new column for the given node.
   * @param node The node the column should be created for.
   */
  #addColumn(node: TaxonomyNode): void {
    const cc = this.getColumnsContainer();
    const column = new TaxonomyExplorerColumn(Config(TaxonomyExplorerColumn, {
      parentNode: node,
      itemId: "taxonomyColumn-" + node.getLevel(),
      clipboardValueExpression: this.getClipboardValueExpression(),
      selectedNodeExpression: this.#selectedValueExpression,
      siteSelectionExpression: this.#siteSelectionExpression,
    }));
    column.addListener("afterrender", bind(this, this.#scrollRight));
    cc.add(column);
  }

  #scrollTop(): void {
    //remove listener...
    const lastColumn = as(this.#columnsContainer.items["items"][this.#columnsContainer.items.length - 1], TaxonomyExplorerColumn);
    if (lastColumn && lastColumn.isInstance) {
      lastColumn.removeListener("afterrender", bind(this, this.#scrollRight));
    }
    //and scroll right
    if (this.#columnsContainer.el) {
      const columnsContainerDom: any = this.#columnsContainer.el.dom;
      columnsContainerDom.parentElement.scrollTop = 0;
      columnsContainerDom.scrollTop = 0;
    }
  }

  #scrollRight(): void {
    EventUtil.invokeLater((): void => {
      const columns = this.getColumnsContainer();
      if (columns.el.dom.childNodes[0]) {
        columns.el.dom.scrollLeft = 10000;
      }
      this.#scrollTop();
    });
  }

  /**
   * Moves the source node to the target node and updates the UI.
   * @param sourceNodes
   * @param targetNode
   */
  moveNodes(sourceNodes: Array<any>, targetNode: TaxonomyNode): void {
    this.setBusy(true);
    this.#clipboardValueExpression.setValue([]);

    TaxonomyUtil.bulkMove(sourceNodes, targetNode, (result: TaxonomyNodeList): void => {
      targetNode.invalidate((): void => {
        this.getColumnContainer(targetNode).updateNode(targetNode);

        for (const updatedNode of result.getNodes() as TaxonomyNode[]) {
          TaxonomyNodeFactory.loadPath(updatedNode.getTaxonomyId(), updatedNode.getRef(), updatedNode.getSite(),
            (nodeList: TaxonomyNodeList): void => {
              const taxonomyAdminTab = as(Ext.getCmp("taxonomyEditor"), TaxonomyEditor);
              taxonomyAdminTab.selectNode(nodeList);
            });
          //only select first node of moved set
          break;
        }
      });
      this.setBusy(false);
    });
  }

  /**
   * Returns the container that contains the dynamic explorer columns.
   * @return
   */
  getColumnsContainer(): Container {
    if (!this.#columnsContainer) {
      this.#columnsContainer = as(this.queryById("columnsContainer"), Container);
    }
    return this.#columnsContainer;
  }

  /**
   * Updates the ext record of the given node, so that the reformatting is triggered.
   * @param node The node to reformat.
   */
  #refreshNode(node: TaxonomyNode): void {
    //refresh given node entry (maybe after an update) of a visible column list.
    const nodesColumn = this.getColumnContainer(node);
    if (nodesColumn) {
      nodesColumn.updateNode(node);
    }
  }

  /**
   * Returns true if the node with the given id is marked for copying.
   * Another style will be applied then to mark this node in the column.
   * @param id
   * @return
   */
  isMarkedForCopying(id: string): boolean {
    const selection: Array<any> = this.getClipboardValueExpression().getValue();
    if (selection) {
      for (const node of selection as TaxonomyNode[]) {
        if (node.getRef() === id) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Selects the ext record of the given node.
   * @param node The node to select.
   * @param callback optional callback method
   */
  #selectNode(node: TaxonomyNode, callback?: AnyFunction): void {
    //refresh given node entry (maybe after an update) of a visible column list.
    const nodesColumn = this.getColumnContainer(node);
    if (nodesColumn) {
      nodesColumn.selectNode(node, callback);
    }
  }

  /**
   * Returns the column container in which the given taxonomy node is located.
   * @param node The taxonomy node to retrieve the column from.
   * @return The column grid panel that contains the node.
   */
  getColumnContainer(node: TaxonomyNode): TaxonomyExplorerColumn {
    const position: int = node.getLevel() - 1;
    return this.getColumnContainerForLevel(position);
  }

  /**
   * Returns the columns container for the given level.
   * @param position The level, starting from -1 which is the root column.
   * @return
   */
  getColumnContainerForLevel(position: int): TaxonomyExplorerColumn {
    if (position === -1) {
      return as(Ext.getCmp("taxonomyRootsColumn"), TaxonomyExplorerColumn);
    }
    return as(this.#columnsContainer.items.getAt(position), TaxonomyExplorerColumn);
  }

  /**
   * Sets the column panels to disabled while REST operations are in place.
   * @param b If true, the panel is disabled.
   */
  setBusy(b: boolean): void {
    this.setDisabled(b);
    this.getForceReadOnlyValueExpression().setValue(b);
  }

  /**
   * Delegates the tab selection event to the tabbed content form dispatcher and selected
   * the hidden tab in the form.
   * @param tabPanel the artificial visible tab panel
   */
  static #onTabChange(tabPanel: TabPanel): void {
    const formDispatcher = TaxonomyExplorerPanelBase.#getDocumentForm().getTabbedDocumentFormDispatcher();
    const documentTabPanel = as(formDispatcher.getActiveItem(), DocumentTabPanel);
    if (documentTabPanel) {
      documentTabPanel.setActiveTab(tabPanel.getActiveTab().getItemId());
    }
  }

  static #clearTabs(): void {
    // Clear the list of tabs. New tabs will be created soon.
    const tabs = TaxonomyExplorerPanelBase.#getTabs();
    tabs.removeAll(true);
  }

  /**
   * Executes on node change:
   * updates the tabs by reading the tab information from the tabbed content form dispatcher.
   */
  #updateTabs(): void {
    TaxonomyExplorerPanelBase.#getTabs().removeListener("tabchange", TaxonomyExplorerPanelBase.#onTabChange);
    TaxonomyExplorerPanelBase.#clearTabs();

    //add tabs
    const tabs = TaxonomyExplorerPanelBase.#getTabs();
    const formDispatcher = TaxonomyExplorerPanelBase.#getDocumentForm().getTabbedDocumentFormDispatcher();
    const documentTabPanel = as(formDispatcher.getActiveItem(), DocumentTabPanel);
    if (documentTabPanel) {
      documentTabPanel.items.each((item: Component): void => {
        const title = as(item["title"], String);
        if (title) {
          const tab = Config(Panel);
          tab.itemId = item.getItemId();
          tab.title = title;
          tabs.add(tab);
        }
      });
      tabs.setActiveTab(documentTabPanel.getActiveTab().getItemId());
    }

    TaxonomyExplorerPanelBase.#getTabs().addListener("tabchange", TaxonomyExplorerPanelBase.#onTabChange);
  }

  static #getTabs(): TabPanel {
    return as(Ext.getCmp("taxonomyTabs"), TabPanel);
  }

  /**
   * Fired if the site selection combo has been changed.
   */
  #siteSelectionChanged(): void {
    const container = as(Ext.getCmp("taxonomySearchField"), TaxonomySearchField);
    container.setValue("");
    this.#getRootColumnPanel().initColumn();
  }

  #getNodeNameDirtyExpression(): ValueExpression {
    if (!this.#nodeNameDirtyExpression) {
      this.#nodeNameDirtyExpression = ValueExpressionFactory.createFromValue(false);
    }
    return this.#nodeNameDirtyExpression;
  }

  static #getDocumentForm(): TaxonomyStandAloneDocumentView {
    return as(Ext.getCmp(TaxonomyExplorerPanel.TAXONOMY_DOCUMENT_VIEW_ITEM_ID), TaxonomyStandAloneDocumentView);
  }
}

export default TaxonomyExplorerPanelBase;
