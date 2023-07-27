import ContentTypes_properties from "@coremedia/studio-client.cap-base-models/content/ContentTypes_properties";
import AddItemsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/AddItemsPlugin";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import CopyResourceBundleProperties from "@coremedia/studio-client.main.editor-components/configuration/CopyResourceBundleProperties";
import AddTabbedDocumentFormsPlugin from "@coremedia/studio-client.main.editor-components/sdk/plugins/AddTabbedDocumentFormsPlugin";
import TabbedDocumentFormDispatcher from "@coremedia/studio-client.main.editor-components/sdk/premular/TabbedDocumentFormDispatcher";
import PreviewPanelToolbar from "@coremedia/studio-client.main.editor-components/sdk/preview/PreviewPanelToolbar";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import resourceManager from "@jangaroo/runtime/l10n/resourceManager";
import AnalyticsDeepLinkButtonContainer from "./AnalyticsDeepLinkButtonContainer";
import AnalyticsStudioPluginBase from "./AnalyticsStudioPluginBase";
import AnalyticsStudioPluginDocTypes_properties from "./AnalyticsStudioPluginDocTypes_properties";
import AnalyticsStudioPlugin_properties from "./AnalyticsStudioPlugin_properties";
import CMALXEventListForm from "./CMALXEventListForm";
import CMALXPageListForm from "./CMALXPageListForm";

interface AnalyticsStudioPluginConfig extends Config<AnalyticsStudioPluginBase> {
}

class AnalyticsStudioPlugin extends AnalyticsStudioPluginBase {
  declare Config: AnalyticsStudioPluginConfig;

  static readonly xtype: string = "com.coremedia.blueprint.studio.config.analytics.analyticsStudioPlugin";

  /**
   * The itemId of the analytics deep link button separator.
   */
  static readonly ANALYTICS_DEEP_LINK_SEP_ITEM_ID: string = "analyticsDeepLinkSeparator";

  constructor(config: Config<AnalyticsStudioPlugin> = null) {
    super(ConfigUtils.apply(Config(AnalyticsStudioPlugin, {

      rules: [
        /* add forms for page/event lists */
        Config(TabbedDocumentFormDispatcher, {
          plugins: [
            Config(AddTabbedDocumentFormsPlugin, {
              documentTabPanels: [
                Config(CMALXPageListForm, { itemId: "CMALXPageList" }),
                Config(CMALXEventListForm, { itemId: "CMALXEventList" }),
              ],
            }),
          ],
        }),

        /* This tab has been disabled while cleaning content forms.
         It is recommend to configure ALX using the struct editor and reduce
         the amount for forms visible for the user.  */
        /* add analytics tab to channel form */
        /*
    <bpforms:CMChannelForm>
      <bpforms:plugins>
        <ui:AddItemsPlugin>
          <ui:items>
            <local:CMChannelAnalyticsTab itemId="analytics"/>
          </ui:items>
          <ui:after>
            <editor:DocumentForm itemId="locale"/>
          </ui:after>
        </ui:AddItemsPlugin>
      </bpforms:plugins>
    </bpforms:CMChannelForm>
     */

        Config(PreviewPanelToolbar, {
          plugins: [
            Config(AddItemsPlugin, {
              items: [
                Config(Separator, {
                  hidden: true,
                  itemId: AnalyticsStudioPlugin.ANALYTICS_DEEP_LINK_SEP_ITEM_ID,
                }),
                Config(AnalyticsDeepLinkButtonContainer, { itemId: "alxDeepLinkButtonContainer" }),
              ],
            }),
          ],
        }),
      ],

      configuration: [
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ContentTypes_properties),
          source: resourceManager.getResourceBundle(null, AnalyticsStudioPluginDocTypes_properties),
        }),
        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, Editor_properties),
          source: resourceManager.getResourceBundle(null, AnalyticsStudioPlugin_properties),
        }),
      ],

    }), config));
  }
}

export default AnalyticsStudioPlugin;
