import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import RichTextPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/richtext/RichTextPropertyField";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CMVideoTutorialDocumentForm_properties from "./CMVideoTutorialDocumentForm_properties";

interface TeaserPropertyFieldGroupConfig extends Config<PropertyFieldGroup> {}

class TeaserPropertyFieldGroup extends PropertyFieldGroup {
  declare Config: TeaserPropertyFieldGroupConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.teaserPropertyFieldGroup";

  constructor(config: Config<PropertyFieldGroup> = null) {
    super(ConfigUtils.apply(Config(TeaserPropertyFieldGroup, {
      itemId: "teaserGroup",
      title: CMVideoTutorialDocumentForm_properties.Group_teaser_title,
      items: [
        Config(StringPropertyField, { propertyName: "teaserTitle" }),
        Config(RichTextPropertyField, { propertyName: "teaserText" }),
        Config(LinkListPropertyField, {
          propertyName: "pictures",
          showThumbnails: true,
        }),
      ],
    }), config));
  }
}

export default TeaserPropertyFieldGroup;
