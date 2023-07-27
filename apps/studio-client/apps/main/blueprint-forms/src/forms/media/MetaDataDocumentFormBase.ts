import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import Ext from "@jangaroo/ext-ts";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import MetaDataDocumentForm from "./MetaDataDocumentForm";
import MetaDataSection from "./MetaDataSection";

interface MetaDataDocumentFormBaseConfig extends Config<PropertyFieldGroup> {
}

/**
 * Base model of the image meta data content form.
 * The base class determines the different meta data type available for an image.
 */
class MetaDataDocumentFormBase extends PropertyFieldGroup {
  declare Config: MetaDataDocumentFormBaseConfig;

  #metaDataExpression: ValueExpression = null;

  #rawMetaDataExpression: ValueExpression = null;

  #sections: Array<any> = [];

  constructor(config: Config<MetaDataDocumentForm> = null) {
    super(config);
    this.#rawMetaDataExpression = config.bindTo.extendBy("properties", config.propertyName, "metadata", config.metadataSectionName);
    this.#rawMetaDataExpression.addChangeListener(bind(this, this.transformRawData));
    this.transformRawData(this.#rawMetaDataExpression);
  }

  addSection(item: MetaDataSection): void {
    this.#sections.push(item);
  }

  getSections(): Array<any> {
    return this.#sections;
  }

  protected transformRawData(ve: ValueExpression): void {
    this.#sections = [];
    const value: Array<any> = ve.getValue();
    if (Ext.isArray(value) && value.length > 0) {
      const result: Record<string, any> = {};
      value.forEach((item: any): void =>{
        const type: string = item.section;
        let metaDataSection: MetaDataSection = null;
        if (undefined === result[type]) {
          metaDataSection = new MetaDataSection(type);
          this.#sections.push(metaDataSection);
          result[type] = metaDataSection;
        } else {
          metaDataSection = result[type];
        }
        metaDataSection.addProperty(item.property, item.value);
      });
    }
    this.#metaDataExpression.setValue(this.#sections);
  }

  #hideOrShow(): void {
    this.setVisible(this.getSections().length > 0);
  }

  getMetaDataExpression(): ValueExpression {
    if (!this.#metaDataExpression) {
      this.#metaDataExpression = ValueExpressionFactory.createFromValue([]);
      this.#metaDataExpression.addChangeListener(bind(this, this.#hideOrShow));
    }
    return this.#metaDataExpression;
  }

  protected static getTemplateKey(mt: MetaDataSection): string {
    return mt.getMetaDataType();
  }

  protected override onDestroy(): void {
    this.#rawMetaDataExpression.removeChangeListener(bind(this, this.transformRawData));
    this.#metaDataExpression.removeChangeListener(bind(this, this.#hideOrShow));
    super.onDestroy();
  }
}

export default MetaDataDocumentFormBase;
