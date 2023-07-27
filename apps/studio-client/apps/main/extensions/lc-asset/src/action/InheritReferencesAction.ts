import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import InheritReferencesActionBase from "./InheritReferencesActionBase";

interface InheritReferencesActionConfig extends Config<InheritReferencesActionBase>, Partial<Pick<InheritReferencesAction,
  "bindTo" |
  "forceReadOnlyValueExpression" |
  "inheritExpression" |
  "referencesExpression" |
  "originReferencesExpression"
>> {
}

/**
 * An action to toggle the inherit state of the placement.
 */
class InheritReferencesAction extends InheritReferencesActionBase {
  declare Config: InheritReferencesActionConfig;

  constructor(config: Config<InheritReferencesAction> = null) {
    super(ConfigUtils.apply(Config(InheritReferencesAction), config));
  }

  /**
   * content form content.
   */
  bindTo: ValueExpression = null;

  /**
   * An optional ValueExpression which makes the action read-only if it is evaluated to true.
   */
  forceReadOnlyValueExpression: ValueExpression = null;

  /**
   * An optional ValueExpression for the inherit flag. When specified the action will operate on this expression
   * instead of on the expression pointing to the inherit struct of the content
   */
  inheritExpression: ValueExpression = null;

  /**
   * An optional ValueExpression for the catalog object references. When specified the action will operate on this expression
   * instead of on the expression pointing to the catalog object struct list of the content
   */
  referencesExpression: ValueExpression = null;

  /**
   * An optional ValueExpression for the origin catalog object references. When specified the action will operate on this expression
   * instead of on the expression pointing to the origin catalog object struct list of the content
   */
  originReferencesExpression: ValueExpression = null;
}

export default InheritReferencesAction;
