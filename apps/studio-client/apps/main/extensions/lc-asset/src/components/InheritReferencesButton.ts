import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import InheritReferencesAction from "../action/InheritReferencesAction";

interface InheritReferencesButtonConfig extends Config<IconButton>, Partial<Pick<InheritReferencesButton,
  "bindTo" |
  "forceReadOnlyValueExpression"
>> {
}

class InheritReferencesButton extends IconButton {
  declare Config: InheritReferencesButtonConfig;

  static override readonly xtype: string = "com.coremedia.livecontext.asset.studio.config.inheritReferencesButton";

  constructor(config: Config<InheritReferencesButton> = null) {
    super(ConfigUtils.apply(Config(InheritReferencesButton, {
      enableToggle: true,

      baseAction: new InheritReferencesAction({
        bindTo: config.bindTo,
        forceReadOnlyValueExpression: config.forceReadOnlyValueExpression,
      }),

    }), config));
  }

  /**
   * content form content.
   */
  bindTo: ValueExpression = null;

  /**
   * An optional ValueExpression which makes the action read-only if it is evaluated to true.
   */
  forceReadOnlyValueExpression: ValueExpression = null;
}

export default InheritReferencesButton;
