import ContentLifecycleUtil from "@coremedia/studio-client.cap-base-models/content/ContentLifecycleUtil";
import ContentLocalizationUtil from "@coremedia/studio-client.cap-base-models/content/ContentLocalizationUtil";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import StatusColumn from "@coremedia/studio-client.ext.cap-base-components/columns/StatusColumn";
import TypeIconColumn from "@coremedia/studio-client.ext.cap-base-components/columns/TypeIconColumn";
import OpenInTabAction from "@coremedia/studio-client.ext.form-services-toolkit/actions/OpenInTabAction";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import BindListPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindListPlugin";
import BindSelectionPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindSelectionPlugin";
import DataField from "@jangaroo/ext-ts/data/field/Field";
import FieldContainer from "@jangaroo/ext-ts/form/FieldContainer";
import GridPanel from "@jangaroo/ext-ts/grid/Panel";
import GridColumn from "@jangaroo/ext-ts/grid/column/Column";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import NavigationContextState from "../state/NavigationContextState";
import SelectionState from "../state/SelectionState";
import DerivedNavigationContextField_properties from "./DerivedNavigationContextField_properties";

interface DerivedNavigationContextFieldConfig extends Config<FieldContainer> {
  /**
   * Configuration property:
   * value expression to the content object
   **/
  bindTo?: ValueExpression;
}

class DerivedNavigationContextField extends FieldContainer {
  declare Config: DerivedNavigationContextFieldConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.training.studio.config.derivedNavigationContextField";

  constructor(config: Config<DerivedNavigationContextField> = null) {
    const selectionState = new SelectionState();
    const navigationContextState = new NavigationContextState(config.bindTo);
    super(ConfigUtils.apply(Config(DerivedNavigationContextField, {
      fieldLabel: DerivedNavigationContextField_properties.fieldLabel_text,
      items: [
        Config(GridPanel, {
          tbar: Config(Toolbar, {
            items: [
              Config(IconButton, {
                itemId: "openSelectionInTab",
                baseAction: Config(OpenInTabAction, { contentValueExpression: selectionState.getSelectionExpression() }),
              }),
              Config(IconButton, {
                itemId: "openFolderPropertiesInTab",
                text: DerivedNavigationContextField_properties.toolbar_openFolderPropertiesInTab_text,
                tooltip: DerivedNavigationContextField_properties.toolbar_openFolderPropertiesInTab_tooltip,
                iconCls: DerivedNavigationContextField_properties.toolbar_openFolderPropertiesInTab_icon,
                baseAction: Config(OpenInTabAction, { contentValueExpression: navigationContextState.getFolderPropertiesExpression() }),
              }),
            ],
          }),
          columns: [
            Config(TypeIconColumn, {
              dataIndex: "typeCls",
              text: DerivedNavigationContextField_properties.gridpanel_columns_type_text,
              sortable: false,
            }),
            Config(GridColumn, {
              dataIndex: "name",
              text: DerivedNavigationContextField_properties.gridpanel_columns_name_text,
              sortable: false,
              flex: 1,
            }),
            Config(StatusColumn, {
              dataIndex: "status",
              text: DerivedNavigationContextField_properties.gridpanel_columns_status_text,
              sortable: false,
            }),
          ],
          plugins: [
            Config(BindListPlugin, {
              bindTo: navigationContextState.getDerivedContextExpression(),
              fields: [
                Config(DataField, {
                  name: "typeCls",
                  mapping: "type",
                  convert: ContentLocalizationUtil.getIconStyleClassForContentType,
                }),
                Config(DataField, {
                  name: "name",
                  mapping: "name",
                }),
                Config(DataField, {
                  name: "status",
                  mapping: "",
                  convert: ContentLifecycleUtil.getDetailedLifecycleStatus,
                }),
              ],
            }),
            Config(BindSelectionPlugin, { selectedValues: selectionState.getSelectionExpression() }),
          ],
        }),
      ],
    }), config));
  }

}

export default DerivedNavigationContextField;
