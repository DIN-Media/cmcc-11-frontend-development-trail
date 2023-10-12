import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import VerticalSpacingPlugin from "@coremedia/studio-client.ext.ui-components/plugins/VerticalSpacingPlugin";
import PropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyField";
import Container from "@jangaroo/ext-ts/container/Container";
import AnchorLayout from "@jangaroo/ext-ts/layout/container/Anchor";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import TaxonomyLinkListPropertyField from "./TaxonomyLinkListPropertyField";
import TaxonomyPropertyFieldBase from "./TaxonomyPropertyFieldBase";
import TaxonomySuggestionsLinkListPanel from "./TaxonomySuggestionsLinkListPanel";

interface TaxonomyPropertyFieldConfig extends Config<TaxonomyPropertyFieldBase>, Partial<Pick<TaxonomyPropertyField,
  "bindTo" |
  "forceReadOnlyValueExpression" |
  "propertyName" |
  "taxonomyId" |
  "taxonomyIdExpression" |
  "disableSuggestions"
>> {
}

class TaxonomyPropertyField extends TaxonomyPropertyFieldBase {
  declare Config: TaxonomyPropertyFieldConfig;

  static override readonly xtype: string = "com.coremedia.blueprint.studio.config.taxonomy.taxonomyPropertyField";

  constructor(config: Config<TaxonomyPropertyField> = null) {
    config = ConfigUtils.apply({ disableSuggestions: false }, config);
    // @ts-expect-error Ext JS semantics
    const this$ = this;
    super(ConfigUtils.apply(Config(TaxonomyPropertyField, {

      defaultType: PropertyField.xtype,

      defaults: Config<PropertyField>({
        anchor: "100%",
        bindTo: config.bindTo,
        ...{
          propertyName: config.propertyName,
          taxonomyIdExpression: this$.getTaxonomyIdExpr(config),
        },
        forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
      }),
      items: [
        Config(TaxonomyLinkListPropertyField, {
          bindTo: config.bindTo,
          hideLabel: true,
          propertyName: config.propertyName,
          labelAlign: "top",
        }),
        Config(Container, {
          itemId: "suggestionsPanel",
          defaultType: PropertyField.xtype,
          defaults: Config<PropertyField>({
            anchor: "100%",
            bindTo: config.bindTo,
            ...{
              propertyName: config.propertyName,
              taxonomyIdExpression: this$.getTaxonomyIdExpr(config),
            },
            forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
          }),
          items: [
            Config(TaxonomySuggestionsLinkListPanel, {
              itemId: "taxonomySuggestionsLinkListPanel",
              maskOnDisable: false,
              disableSuggestions: config.disableSuggestions,
            }),
          ],
          plugins: [
            Config(VerticalSpacingPlugin),
          ],
          layout: Config(AnchorLayout, { manageOverflow: false }),
        }),
      ],
      layout: Config(AnchorLayout, { manageOverflow: false }),
    }), config));
  }

  /**
   * The content bean value expression.
   */
  bindTo: ValueExpression = null;

  /**
   * An optional ValueExpression which makes the component read-only if it is evaluated to true.
   */
  forceReadOnlyValueExpression: ValueExpression = null;

  /**
   * The property name of the content that should contain the taxonomy to display.
   */
  propertyName: string = null;

  /**
   * The taxonomy identifier configured on the server side.
   */
  taxonomyId: string = null;

  /** a value expression to calculate the taxonomyId in case the field 'taxonomyId' has not been set */
  taxonomyIdExpression: any = null;

  /**
   * If true, suggestions field is not shown, default is false.
   */
  disableSuggestions: boolean = false;
}

export default TaxonomyPropertyField;
