import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import RichTextPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/richtext/RichTextPropertyField";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CMVideoTutorialDocumentForm_properties from "./CMVideoTutorialDocumentForm_properties";

interface TutorialPropertyFieldGroupConfig extends Config<PropertyFieldGroup> {}

class TutorialPropertyFieldGroup extends PropertyFieldGroup {
  declare Config: TutorialPropertyFieldGroupConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.tutorialPropertyFieldGroup";

  constructor(config: Config<TutorialPropertyFieldGroup> = null) {
    super(ConfigUtils.apply(Config(TutorialPropertyFieldGroup, {
      itemId: "tutorialGroup",
      title: CMVideoTutorialDocumentForm_properties.Group_tutorial_title,
      items: [
        Config(StringPropertyField, { propertyName: "title" }),
        Config(LinkListPropertyField, { propertyName: "video" }),
        Config(RichTextPropertyField, { propertyName: "detailText" }),
      ],
    }), config));
  }
}

export default TutorialPropertyFieldGroup;
