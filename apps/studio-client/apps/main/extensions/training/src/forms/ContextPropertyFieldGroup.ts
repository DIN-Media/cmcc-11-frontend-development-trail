import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import DerivedNavigationContextField from "../fields/DerivedNavigationContextField";
import CMVideoTutorialDocumentForm_properties from "./CMVideoTutorialDocumentForm_properties";

interface ContextPropertyFieldGroupConfig extends Config<PropertyFieldGroup> {
}

class ContextPropertyFieldGroup extends PropertyFieldGroup {
  declare Config: ContextPropertyFieldGroupConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.contextPropertyFieldGroup";

  constructor(config: Config<PropertyFieldGroup> = null) {
    super(ConfigUtils.apply(Config(ContextPropertyFieldGroup, {
      itemId: "contextPropertyFieldGroup",
      title: CMVideoTutorialDocumentForm_properties.Group_context_title,
      items: [
        Config(LinkListPropertyField, { propertyName: "contexts" }),
        Config(DerivedNavigationContextField),
      ],
    }), config));
  }
}

export default ContextPropertyFieldGroup;
