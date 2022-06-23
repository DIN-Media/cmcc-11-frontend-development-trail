import CMArticleSystemForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/components/CMArticleSystemForm";
import SEOForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/SEOForm";
import AddItemsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/AddItemsPlugin";
import NestedRulesPlugin from "@coremedia/studio-client.ext.ui-components/plugins/NestedRulesPlugin";
import StudioPlugin from "@coremedia/studio-client.main.editor-components/configuration/StudioPlugin";
import NewContentMenu from "@coremedia/studio-client.main.editor-components/sdk/newcontent/NewContentMenu";
import AddTabbedDocumentFormsPlugin from "@coremedia/studio-client.main.editor-components/sdk/plugins/AddTabbedDocumentFormsPlugin";
import ReferrerListPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/ReferrerListPanel";
import TabbedDocumentFormDispatcher from "@coremedia/studio-client.main.editor-components/sdk/premular/TabbedDocumentFormDispatcher";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import QuickCreateMenuItem from "@coremedia/studio-client.main.editor-components/sdk/quickcreate/QuickCreateMenuItem";
import MenuSeparator from "@jangaroo/ext-ts/menu/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import CMVideoTutorialTabPanel from "./forms/CMVideoTutorialTabPanel";
import ContextPropertyFieldGroup from "./forms/ContextPropertyFieldGroup";

interface TrainingStudioPluginConfig extends Config<StudioPlugin> {
}

class TrainingStudioPlugin extends StudioPlugin {
  declare Config: TrainingStudioPluginConfig;

  static readonly xtype: string = "com.coremedia.blueprint.training.studio.config.trainingStudioPlugin";

  constructor(config: Config<TrainingStudioPlugin> = null) {
    super(ConfigUtils.apply(Config(TrainingStudioPlugin, {

      rules: [
        Config(CMVideoTutorialTabPanel, {
          plugins: [
            Config(NestedRulesPlugin, {
              rules: [
                Config(SEOForm, {
                  plugins: [
                    Config(AddItemsPlugin, {
                      items: [
                        Config(StringPropertyField, { propertyName: "localSettings.targetGroup" }),
                      ],
                    }),
                  ],
                }),
              ],
            }),
          ],
        }),

        Config(TabbedDocumentFormDispatcher, {
          plugins: [
            Config(AddTabbedDocumentFormsPlugin, {
              documentTabPanels: [
                Config(CMVideoTutorialTabPanel, { itemId: "CMVideoTutorial" }),
              ],
            }),
          ],
        }),

        Config(NewContentMenu, {
          plugins: [
            Config(AddItemsPlugin, {
              items: [
                Config(QuickCreateMenuItem, { contentType: "CMVideoTutorial" }),
              ],
              before: [
                Config(MenuSeparator, { itemId: "createFromTemplateSeparator" }),
              ],
            }),
          ],
        }),

        Config(CMArticleSystemForm, {
          plugins: [
            Config(AddItemsPlugin, {
              items: [
                Config(ContextPropertyFieldGroup),
              ],
              after: [
                Config(ReferrerListPanel),
              ],
            }),
          ],
        }),
      ],
      configuration: [],

    }), config));
  }
}

export default TrainingStudioPlugin;
