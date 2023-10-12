import { ckeditor5CoreMediaOemKey } from "@coremedia/ckeditor5-coremedia-oem-activation";
import type { EditorConfig } from "@ckeditor/ckeditor5-core";

/**
 * Configuration API for defining the license key for CKEditor 5 instances.
 */
// @ts-expect-error - licenseKey unavailable in interface for versions less than 38.x
export type EditorLicenseConfig = Pick<EditorConfig, "licenseKey">;

/**
 * Resolves to a license key suitable for use in instance creation of
 * CKEditor 5.
 *
 * The CoreMedia OEM key must be replaced by your purchased key to use
 * premium features of CKEditor 5.
 *
 * The CoreMedia OEM key must not be used outside CMCC.
 *
 * If required and available, you may update the OEM key by using
 * `pnpm update` for `@coremedia/ckeditor5-coremedia-oem-activation`.
 * Ensure to update to minor versions only.
 */
export const ckeditor5License: EditorLicenseConfig = {
  licenseKey: ckeditor5CoreMediaOemKey,
};
