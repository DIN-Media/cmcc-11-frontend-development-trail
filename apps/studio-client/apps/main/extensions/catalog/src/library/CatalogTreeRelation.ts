import ContentTreeRelation from "@coremedia/studio-client.cap-base-models/content/ContentTreeRelation";
import IdHelper from "@coremedia/studio-client.cap-rest-client/common/IdHelper";
import session from "@coremedia/studio-client.cap-rest-client/common/session";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ContentCreateResult from "@coremedia/studio-client.cap-rest-client/content/ContentCreateResult";
import ContentType from "@coremedia/studio-client.cap-rest-client/content/ContentType";
import PublicationService from "@coremedia/studio-client.cap-rest-client/content/publication/PublicationService";
import Logger from "@coremedia/studio-client.client-core-impl/logging/Logger";
import { mixin } from "@jangaroo/runtime";
import { AnyFunction } from "@jangaroo/runtime/types";
import CatalogCopy from "./CatalogCopy";
import CatalogDelete from "./CatalogDelete";
import CatalogMove from "./CatalogMove";
import CatalogTreeRelationHelper from "./CatalogTreeRelationHelper";
import CatalogUndelete from "./CatalogUndelete";

/**
 * Intercepts the new content creation and updates the calculateDisabled/hidden state for catalog content types.
 */
class CatalogTreeRelation implements ContentTreeRelation {

  static readonly PROPERTY_CONTEXTS: string = CatalogTreeRelationHelper.PROPERTY_CONTEXTS;

  static readonly PROPERTY_CHILDREN: string = CatalogTreeRelationHelper.PROPERTY_CHILDREN;

  static readonly CONTENT_TYPE_CATEGORY: string = CatalogTreeRelationHelper.CONTENT_TYPE_CATEGORY;

  static readonly CONTENT_TYPE_PRODUCT: string = CatalogTreeRelationHelper.CONTENT_TYPE_PRODUCT;

  #linkListContainers: any = null;

  #linkListParents: any = null;

  constructor() {
    this.#linkListContainers = { "CMCategory": CatalogTreeRelation.PROPERTY_CHILDREN };
    this.#linkListParents = { "CMHasContexts": CatalogTreeRelation.PROPERTY_CONTEXTS };
  }

  folderNodeType(): string {
    return CatalogTreeRelation.CONTENT_TYPE_CATEGORY;
  }

  leafNodeType(): string {
    return CatalogTreeRelation.CONTENT_TYPE_PRODUCT;
  }

  /**
   * @return true if the given content is of the given type, undefined if something is not loaded yet, false otherwise.
   */
  static #typeIs(content: Content, typeName: string): boolean {
    const contentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    return contentType.getName() && contentType.getName() === typeName;
  }

  isLeafNode(content: Content): boolean {
    return CatalogTreeRelation.#typeIs(content, CatalogTreeRelation.CONTENT_TYPE_PRODUCT);
  }

  isFolderNode(content: Content): boolean {
    return CatalogTreeRelation.#typeIs(content, CatalogTreeRelation.CONTENT_TYPE_CATEGORY);
  }

  getParent(content: Content): Content {
    const parents = this.getParents(content);
    if (parents === undefined) {
      return undefined;
    }
    if (parents && parents.length > 0) {
      return parents[0];
    }
    return null;
  }

  getParents(content: Content): Array<any> {
    const linkListContainerType = this.#getLinkListContainerType(content);
    if (linkListContainerType === undefined) {
      return undefined;
    }
    if (linkListContainerType) {
      const parentCategories = content.getReferrersWithNamedDescriptor(linkListContainerType,
        this.#linkListContainers[linkListContainerType]);
      if (parentCategories === undefined) {
        return undefined;
      }
      return parentCategories || null;
    }
    const linkListParentType = this.#getLinkListParentType(content);
    if (linkListParentType === undefined) {
      return undefined;
    }
    if (linkListParentType) {
      const contexts: Array<any> = content.getProperties().get(this.#linkListParents[linkListParentType]);
      if (contexts === undefined) {
        return undefined;
      }
      return contexts;
    }
    return null;
  }

  static #lookupType(typeMapping: any, content: Content): string {
    const contentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    for (const typeName in typeMapping) {
      if (contentType.isSubtypeOf(typeName)) {
        return typeName;
      }
    }
    return null;
  }

  #getLinkListContainerType(content: Content): string {
    return CatalogTreeRelation.#lookupType(this.#linkListContainers, content);
  }

  #getLinkListParentType(content: Content): string {
    return CatalogTreeRelation.#lookupType(this.#linkListParents, content);
  }

  getSubFolders(content: Content): Array<any> {
    const linkListContainerType = this.#getLinkListContainerType(content);
    switch (linkListContainerType) {
    case undefined:
      return undefined;
    case null:
      return [];
    }
    return content.getProperties().get(this.#linkListContainers[linkListContainerType]);
  }

  getLeafContent(content: Content): Array<any> {
    //get the children of the selected category
    const filtered = [];
    const linkingChildren = content.getReferrersWithNamedDescriptor(CatalogTreeRelation.CONTENT_TYPE_PRODUCT, CatalogTreeRelation.PROPERTY_CONTEXTS);
    if (linkingChildren === undefined) {
      return undefined;
    }
    for (const child of linkingChildren as Content[]) {
      if (child.isInProduction()) {
        filtered.push(child);
      }
    }
    return filtered;
  }

  mayMove(contents: Array<any>, newParent: Content): boolean {
    if (contents.some((content: Content): boolean =>
      this.getParent(content) === null,
    )) {
      return false; // must not move root
    }
    if (!newParent) {
      return true;
    }
    if (newParent.isFolder() || newParent.getType().getName() !== CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
      return false;
    }
    for (const source of contents as Content[]) {
      const typeName = source.getType().getName();
      if (typeName !== CatalogTreeRelation.CONTENT_TYPE_PRODUCT && typeName !== CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
        return false;
      }

      //check drop on itself
      if (IdHelper.parseContentId(source) === IdHelper.parseContentId(newParent)) {
        return false;
      }

      // check for root node and paste into same parent
      const parent = this.getParent(source);
      if (!parent || IdHelper.parseContentId(parent) === IdHelper.parseContentId(newParent)) {
        return false;
      }

      //check paste into children
      let targetParent = this.getParent(newParent);
      while (targetParent) {
        if (IdHelper.parseContentId(targetParent) === IdHelper.parseContentId(source)) {
          return false;
        }
        targetParent = this.getParent(targetParent);
      }
    }
    return true;
  }

  move(sources: Array<any>, newParent: Content, callback?: AnyFunction): void {
    if (this.mayMove(sources, newParent)) {
      const command = new CatalogMove(this, sources, newParent, callback);
      command.execute();
    }
  }

  mayCreate(folder: Content, contentType: ContentType): boolean {
    if (contentType.getName() === undefined) {
      return undefined;
    }
    if (folder.getType().getName() === undefined) {
      return undefined;
    }

    const isCatalogType: boolean = contentType.getName() === CatalogTreeRelation.CONTENT_TYPE_CATEGORY || contentType.getName() === CatalogTreeRelation.CONTENT_TYPE_PRODUCT;
    return folder.getType().getName() === CatalogTreeRelation.CONTENT_TYPE_CATEGORY && isCatalogType;
  }

  mayCopy(sources: Array<any>, newParent: Content): boolean {
    //TODO category copy currently not allowed!
    for (const source of sources as Content[]) {
      if (source.getType().isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
        return false;
      }
    }

    return this.mayMove(sources, newParent);
  }

  copy(sources: Array<any>, newParent: Content, callback?: AnyFunction): void {
    const command = new CatalogCopy(this, sources, newParent, callback);
    command.execute();
  }

  /**
   * @param contents the contents to delete, products or categories.
   * @param callback optional callback
   */
  deleteContents(contents: Array<any>, callback?: AnyFunction): void {
    const command = new CatalogDelete(this, contents, callback);
    command.execute();
  }

  undeleteContents(contents: Array<any>, callback?: AnyFunction): void {
    const command = new CatalogUndelete(this, contents, callback);
    command.execute();
  }

  mayDelete(contents: Array<any>): boolean {
    for (const source of contents as Content[]) {
      if (!source.isInProduction()) {
        return false;
      }

      const typeName = source.getType().getName();
      //products can always be deleted or at least unlinked if there is more than one parent
      if (typeName !== CatalogTreeRelation.CONTENT_TYPE_PRODUCT && typeName !== CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
        return false;
      }

      if (this.getParents(source) === undefined) {
        return false;
      }

      //check if the content must be unlinked first
      if (this.getParents(source).length > 1) {
        return false;
      }

      //well, for categories we have to check if there are remaining products in it
      if (typeName === CatalogTreeRelation.CONTENT_TYPE_CATEGORY) {
        const leafContent = this.getLeafContent(source);
        let children = this.getSubFolders(source);
        if (leafContent) {
          children = children.concat(leafContent);
        }
        for (const child of children as Content[]) {
          if (child.isInProduction()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  addChildNeedsFolderCheckout(folder: Content, childType: string): boolean {
    return childType === CatalogTreeRelation.CONTENT_TYPE_CATEGORY;
  }

  provideRepositoryFolderFor(contentType: ContentType, folderNode: Content, childNodeName: string, callback: AnyFunction): void {
    const repository = session._.getConnection().getContentRepository();
    if (contentType.isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_CATEGORY)) {
      repository.getFolderContentType().create(folderNode.getParent(), childNodeName, (result: ContentCreateResult): void => {
        if (result.error) {
          Logger.error("Error creating folder \"" + childNodeName + "\"in " + folderNode.getParent().getPath() + ": "
          + result.error.errorCode + "/" + result.error.errorName);
        }
        if (result.createdContent) {
          callback(result.createdContent);
        }
      });
    } else if (contentType.isSubtypeOf(CatalogTreeRelation.CONTENT_TYPE_PRODUCT)) {
      callback(folderNode.getParent());
    }
  }

  rename(content: Content, newName: string, callback: AnyFunction = null): void {
    if (this.isFolderNode(content)) {
      // for categories the content and its parent folder need to be renamed
      // start with the parent folder
      const parent = content.getParent();
      const oldName = parent.getName();
      parent.rename(newName, (): void => {
        // only rename content if parent could be renamed
        if (parent.getName() === newName) {
          content.rename(newName, (): void => {
            // if content could not be renamed revert renaming of parent
            if (content.getName() === newName) {
              callback();
            } else {
              parent.rename(oldName, callback);
            }
          });
        }
      });
    } else {
      content.rename(newName, callback);
    }
  }

  /**
   * Shows an error dialog about checked out content. Since we are working on content documents, not folders
   * we have to take extra care about the lifecycle state when documents are copied or moved.
   * @param target the target is that already checked out by another user.
   */
  showCheckoutError(target: Content): void {
    CatalogTreeRelationHelper.showCheckoutError(target);
  }

  addChildNodes(treeParent: Content, sources: Array<any>, callback: AnyFunction): void {
    if (CatalogTreeRelationHelper.validateCheckoutState(sources.concat(treeParent))) {
      const modifications = [];
      //update linking
      for (const source of sources as Content[]) {
        modifications.push(CatalogTreeRelationHelper.addCategoryChild(treeParent, source));
      }

      //update checkin state
      for (const modified of modifications as Content[]) {
        if (modified) {
          modified.invalidate((refreshContent: Content): void => {
            if (refreshContent.isCheckedOut()) {
              refreshContent.checkIn();
            }
          });
        }
      }

      callback();
    }
  }

  withdraw(contents: Array<any>, publicationService: PublicationService, callback: AnyFunction): void {
    const repository = session._.getConnection().getContentRepository();
    repository.getPublicationService().withdrawAllFromTree(contents, CatalogTreeRelation.CONTENT_TYPE_CATEGORY, CatalogTreeRelation.PROPERTY_CHILDREN, callback);
  }
}
mixin(CatalogTreeRelation, ContentTreeRelation);

export default CatalogTreeRelation;
