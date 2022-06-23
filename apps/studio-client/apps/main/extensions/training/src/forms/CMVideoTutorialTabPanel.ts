import CMArticleSystemForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/CMArticleSystemForm";
import DefaultExtraDataForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/DefaultExtraDataForm";
import MultiLanguageDocumentForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/MultiLanguageDocumentForm";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CMVideoTutorialDocumentForm from "./CMVideoTutorialDocumentForm";

interface CMVideoTutorialTabPanelConfig extends Config<DocumentTabPanel> {}

class CMVideoTutorialTabPanel extends DocumentTabPanel {
  declare Config: CMVideoTutorialTabPanelConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.cmVideoTutorialTabPanel";

  constructor(config: Config<DocumentTabPanel> = null) {
    super(ConfigUtils.apply(Config(CMVideoTutorialTabPanel, {
      items: [
        Config(CMVideoTutorialDocumentForm),
        Config(DefaultExtraDataForm),
        Config(MultiLanguageDocumentForm),
        Config(CMArticleSystemForm),
      ],
    }), config));
  }
}

export default CMVideoTutorialTabPanel;
