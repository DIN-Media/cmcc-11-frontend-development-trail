import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import IntegerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/IntegerPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import RichTextPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/richtext/RichTextPropertyField";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CMVideoTutorialDocumentForm_properties from "./CMVideoTutorialDocumentForm_properties";

interface InformationPropertyFieldGroupConfig extends Config<PropertyFieldGroup>{}

class InformationPropertyFieldGroup extends PropertyFieldGroup {
  declare Config: InformationPropertyFieldGroupConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.informationPropertyFieldGroup";

  constructor(config: Config<InformationPropertyFieldGroup> = null) {
    super(ConfigUtils.apply(Config(InformationPropertyFieldGroup, {
      itemId: "informationGroup",
      title: CMVideoTutorialDocumentForm_properties.Group_information_title,
      items: [
        Config(RichTextPropertyField, { propertyName: "productionInfo" }),
        Config(StringPropertyField, { propertyName: "copyright" }),
        Config(IntegerPropertyField, { propertyName: "duration" }),
      ],
    }), config));
  }
}

export default InformationPropertyFieldGroup;
