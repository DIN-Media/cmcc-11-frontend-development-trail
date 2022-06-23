import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";

export default class SelectionState {

  /**
   * a private field that holds the selected navigation context
   */
  #selectionExpression: ValueExpression;

  constructor() {
    this.#selectionExpression = ValueExpressionFactory.createFromValue([]);
  }

  /**
   * Getter for the field #selectionExpression
   */
  getSelectionExpression(): ValueExpression {
    return this.#selectionExpression;
  }
}
