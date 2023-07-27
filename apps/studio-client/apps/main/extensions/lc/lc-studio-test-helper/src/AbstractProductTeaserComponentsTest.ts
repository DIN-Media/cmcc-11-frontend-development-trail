import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ContentType from "@coremedia/studio-client.cap-rest-client/content/ContentType";
import RemoteBean from "@coremedia/studio-client.client-core/data/RemoteBean";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import PropertyEditorUtil from "@coremedia/studio-client.main.editor-components/sdk/util/PropertyEditorUtil";
import { as } from "@jangaroo/runtime";
import { AnyFunction } from "@jangaroo/runtime/types";

/**
 * Basis test class. Extend this class to test components related to product teaser
 */
class AbstractProductTeaserComponentsTest extends AbstractCatalogTest {
  protected productTeaser: Content = null;

  #bindTo: ValueExpression = null;

  #propertyExpression: ValueExpression = null;

  #createReadOnlyValueExpression: AnyFunction = null;

  #forceReadOnlyValueExpression: ValueExpression = null;

  override setUp(): void {
    super.setUp();

    this.resetCatalogHelper();

    this.createPlugin();

    this.productTeaser = as(beanFactory._.getRemoteBean("content/100"), Content);
    //we need to mock the write access
    this.productTeaser.getRepository().getAccessControl().mayWrite = ((): boolean => true);

    this.#bindTo = ValueExpressionFactory.createFromValue(this.productTeaser);
    this.#propertyExpression = this.#bindTo.extendBy("properties", "externalId");
    this.#forceReadOnlyValueExpression = ValueExpressionFactory.createFromValue(false);

    //Mock PropertyEditorUtil#createReadOnlyValueExpression
    this.#createReadOnlyValueExpression = PropertyEditorUtil.createReadOnlyValueExpression;
    PropertyEditorUtil.createReadOnlyValueExpression = ((contentValueExpression: ValueExpression, forceReadOnlyValueExpression?: ValueExpression): ValueExpression =>
      ValueExpressionFactory.createFromFunction((): boolean => {
        if (forceReadOnlyValueExpression && forceReadOnlyValueExpression.getValue()) {
          return true;
        }
        if (!contentValueExpression) {
          return false;
        }
        const mayWrite: any = true;
        return mayWrite === undefined ? undefined : !mayWrite;
      })

    );
  }

  protected createPlugin(): void {
    // Effectively abstract, must be implemented
  }

  override tearDown(): void {
    super.tearDown();
    PropertyEditorUtil.createReadOnlyValueExpression = this.#createReadOnlyValueExpression;
  }

  protected async waitForProductTeaserToBeLoaded(): Promise<void> {
    // Wait for the product teaser to be loaded:
    await this.productTeaser.load();
  }

  protected async waitForProductTeaserContentTypeToBeLoaded(): Promise<void> {
    // Wait for the product teaser content type to be loaded:
    await (this.productTeaser.getType() as ContentType & RemoteBean).load();
  }

  protected setLink(value: string): void {
    // set product link to <value>:
    this.#propertyExpression.setValue(value);
  }

  protected setForceReadOnly(value: boolean): void {
    // set forceReadOnlyValueExpression to <value>:
    this.#forceReadOnlyValueExpression.setValue(value);
  }

  protected getBindTo(): ValueExpression {
    return this.#bindTo;
  }

  protected getForceReadOnlyValueExpression(): ValueExpression {
    return this.#forceReadOnlyValueExpression;
  }
}

export default AbstractProductTeaserComponentsTest;
