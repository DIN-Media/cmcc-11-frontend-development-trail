import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import IconDisplayField from "@coremedia/studio-client.ext.ui-components/components/IconDisplayField";
import OverflowBehaviour from "@coremedia/studio-client.ext.ui-components/mixins/OverflowBehaviour";
import BindPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPlugin";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import IconDisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/IconDisplayFieldSkin";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import Fill from "@jangaroo/ext-ts/toolbar/Fill";
import Spacer from "@jangaroo/ext-ts/toolbar/Spacer";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import { AnyFunction } from "@jangaroo/runtime/types";
import CommerceFormToolbarBase from "./CommerceFormToolbarBase";

interface CommerceFormToolbarConfig extends Config<CommerceFormToolbarBase>, Partial<Pick<CommerceFormToolbar,
  "collapseHandler"
>> {
}

class CommerceFormToolbar extends CommerceFormToolbarBase {
  declare Config: CommerceFormToolbarConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.studio.config.commerceFormToolbar";

  static readonly LOCALE_NAME_ITEM_ID: string = "localeName";

  static readonly DOCUMENT_TYPE_ITEM_ID: string = "documentType";

  static readonly COLLAPSE_BUTTON_ITEM_ID: string = "collapseButton";

  constructor(config: Config<CommerceFormToolbar> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    super(ConfigUtils.apply(Config(CommerceFormToolbar, {
      items: [
        Config(Spacer, { width: 12 }),
        /*Locale name*/
        Config(IconDisplayField, {
          itemId: CommerceFormToolbar.LOCALE_NAME_ITEM_ID,
          tooltipOnValue: true,
          overflowBehaviour: OverflowBehaviour.ELLIPSIS,
          ui: IconDisplayFieldSkin.WORKAREA.getSkin(),
          plugins: [
            Config(BindPlugin, {
              bindTo: this$.getLocaleValueExpression(),
              boundValueChanged: CommerceFormToolbarBase.changeLocale,
            }),
          ],
        }),
        Config(Fill),
        /*content type*/
        Config(IconDisplayField, {
          itemId: CommerceFormToolbar.DOCUMENT_TYPE_ITEM_ID,
          tooltipOnValue: true,
          overflowBehaviour: OverflowBehaviour.ELLIPSIS,
          ui: IconDisplayFieldSkin.WORKAREA.getSkin(),
          plugins: [
            Config(BindPlugin, {
              bindTo: config.bindTo,
              boundValueChanged: CommerceFormToolbarBase.changeType,
            }),
          ],
        }),
        Config(Spacer, { width: 6 }),
        /*Button to collapse content form*/
        Config(IconButton, {
          itemId: CommerceFormToolbar.COLLAPSE_BUTTON_ITEM_ID,
          scale: "medium",
          ui: ButtonSkin.WORKAREA.getSkin(),
          tooltip: Editor_properties.Document_panel_btn_tooltip,
          text: Editor_properties.Document_panel_btn_tooltip,
          iconCls: CoreIcons_properties.collapsing_arrow_left,
          handler: config.collapseHandler,
        }),
      ],

    }), config));
  }

  /** The function that will be called on collapse */
  collapseHandler: AnyFunction = null;
}

export default CommerceFormToolbar;
