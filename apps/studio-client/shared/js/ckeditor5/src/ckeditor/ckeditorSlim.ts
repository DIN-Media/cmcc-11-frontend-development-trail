import { ClassicEditor } from '@ckeditor/ckeditor5-editor-classic';

import { Essentials } from '@ckeditor/ckeditor5-essentials';
import { Differencing } from "@coremedia/ckeditor5-coremedia-differencing";
import { CoreMediaStudioEssentials } from "@coremedia/ckeditor5-coremedia-studio-essentials";
import { FontMapper as CoreMediaFontMapper} from '@coremedia/ckeditor5-font-mapper';

import { localization, localize } from "../lang/LocalizationUtils";
import LocaleUtil from "@coremedia/studio-client.cap-base-models/locale/LocaleUtil";
import { CKEditorPluginConfig, CreateCKEditorFunction } from "@coremedia/studio-client.ckeditor-common/CKEditorCreateFunctionType";
import '../theme/custom.css';
import { Autosave } from "@ckeditor/ckeditor5-autosave";
import { Paragraph } from '@ckeditor/ckeditor5-paragraph';
import { ckeditor5License } from "./ckeditor5License";

/**
 * Localization the editor configuration
 */
localization.add({
  "de": {
    "Type your text here...": "Text hier eingeben..."
  }
})

/**
 * This is the slim configuration for the CKEditor 5 in CoreMedia Studio.
 * Please be advised, that any misconfiguration of this editor might cause issues in CoreMedia Studio.
 *
 * If you want to add a custom configuration, you should create a separate module
 * and add the exported editor in {@link ckeditor.ts} accordingly.
 */
export const createSlimCKEditor: CreateCKEditorFunction = (domElement:(string | HTMLElement), pluginConfig: CKEditorPluginConfig): Promise<ClassicEditor> => {
  const defaultToolbarItems = [
    'undo',
    'redo',
    ];
  const language = LocaleUtil.getLocale();
  // @ts-expect-error - CoreMedia Configuration Types (Augmentation) needs to be fixed.
  return ClassicEditor.create(domElement, {
    ...ckeditor5License,
    placeholder: localize('Type your text here...', language),
    plugins: [
      Autosave,
      CoreMediaStudioEssentials,
      CoreMediaFontMapper,
      Differencing,
      Essentials,
      Paragraph,
    ],
    toolbar: {
      items: defaultToolbarItems
    },
    language: language,
    autosave: {
      save: pluginConfig.autosave.save,
      waitingTime: 1000,
    }
  });
}
