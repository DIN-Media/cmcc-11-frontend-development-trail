import Class from "@jangaroo/runtime/Class";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CreateCatalogObjectDocumentActionBase from "./CreateCatalogObjectDocumentActionBase";

interface CreateCatalogObjectDocumentActionConfig extends Config<CreateCatalogObjectDocumentActionBase>, Partial<Pick<CreateCatalogObjectDocumentAction,
  "actionName" |
  "contentType" |
  "catalogObjectType" |
  "inheritEditors"
>> {
}

class CreateCatalogObjectDocumentAction extends CreateCatalogObjectDocumentActionBase {
  declare Config: CreateCatalogObjectDocumentActionConfig;

  constructor(config: Config<CreateCatalogObjectDocumentAction> = null) {
    super(ConfigUtils.apply(Config(CreateCatalogObjectDocumentAction), config));
  }

  actionName: string = null;

  contentType: string = null;

  catalogObjectType: Class = null;

  /**
   * Optional: if set to false the property editors will not be inherited from the super content types.
   * Default is true, i.e. the property editors are inherited.
   */
  inheritEditors: boolean = false;
}

export default CreateCatalogObjectDocumentAction;
