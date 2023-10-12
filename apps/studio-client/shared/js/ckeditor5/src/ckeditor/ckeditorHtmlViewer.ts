import { ClassicEditor } from '@ckeditor/ckeditor5-editor-classic';
import { GeneralHtmlSupport } from '@ckeditor/ckeditor5-html-support';

import {
  CKEditorPluginConfig,
  CreateCKEditorFunction
} from "@coremedia/studio-client.ckeditor-common/CKEditorCreateFunctionType";
import '../theme/custom.css';
import { ckeditor5License } from "./ckeditor5License";

/**
 * This is an HTML viewer configuration for the CKEditor 5 in CoreMedia Studio.
 * Please be advised, that any misconfiguration of this editor might cause issues in CoreMedia Studio.
 *
 * If you want to add a custom configuration, you should create a separate module
 * and add the exported editor in {@link ckeditor.ts} accordingly.
 *
 * IMPORTANT:
 *
 * This editor type is used to display HTML content inside the editor.
 *
 * When content from external sources (such as third-party commerce systems) is
 * displayed, this can pose a serious security risk regarding stored
 * Cross-Site-Scripting (XSS), because malicious scripts could be executed
 * inside CoreMedia Studio, as well as element styling may corrupt the
 * Studio layout.
 *
 * Please see the `htmlSupport` configuration below.
 */
export const createHtmlViewerCKEditor: CreateCKEditorFunction = (domElement:(string | HTMLElement), pluginConfig: CKEditorPluginConfig): Promise<ClassicEditor> => {
  // @ts-expect-error - CoreMedia Configuration Types (Augmentation) needs to be fixed.
  return ClassicEditor.create(domElement, {
    ...ckeditor5License,
    plugins: [
      GeneralHtmlSupport,
    ],
    toolbar: {
      items: []
    },
    htmlSupport: {
      /**
       * Please note that a misconfiguration of the `htmlSupport` configuration
       * can pose a potential security risk.
       *
       * This configuration is only a rough proposal and should be adjusted to
       * meet the individual requirements of the HTML content provided by your
       * external sources.
       *
       * As a default, this configuration allows all HTML elements, attributes,
       * classes and styles with the following exceptions:
       *
       * * The `<script>` and `<style>` tags are not allowed.
       * * Attributes starting with "on" are not allowed. (e.g., `onclick`,
       *   `onload`, `onmouseover`)
       *
       * Along with that, you may want, for example, disallow `style` and
       * `class` attributes, which may conflict with Studio UI such as
       * floating elements.
       *
       * Please find more information on XSS attacks and how to prevent them here:
       *
       * * [Cross Site Scripting Prevention - HTML Sanitization](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html#html-sanitization)
       *
       * Sanitizers such as `DOMPurify` could be added as data processor replacing
       * the default `HtmlDataProcessor` provided by CKEditor 5.
       */
      allow: [
        {
          name: /.*/,
          attributes: true,
          classes: true,
          styles: true
        }
      ],
      disallow: [
        {
          name: /^(script|style)$/,
        },
        {
          attributes: /^on\w+/,
        }
      ]
    }
  });
}
