import { Essentials } from '@ckeditor/ckeditor5-essentials';
import { Alignment } from '@ckeditor/ckeditor5-alignment';
import { Indent } from '@ckeditor/ckeditor5-indent';
import { BlockQuote } from '@ckeditor/ckeditor5-block-quote';
import { Heading } from '@ckeditor/ckeditor5-heading';
import { AutoLink, Link, LinkImage } from '@ckeditor/ckeditor5-link';
// ImageInline: See ckeditor/ckeditor5#12027.
import ImageInline from "@ckeditor/ckeditor5-image/src/imageinline";
// ImageBlockEditing: See ckeditor/ckeditor5#12027.
import ImageBlockEditing from "@ckeditor/ckeditor5-image/src/image/imageblockediting";
import { ImageTextAlternative, ImageToolbar, ImageStyle } from "@ckeditor/ckeditor5-image";
import { Paragraph } from '@ckeditor/ckeditor5-paragraph';
import { Table, TableToolbar } from '@ckeditor/ckeditor5-table';
import { RemoveFormat } from '@ckeditor/ckeditor5-remove-format';
import { Bold, Italic, Code, Strikethrough, Subscript, Superscript, Underline } from '@ckeditor/ckeditor5-basic-styles';
import { Autoformat } from '@ckeditor/ckeditor5-autoformat';
import { FindAndReplace } from '@ckeditor/ckeditor5-find-and-replace';
import { CodeBlock } from '@ckeditor/ckeditor5-code-block';

import { Autosave } from '@ckeditor/ckeditor5-autosave';
import { icons } from '@ckeditor/ckeditor5-core';
import { localization, localize } from "../lang/LocalizationUtils";
import LocaleUtil from "@coremedia/studio-client.cap-base-models/locale/LocaleUtil";
import { CKEditorPluginConfig, CreateCKEditorFunction } from "@coremedia/studio-client.ckeditor-common/CKEditorCreateFunctionType";
import '../theme/custom.css';
import { PasteFromOffice } from "@ckeditor/ckeditor5-paste-from-office";
import { SourceEditing } from "@ckeditor/ckeditor5-source-editing";
import CKEditorFeatureFlags from "@coremedia/studio-client.ckeditor-common/CKEditorFeatureFlags";
import { COREMEDIA_LINK_CONFIG_KEY, ContentLinks, LinkTarget } from "@coremedia/ckeditor5-coremedia-link";
import { linkAttributesConfig } from "./linkAttributesConfig";
import { ContentClipboard } from "@coremedia/ckeditor5-coremedia-content-clipboard";
import { ContentImagePlugin } from "@coremedia/ckeditor5-coremedia-images";
import { CoreMediaStudioEssentials } from "@coremedia/ckeditor5-coremedia-studio-essentials";
import { Differencing } from "@coremedia/ckeditor5-coremedia-differencing";
import { LinkAttributes } from "@coremedia/ckeditor5-link-common";
import { PasteContentPlugin } from "@coremedia/ckeditor5-coremedia-content-clipboard";
import { DocumentList } from "@ckeditor/ckeditor5-list";
import { FontMapper as CoreMediaFontMapper} from "@coremedia/ckeditor5-font-mapper";
import { ClassicEditor } from "@ckeditor/ckeditor5-editor-classic";
import { ckeditor5License } from "./ckeditor5License";

const {
  objectInline: withinTextIcon,
  objectLeft: alignLeftIcon,
  objectRight: alignRightIcon,
  objectSizeFull: pageDefaultIcon
} = icons;

/**
 * Localization for the image alignment entries in
 * the editor configuration.
 */
localization.add({
  "de": {
    "Left-aligned": "Linksbündig",
    "Right-aligned": "Rechtsbündig",
    "Within Text": "Im Text",
    "Page default": "Standardeinstellung",
    "Type your text here...": "Text hier eingeben...",
    "More formatting": "Weitere Formatierung",
    "Paragraph": "Absatz",
    "Heading 1": "Überschrift 1",
    "Heading 2": "Überschrift 2",
    "Heading 3": "Überschrift 3",
  }
})

/**
 * This is the default configuration for the CKEditor 5 in CoreMedia Studio.
 * Please be advised, that any misconfiguration of this editor might cause issues in CoreMedia Studio.
 *
 * If you want to add a custom configuration, you should create a separate module
 * and add the exported editor in {@link ckeditor.ts} accordingly.
 *
 * **Available Feature Flags:**
 *
 * * `administrative`
 *
 *   Adds administrative capabilities to the provided CKEditor instance, such as
 *   source editing.
 */
export const createDefaultCKEditor: CreateCKEditorFunction = (domElement:(string | HTMLElement), pluginConfig: CKEditorPluginConfig): Promise<ClassicEditor> => {
  const language = LocaleUtil.getLocale();
  const defaultToolbarItems = [
    'undo',
    'redo',
    '|',
    'heading',
    '|',
    'bold',
    'italic',
    'underline',
    {
      label: localize('More formatting', language),
      icon: 'threeVerticalDots',
      items: [ 'strikethrough', 'subscript', 'superscript', 'code' ]
    },
    '|',
    'removeFormat',
    '|',
    'link',
    '|',
    'alignment',
    'blockQuote',
    'codeBlock',
    '|',
    'insertTable',
    '|',
    'numberedList',
    'bulletedList',
    'outdent',
    'indent',
    '|',
    'pasteContent',
    'findAndReplace'
  ];
  if (pluginConfig.featureFlags?.includes(CKEditorFeatureFlags.ADMINISTRATIVE)) {
    defaultToolbarItems.push("|", "sourceEditing");
  }
  return ClassicEditor.create(domElement, {
    ...ckeditor5License,
    placeholder: localize('Type your text here...', language),
    plugins: [
      Alignment,
      Autoformat,
      AutoLink,
      Autosave,
      BlockQuote,
      Bold,
      Code,
      CodeBlock,
      ContentLinks,
      ContentClipboard,
      ContentImagePlugin,
      CoreMediaStudioEssentials,
      CoreMediaFontMapper,
      Differencing,
      Essentials,
      FindAndReplace,
      Heading,
      ImageInline,
      ImageBlockEditing,
      ImageStyle,
      ImageToolbar,
      ImageTextAlternative,
      Indent,
      Italic,
      Link,
      LinkAttributes,
      LinkImage,
      LinkTarget,
      DocumentList,
      Paragraph,
      PasteContentPlugin,
      PasteFromOffice,
      RemoveFormat,
      SourceEditing,
      Strikethrough,
      Subscript,
      Superscript,
      Table,
      TableToolbar,
      Underline,
    ],
    toolbar: defaultToolbarItems,
    heading: {
      options: [
        { model: 'paragraph', title: localize('Paragraph', language), class: 'ck-heading_paragraph' },
        { model: 'heading1', view: 'h1', title: localize('Heading 1', language), class: 'ck-heading_heading1' },
        { model: 'heading2', view: 'h2', title: localize('Heading 2', language), class: 'ck-heading_heading2' },
        { model: 'heading3', view: 'h3', title: localize('Heading 3', language), class: 'ck-heading_heading3' },
      ],
    },
    link: {
      defaultProtocol: 'https://',
      ...linkAttributesConfig,
    },
    alignment: {
      options: [
        {
          name: "left",
          className: "align--left",
        },
        {
          name: "right",
          className: "align--right",
        },
        {
          name: "center",
          className: "align--center",
        },
        {
          name: "justify",
          className: "align--justify",
        },
      ],
    },
    [COREMEDIA_LINK_CONFIG_KEY]: {
      linkBalloon: {
        // Clicking these elements will not trigger closing the link balloon:
        keepOpen: {
          // library, quick-search (+buttons in header toolbar)
          ids: ["collection-view-container", "side-panel-window_collection-view-container", "quickSearchDialog", "libraryButton", "QuickSearchButtonId", "controlRoomButtonId"],
          // input field menus in the library:
          classes: ["x-boundlist"],
        },
      },
    },
    image: {
      styles: {
        // Defining custom styling options for the images.
        options: [
          {
            name: 'float-left',
            icon: alignLeftIcon,
            title: localize('Left-aligned', language),
            className: 'float--left',
            modelElements: [ 'imageInline' ]
          },
          {
            name: 'float-right',
            icon: alignRightIcon,
            title: localize('Right-aligned', language),
            className: 'float--right',
            modelElements: [ 'imageInline' ]
          },
          {
            name: 'float-none',
            icon: withinTextIcon,
            title: localize('Within Text', language),
            className: 'float--none',
            modelElements: [ 'imageInline' ]
          },
          //@ts-expect-error missing the attribute modelElements which is optional.
          {
            name: 'inline',
            title: localize('Page default', language),
            icon: pageDefaultIcon,
          }
        ]
      },
      toolbar: [
        'imageStyle:float-left',
        'imageStyle:float-right',
        'imageStyle:float-none',
        'imageStyle:inline',
        "|",
        "linkImage",
        "imageTextAlternative",
        "contentImageOpenInTab",
      ]
    },
    table: {
      contentToolbar: [
        'tableRow',
        'tableColumn',
        'mergeTableCells'
      ]
    },
    language: language,
    autosave: {
      save: pluginConfig.autosave.save,
      waitingTime: 1000,
    }
  });
}
