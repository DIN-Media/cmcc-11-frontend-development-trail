import ResourceBundleUtil from "@jangaroo/runtime/l10n/ResourceBundleUtil";
import BlueprintProcessDefinitions_properties from "./BlueprintProcessDefinitions_properties";

/**
 * Overrides of ResourceBundle "BlueprintProcessDefinitions" for Locale "ja".
 * @see BlueprintProcessDefinitions_properties#INSTANCE
 */
ResourceBundleUtil.override(BlueprintProcessDefinitions_properties, {
  Translation_text: "翻訳",
  Translation_state_Translate_text: "翻訳ワークフローを承認",
  Translation_state_TranslateSelf_text: "翻訳ワークフローを開始",
  Translation_state_sendToTranslationService_text: "翻訳サービスへ送る",
  Translation_state_rollbackTranslation_text: "変更を拒否",
  Translation_state_finishTranslation_text: "コンテンツのローカリゼーションを終了",
  Translation_state_Review_text: "翻訳を見直す",
  Translation_state_translationReviewed_text: "コンテンツのローカリゼーションを終了 (翻訳を見直し済み)",
  Translation_task_Translate_text: "翻訳する",
  Translation_task_TranslateSelf_text: "翻訳する",
  Translation_task_Review_text: "見直す",
  Synchronization_text: "同期",
  Synchronization_state_Synchronize_text: "同期する",
  Synchronization_state_finishSynchronization_text: "同期が終了しました",
});
