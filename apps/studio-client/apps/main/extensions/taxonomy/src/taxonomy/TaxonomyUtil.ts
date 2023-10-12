import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ContentPropertyNames from "@coremedia/studio-client.cap-rest-client/content/ContentPropertyNames";
import RemoteServiceMethod from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethod";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import BeanRecord from "@coremedia/studio-client.ext.ui-components/store/BeanRecord";
import EditorMainView from "@coremedia/studio-client.main.editor-components/sdk/desktop/EditorMainView";
import WorkArea from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkArea";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import MessageBoxUtil from "@coremedia/studio-client.main.editor-components/sdk/util/MessageBoxUtil";
import Ext from "@jangaroo/ext-ts";
import StringUtil from "@jangaroo/ext-ts/String";
import { as, is } from "@jangaroo/runtime";
import trace from "@jangaroo/runtime/trace";
import { AnyFunction } from "@jangaroo/runtime/types";
import TaxonomyNode from "./TaxonomyNode";
import TaxonomyNodeFactory from "./TaxonomyNodeFactory";
import TaxonomyNodeList from "./TaxonomyNodeList";
import TaxonomyStudioPlugin_properties from "./TaxonomyStudioPlugin_properties";

/**
 * Common utility methods for taxonomies.
 */
class TaxonomyUtil {
  static readonly #TAXONOMY_SETTINGS: string = "TaxonomySettings";

  static #latestAdminSelection: TaxonomyNode = null;

  static getLatestSelection(): TaxonomyNode {
    return TaxonomyUtil.#latestAdminSelection;
  }

  static setLatestSelection(node: TaxonomyNode): void {
    TaxonomyUtil.#latestAdminSelection = node;
  }

  /**
   * Utility method that checks if the given node is already part of the active selection list.
   */
  static isInSelection(selection: Array<any>, ref: string): boolean {
    if (selection) {
      for (let i = 0; i < selection.length; i++) {
        const selectedContent: Content = selection[i];
        const restId = TaxonomyUtil.parseRestId(selectedContent);
        if (restId === ref) {
          return true;
        }
      }
    }
    return false;
  }

  static decodeHTML(xml: string): string {
    while (xml.indexOf("&nbsp;") !== -1) {
      xml = xml.replace("&nbsp;", " ");
    }
    return xml;
  }

  static escapeHTML(xml: string): string {
    while (xml.indexOf(">") !== -1) {
      xml = xml.replace(">", "&gt;");
    }
    while (xml.indexOf("<") !== -1) {
      xml = xml.replace("<", "&lt;");
    }
    while (xml.indexOf(" ") !== -1) {
      xml = xml.replace(" ", "&nbsp;");
    }
    return xml;
  }

  static getTaxonomyName(taxonomy: Content): string {
    const properties = taxonomy.getProperties();
    if (properties) {
      const value = as(properties.get("value"), String);
      if (value && value.length > 0) {
        return value;
      }
    }
    return taxonomy.getName();
  }

  /**
   * Invokes the callback function with true or false depending on if the taxonomy is editable or not.
   * @param taxonomyId The taxonomy id to check.
   * @param callback The callback handler.
   * @param content content used for checking if the editor is editable
   */
  static isEditable(taxonomyId: string, callback: AnyFunction, content?: Content): void {
    if (!content) {
      content = WorkArea.ACTIVE_CONTENT_VALUE_EXPRESSION.getValue();
    }
    if (!content) {
      callback.call(null, true);
    } else if (!content.getState().readable) {
      callback.call(null, false);
    } else {
      ValueExpressionFactory.create(ContentPropertyNames.PATH, content).loadValue((): void => {
        const siteId = editorContext._.getSitesService().getSiteIdFor(content);
        TaxonomyNodeFactory.loadTaxonomyRoot(siteId, taxonomyId, (parent: TaxonomyNode): void => {
          if (parent) {
            callback.call(null, true);
          } else {
            callback.call(null, false);
          }
        });
      });
    }
  }

  /**
   * Loads the path nodes for the given bean record (content).
   * @param record The record to load the path for.
   * @param content content used to determine the site specific taxonomy for the given path, may be null
   * @param taxonomyId The id of the taxonomy the record is located in.
   * @param callback The callback function the updated record is passed to or null if node does not exist.
   */
  static loadTaxonomyPath(record: BeanRecord, content: Content, taxonomyId: string, callback: AnyFunction): void {
    const bean = as(record.getBean(), Content);
    let siteId: string = null;
    if (content && is(content, Content)) {
      siteId = editorContext._.getSitesService().getSiteIdFor(content);
    }
    const url = "taxonomies/path?" + Ext.urlEncode({
      taxonomyId: taxonomyId,
      nodeRef: TaxonomyUtil.parseRestId(bean),
      site: siteId,
    });
    const taxRemoteBean = beanFactory._.getRemoteBean(url);
    taxRemoteBean.load((): void =>
      EventUtil.invokeLater((): void => {
        if (taxRemoteBean.get("path")) { //maybe not set if the taxonomy does not exist
          const nodes: Array<any> = taxRemoteBean.get("path")["nodes"];
          const leafNode = new TaxonomyNode(nodes[nodes.length - 1]);
          record.data.leafNode = leafNode;
          record.data.nodes = nodes;
          callback.call(null, record);
        } else {
          trace("[INFO]", "Taxonomy node " + bean + " does not exist anymore or is not readable.");
          callback.call(null, record);
        }
      }),
    );
  }

  /**
   * Bulk operation to move all the sources nodes to the given target node
   * @param sourceNodes
   * @param targetNode
   * @param callback
   */
  static bulkMove(sourceNodes: Array<any>, targetNode: TaxonomyNode, callback: AnyFunction): void {
    const url = "taxonomies/bulkmove";
    const bulkOperation = new RemoteServiceMethod(url, "POST");
    bulkOperation.request({
      "taxonomyId": targetNode.getTaxonomyId(),
      "site": targetNode.getSite(),
      "targetNodeRef": targetNode.getRef(),
      "nodeRefs": TaxonomyUtil.#getNodeRefs(sourceNodes),
    }, (result: any): void => {
      const resultList = new TaxonomyNodeList(result.getResponseJSON().nodes);
      callback.call(null, resultList);
    });
  }

  /**
   * Bulk operation to delete the given nodes
   * @param sourceNodes
   * @param callback
   */
  static bulkDelete(sourceNodes: Array<any>, callback: AnyFunction): void {
    Ext.getCmp(EditorMainView.ID).getEl().setStyle("cursor", "wait");
    const url = "taxonomies/bulkdelete";
    const bulkOperation = new RemoteServiceMethod(url, "POST");
    const node: TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      "taxonomyId": node.getTaxonomyId(),
      "site": node.getSite(),
      "nodeRefs": TaxonomyUtil.#getNodeRefs(sourceNodes),
    }, (result: any): void => {
      Ext.getCmp(EditorMainView.ID).getEl().setStyle("cursor", "default");
      const parentNode = new TaxonomyNode(result.getResponseJSON());
      callback.call(null, parentNode);
    }, (result: any): void => {
      Ext.getCmp(EditorMainView.ID).getEl().setStyle("cursor", "default");
      let message = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_failed_text;
      message = StringUtil.format(message, result.getResponseJSON());
      const title = TaxonomyStudioPlugin_properties.TaxonomyEditor_deletion_failed_title;
      MessageBoxUtil.showInfo(title, message);
    });
  }

  /**
   * Bulk operation to check all links to the given nodes
   * @param sourceNodes
   * @param callback called with an array of referred content
   */
  static bulkLinks(sourceNodes: Array<any>, callback: AnyFunction): void {
    const url = "taxonomies/bulklinks";
    const bulkOperation = new RemoteServiceMethod(url, "POST");
    const node: TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      "taxonomyId": node.getTaxonomyId(),
      "site": node.getSite(),
      "nodeRefs": TaxonomyUtil.#getNodeRefs(sourceNodes),
    }, (result: any): void => {
      callback.call(null, as(result.getResponseJSON().items, Array));
    });
  }

  /**
   * Bulk operation to check strong links of the given nodes
   * @param sourceNodes
   * @param callback called with an array of referred content
   */
  static bulkStrongLinks(sourceNodes: Array<any>, callback: AnyFunction): void {
    const url = "taxonomies/bulkstronglinks";
    const bulkOperation = new RemoteServiceMethod(url, "POST");
    const node: TaxonomyNode = sourceNodes[0];
    bulkOperation.request({
      "taxonomyId": node.getTaxonomyId(),
      "site": node.getSite(),
      "nodeRefs": TaxonomyUtil.#getNodeRefs(sourceNodes),
    }, (result: any): void => {
      callback.call(null, as(result.getResponseJSON().items, Array));
    });
  }

  /**
   * Helper method to join the ref values of the given nodes to a string
   * @param sourceNodes the node to join the refs for
   */
  static #getNodeRefs(sourceNodes: Array<any>): string {
    const result = [];
    for (const node of sourceNodes as TaxonomyNode[]) {
      result.push(node.getRef());
    }

    return result.join(",");
  }

  /**
   * Adds the content represented by the given node to the list of the
   * selection expression.
   * @param selectionExpression the current selection
   * @param contentId The id of the node to add to the selection.
   * @param callback An optional callback called after selection update
   */
  static addNodeToSelection(selectionExpression: ValueExpression, contentId: string, callback?: AnyFunction): void {
    let newSelection = [];

    const child = as(beanFactory._.getRemoteBean(contentId), Content);
    child.load((bean: Content): void => {
      newSelection.push(bean);
      const selection: Array<any> = selectionExpression.getValue();
      if (selection) {
        newSelection = selection.concat(newSelection);
      }
      selectionExpression.setValue(newSelection);

      if (callback) {
        callback(contentId);
      }
    });
  }

  /**
   * Removes the content represented by the given node from the list of the
   * selection expression.
   * @param selectionExpression the current selection
   * @param contentId The node to remove from the selection.
   * @param callback An optional callback called after selection update
   */
  static removeNodeFromSelection(selectionExpression: ValueExpression, contentId: string, callback?: AnyFunction): void {
    const selection: Array<any> = selectionExpression.getValue();
    const newSelection = [];
    if (selection) {
      for (let i = 0; i < selection.length; i++) {
        const selectedContent: Content = selection[i];
        const restId = TaxonomyUtil.parseRestId(selectedContent);
        if (restId === contentId) {
          continue;
        }
        newSelection.push(selectedContent);
      }
    }
    selectionExpression.setValue(newSelection);
    if (callback) {
      callback(contentId);
    }
  }

  /**
   * Returns the formatted content REST id, formatted using the CAP id.
   * @param ref
   * @return
   */
  static getRestIdFromCapId(ref: string): string {
    if (ref.indexOf("/") !== -1) {
      return "content/" + ref.substr(ref.lastIndexOf("/") + 1, ref.length);
    }

    return ref.replace("-", "/");
  }

  /**
   * Returns the content id in REST format.
   * @param bean The content to retrieve the REST id from.
   */
  static parseRestId(bean: any): string {
    return "content/" + bean.getNumericId();
  }
}

export default TaxonomyUtil;
