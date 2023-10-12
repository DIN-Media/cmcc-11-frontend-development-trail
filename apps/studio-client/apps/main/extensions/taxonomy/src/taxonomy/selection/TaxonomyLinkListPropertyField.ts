import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";
import LinkListDropArea from "@coremedia/studio-client.ext.link-list-components/LinkListDropArea";
import LinkListBEMEntities from "@coremedia/studio-client.ext.ui-components/bem/LinkListBEMEntities";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import IconDisplayField from "@coremedia/studio-client.ext.ui-components/components/IconDisplayField";
import BEMMixin from "@coremedia/studio-client.ext.ui-components/plugins/BEMMixin";
import BEMPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BEMPlugin";
import ContextMenuPlugin from "@coremedia/studio-client.ext.ui-components/plugins/ContextMenuPlugin";
import HideObsoleteSeparatorsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/HideObsoleteSeparatorsPlugin";
import TextfieldSkin from "@coremedia/studio-client.ext.ui-components/skins/TextfieldSkin";
import ToolbarSkin from "@coremedia/studio-client.ext.ui-components/skins/ToolbarSkin";
import createComponentSelector from "@coremedia/studio-client.ext.ui-components/util/createComponentSelector";
import Actions_properties from "@coremedia/studio-client.main.editor-components/sdk/actions/Actions_properties";
import LinkListCopyAction from "@coremedia/studio-client.main.editor-components/sdk/actions/LinkListCopyAction";
import LinkListCutAction from "@coremedia/studio-client.main.editor-components/sdk/actions/LinkListCutAction";
import LinkListPasteAction from "@coremedia/studio-client.main.editor-components/sdk/actions/LinkListPasteAction";
import LinkListRemoveAction from "@coremedia/studio-client.main.editor-components/sdk/actions/LinkListRemoveAction";
import PropertyFieldPlugin from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldPlugin";
import SetPropertyLabelPlugin from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/plugins/SetPropertyLabelPlugin";
import PropertyEditorUtil from "@coremedia/studio-client.main.editor-components/sdk/util/PropertyEditorUtil";
import ShowIssuesPlugin from "@coremedia/studio-client.main.editor-components/sdk/validation/ShowIssuesPlugin";
import ActionRef from "@jangaroo/ext-ts/ActionRef";
import AnchorLayout from "@jangaroo/ext-ts/layout/container/Anchor";
import HBoxLayout from "@jangaroo/ext-ts/layout/container/HBox";
import Item from "@jangaroo/ext-ts/menu/Item";
import Menu from "@jangaroo/ext-ts/menu/Menu";
import ext_menu_Separator from "@jangaroo/ext-ts/menu/Separator";
import ext_toolbar_Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import { AnyFunction } from "@jangaroo/runtime/types";
import TaxonomyStudioPlugin_properties from "../TaxonomyStudioPlugin_properties";
import OpenTaxonomyChooserAction from "../action/OpenTaxonomyChooserAction";
import TaxonomyLinkListGridPanel from "./TaxonomyLinkListGridPanel";
import TaxonomyLinkListPropertyFieldBase from "./TaxonomyLinkListPropertyFieldBase";
import TaxonomySearchField from "./TaxonomySearchField";

interface TaxonomyLinkListPropertyFieldConfig extends Config<TaxonomyLinkListPropertyFieldBase>, Partial<Pick<TaxonomyLinkListPropertyField,
  "taxonomyLinkListSideButtonVerticalAdjustment" |
  "taxonomyLinkListSideButtonHorizontalAdjustment" |
  "taxonomyLinkListSideButtonRenderToFunction" |
  "hideIssues"
>> {
}

class TaxonomyLinkListPropertyField extends TaxonomyLinkListPropertyFieldBase {
  declare Config: TaxonomyLinkListPropertyFieldConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.taxonomy.taxonomyLinkListPropertyField";

  taxonomyLinkListSideButtonVerticalAdjustment: number = NaN;

  taxonomyLinkListSideButtonHorizontalAdjustment: number = NaN;

  taxonomyLinkListSideButtonRenderToFunction: AnyFunction = null;

  constructor(config: Config<TaxonomyLinkListPropertyField> = null) {
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    super(ConfigUtils.apply(Config(TaxonomyLinkListPropertyField, {
      labelAlign: "top",
      labelSeparator: "",
      layout: Config(AnchorLayout, {
        anchor: "100%",
        manageOverflow: false,
      }),
      ...ConfigUtils.append({
        actionList: [
          new LinkListCutAction({
            text: Actions_properties.Action_cutToClipboard_text,
            tooltip: Actions_properties.Action_cutToClipboard_tooltip,
            linkListWrapper: this$.getLinkListWrapper(config),
            selectedValuesExpression: this$.getSelectedValuesVE(),
            selectedPositionsExpression: this$.getSelectedPositionsVE(),
          }),
          new LinkListCopyAction({
            text: Actions_properties.Action_copyToClipboard_text,
            tooltip: Actions_properties.Action_copyToClipboard_tooltip,
            linkListWrapper: this$.getLinkListWrapper(config),
            selectedValuesExpression: this$.getSelectedValuesVE(),
          }),
          new LinkListPasteAction({
            text: Actions_properties.Action_pasteFromClipboard_text,
            tooltip: Actions_properties.Action_pasteFromClipboard_tooltip,
            linkListWrapper: this$.getLinkListWrapper(config),
            selectedValuesExpression: this$.getSelectedValuesVE(),
            selectedPositionsExpression: this$.getSelectedPositionsVE(),
          }),
          new LinkListRemoveAction({
            text: Actions_properties.Action_deleteSelectedLinks_text,
            tooltip: Actions_properties.Action_deleteSelectedLinks_tooltip,
            linkListWrapper: this$.getLinkListWrapper(config),
            selectedValuesExpression: this$.getSelectedValuesVE(),
            selectedPositionsExpression: this$.getSelectedPositionsVE(),
          }),
          new OpenTaxonomyChooserAction({
            itemId: "openTaxonomyChooser",
            text: TaxonomyStudioPlugin_properties.Taxonomy_action_tooltip,
            tooltip: TaxonomyStudioPlugin_properties.Taxonomy_action_tooltip,
            iconCls: TaxonomyStudioPlugin_properties.Taxonomy_action_icon,
            bindTo: config.bindTo,
            siteSelectionExpression: this$.getSiteSelectionExpression(config.bindTo),
            forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
            propertyValueExpression: config.bindTo.extendBy("properties." + config.propertyName),
            taxonomyIdExpression: config.taxonomyIdExpression,
          }),
        ],
      }),
      plugins: [
        Config(SetPropertyLabelPlugin, {
          bindTo: config.bindTo,
          propertyName: config.propertyName,
        }),
      ],
      items: [
        Config(TaxonomyLinkListGridPanel, {
          itemId: TaxonomyLinkListPropertyFieldBase.GRID_PANEL_ITEM_ID,
          linkListWrapper: this$.getLinkListWrapper(config),
          taxonomyIdExpression: config.taxonomyIdExpression,
          bindTo: config.bindTo,
          selectedPositionsExpression: this$.getSelectedPositionsVE(),
          selectedValuesExpression: this$.getSelectedValuesVE(),
          readOnlyValueExpression: PropertyEditorUtil.createReadOnlyValueExpression(config.bindTo, config.forceReadOnlyValueExpression),
          sideButtonHorizontalAdjustment: config.taxonomyLinkListSideButtonHorizontalAdjustment,
          sideButtonVerticalAdjustment: config.taxonomyLinkListSideButtonVerticalAdjustment,
          sideButtonRenderToFunction: config.taxonomyLinkListSideButtonRenderToFunction,
          ...ConfigUtils.append({
            plugins: [
              Config(ShowIssuesPlugin, {
                bindTo: config.bindTo,
                ifUndefined: "",
                propertyName: config.propertyName,
                hideIssues: config.hideIssues,
              }),

              Config(PropertyFieldPlugin, { propertyName: config.propertyName }),
              Config(BEMPlugin, {
                block: LinkListBEMEntities.BLOCK,
                bodyElement: LinkListBEMEntities.ELEMENT_LIST,
                modifier: this$.getModifierVE(config),
              }),
              Config(ContextMenuPlugin, {
                contextMenu: Config(Menu, {
                  plain: true,
                  plugins: [
                    Config(HideObsoleteSeparatorsPlugin),
                  ],
                  items: [
                    Config(Item, { baseAction: Config(ActionRef, { actionId: OpenTaxonomyChooserAction.ACTION_ID }) }),
                    Config(ext_menu_Separator),
                    Config(Item, { baseAction: Config(ActionRef, { actionId: LinkListCutAction.ACTION_ID }) }),
                    Config(Item, { baseAction: Config(ActionRef, { actionId: LinkListCopyAction.ACTION_ID }) }),
                    Config(Item, { baseAction: Config(ActionRef, { actionId: LinkListPasteAction.ACTION_ID }) }),
                    Config(ext_menu_Separator),
                    Config(Item, { baseAction: Config(ActionRef, { actionId: LinkListRemoveAction.ACTION_ID }) }),
                  ],
                }),
              }),
            ],
          }),
          tbar: Config(Toolbar, {
            ui: ToolbarSkin.FIELD.getSkin(),
            items: [
              Config(IconButton, {
                itemId: TaxonomyLinkListPropertyFieldBase.DELETE_BUTTON_ITEM_ID,
                baseAction: Config(ActionRef, { actionId: LinkListRemoveAction.ACTION_ID }),
              }),
              Config(IconButton, {
                itemId: TaxonomyLinkListPropertyFieldBase.OPEN_TAXONOMY_CHOOSER_BUTTON_ITEM_ID,
                baseAction: Config(ActionRef, { actionId: OpenTaxonomyChooserAction.ACTION_ID }),
              }),

              Config(ext_toolbar_Separator),
              /* cut */
              Config(IconButton, { baseAction: Config(ActionRef, { actionId: LinkListCutAction.ACTION_ID }) }),
              /* copy */
              Config(IconButton, { baseAction: Config(ActionRef, { actionId: LinkListCopyAction.ACTION_ID }) }),
              /* paste */
              Config(IconButton, { baseAction: Config(ActionRef, { actionId: LinkListPasteAction.ACTION_ID }) }),
            ],
          }),
          dockedItems: [
            /*We are setting the default focus to the text field since we are not interested in opening the library */
            Config(LinkListDropArea, {
              dock: "bottom",
              linkListWrapper: this$.getLinkListWrapper(config),
              ddGroups: ["ContentDD", "ContentLinkDD"],
              defaultFocus: createComponentSelector().itemId(TaxonomyLinkListPropertyFieldBase.TAXONOMY_SEARCH_FIELD_ITEM_ID).build(),
              appendOnDrop: false,
              dropHandler: bind(this$, this$.handleDropAreaDrop),
              readOnlyValueExpression: PropertyEditorUtil.createReadOnlyValueExpression(config.bindTo, config.forceReadOnlyValueExpression),
              ...Config<BEMMixin>({ bemElement: LinkListBEMEntities.ELEMENT_TAIL }),
              layout: Config(HBoxLayout),
              items: [
                Config(IconDisplayField, {
                  iconCls: CoreIcons_properties.add,
                  width: 16,
                  margin: "0 0 0 8",
                }),
                Config(TaxonomySearchField, {
                  itemId: TaxonomyLinkListPropertyFieldBase.TAXONOMY_SEARCH_FIELD_ITEM_ID,
                  ui: TextfieldSkin.EMBEDDED.getSkin(),
                  flex: 1,
                  resetOnBlur: true,
                  showSelectionPath: false,
                  taxonomyIdExpression: config.taxonomyIdExpression,
                  linkListWrapper: this$.getLinkListWrapper(config),
                  bindTo: config.bindTo,
                  forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
                  propertyName: config.propertyName,
                  siteSelectionExpression: this$.getSiteSelectionExpression(config.bindTo),
                  searchResultExpression: this$.getSearchResultExpression(),
                }),
              ],
            }),
          ],
        }),
      ],

    }), config));
  }

  hideIssues: boolean = false;
}

export default TaxonomyLinkListPropertyField;
