import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import StructPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/struct/StructPropertyField";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CustomLabels_properties from "../../CustomLabels_properties";

interface MiscSettingsFormConfig extends Config<PropertyFieldGroup>, Partial<Pick<MiscSettingsForm,
  "contentType"
>> {
}

class MiscSettingsForm extends PropertyFieldGroup {
  declare Config: MiscSettingsFormConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.miscSettingsForm";

  constructor(config: Config<MiscSettingsForm> = null) {
    super(ConfigUtils.apply(Config(MiscSettingsForm, {
      title: CustomLabels_properties.PropertyGroup_MiscSettings_label,
      itemId: "miscSettingsForm",

      items: [
        Config(StructPropertyField, {
          propertyName: "misc",
          hideLabel: true,
          itemId: "miscSettings",
        }),
      ],

    }), config));
  }

  contentType: string = null;
}

export default MiscSettingsForm;
