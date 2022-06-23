import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";

export default class NavigationContextState {

  /**
   * The value expression containing the content item, for which the
   * derived navigation context should be evaluated.
   */
  #bindTo: ValueExpression;

  /**
   * Constructor.
   *
   * @param bindTo the value expression containing the content item, for which the
   * derived navigation context should be evaluated.
   */
  constructor(bindTo: ValueExpression) {
    this.#bindTo = bindTo;
  }

  /**
   * A helper function to find a content item with name "_folderProperties" within the folder hierarchy.
   *
   * This method is called recursively for a document and its parent folders, until a folder properties object is
   * found.
   *
   * @param content the document or one of its parent folders
   * @return content the folder properties object (or null)
   */
  protected findFolderProperties(content: Content): Content {
    if (!content) {
      // Emergency exit: if content is null, we have to stop.
      return undefined;
    }
    if (content.isDocument()) {
      // we continue with the parent folder of this content object
      const folder: Content = content.getParent();
      return this.findFolderProperties(folder);
    }
    // find a content object with name "_folderProperties" within the given content folder
    const folderProperties: Content = content.getChild("_folderProperties");
    if (folderProperties) {
      // success: this is the content we are looking for
      return folderProperties;
    } else {
      // we don't have a folderProperties object here, so we have to check the parent folder
      const parent: Content = content.getParent();
      return this.findFolderProperties(parent);
    }
  }

  /**
   * A value expression, that returns the FolderProperties content object for the current content.
   *
   * @return ValueExpression<Content>
   */
  getFolderPropertiesExpression(): ValueExpression {
    return ValueExpressionFactory.createFromFunction(() => {
      const content: Content = this.#bindTo.getValue() as Content;
      return this.findFolderProperties(content);
    });
  }

  /**
   * A value expression, that returns the contexts (array of Content) from the folder properties of the current
   * content object.
   *
   * @return ValueExpression<Array<Content>>
   */
  getDerivedContextExpression(): ValueExpression {
    const folderPropertiesExpression: ValueExpression = this.getFolderPropertiesExpression();
    return folderPropertiesExpression.extendBy("properties", "contexts");
  }
}
