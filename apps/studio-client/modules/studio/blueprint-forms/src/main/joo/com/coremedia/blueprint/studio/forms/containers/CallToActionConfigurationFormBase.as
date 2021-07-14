package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.components.IAnnotatedLinkListForm;

/**
 * Fires after the configuration has changed.
 */
[Event(name="CTAConfigurationChanged")] // NOSONAR - no type

public class CallToActionConfigurationFormBase extends PropertyFieldGroup implements IAnnotatedLinkListForm {

  public static const CTA_CONFIGURATION_CHANGED_EVENT:String = "CTAConfigurationChanged";
  internal static const TEXT_ITEM_ID:String = "customCTAText";
  internal static const HASH_ITEM_ID:String = "CTAHash";

  /**
   * A value expression that leads to a bean storing the {@link CallToActionSettings}.
   */
  private var _settingsVE:ValueExpression;

  /**
   * If TRUE legacy CTA settings (described in {@link CallToActionSettings}) are used.
   */
  [ExtConfig]
  public var useLegacyCTASettings:Boolean;

  private var _ctaSettings:CallToActionSettings;

  private var _ctaViewModel:CallToActionViewModel;

  public function CallToActionConfigurationFormBase(config:CallToActionConfigurationForm = null) {
    super(config);
    ctaSettings.addListener(CallToActionSettings.CALL_TO_ACTION_ENABLED_PROPERTY_NAME, ctaConfigurationChangedListener);
    ctaSettings.addListener(CallToActionSettings.CALL_TO_ACTION_CUSTOM_TEXT_PROPERTY_NAME, ctaConfigurationChangedListener);
    ctaSettings.addListener(CallToActionSettings.CALL_TO_ACTION_HASH_PROPERTY_NAME, ctaConfigurationChangedListener);
    if (settingsVE.isLoaded()) {
      ctaConfigurationChangedListener();
    }
  }

  [Bindable]
  internal function get ctaSettings():CallToActionSettings {
    if (!_ctaSettings) {
      _ctaSettings = new CallToActionSettings(settingsVE, useLegacyCTASettings);
    }
    return _ctaSettings;
  }

  [Bindable]
  internal function get ctaViewModel():CallToActionViewModel {
    if (!_ctaViewModel) {
      _ctaViewModel = new CallToActionViewModel();
    }
    return _ctaViewModel;
  }

  private function ctaConfigurationChangedListener():void {
    fireEvent(CTA_CONFIGURATION_CHANGED_EVENT);
  }

  override protected function onDestroy():void {
    ctaViewModel.destroy();
    ctaSettings.destroy();
    super.onDestroy();
  }

  [Bindable]
  public function set settingsVE(settingsVE:ValueExpression):void {
    _settingsVE = settingsVE;
  }

  [Bindable]
  public function get settingsVE():ValueExpression {
    return _settingsVE;
  }
}
}
