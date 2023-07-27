import CatalogObject from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/CatalogObject";
import Category from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Category";
import Product from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/Product";
import ProductVariant from "@coremedia-blueprint/studio-client.main.ec-studio-model/model/ProductVariant";
import ConverterTargetKeys from "@coremedia/studio-client.cap-base-models/converter/ConverterTargetKeys";
import ItemConverter from "@coremedia/studio-client.cap-base-models/converter/ItemConverter";
import ToContentConverterHint from "@coremedia/studio-client.cap-base-services-api/converter/content/ToContentConverterHint";
import ItemConverterHint from "@coremedia/studio-client.cap-base-services-api/converter/ItemConverterHint";
import jobService from "@coremedia/studio-client.cap-rest-client/common/jobService";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import sitesService from "@coremedia/studio-client.multi-site-models/global/sitesService";
import Ext from "@jangaroo/ext-ts";
import { as, is, mixin } from "@jangaroo/runtime";
import { AnyFunction } from "@jangaroo/runtime/types";
import LivecontextStudioPluginBase from "../LivecontextStudioPluginBase";
import AugmentationJob from "../job/AugmentationJob";

class CatalogItemsToToContentConverter implements ItemConverter {

  static readonly ID: string = "catalogItemsToContentConverter";

  getId(): string {
    return CatalogItemsToToContentConverter.ID;
  }

  getTargetKey(): string {
    return ConverterTargetKeys.CONTENT;
  }

  handles(item: any): Promise<boolean> {
    return new Promise((resolve: AnyFunction): void => {
      resolve(is(item, Product) || is(item, Category));
    });
  }

  computeHints(items: Array<any>, options: any = null): Promise<ItemConverterHint[]> {
    return new Promise((resolve: AnyFunction): void => {
      resolve(items.map((item: CatalogObject): ToContentConverterHint => {
        const contentConverterHint = new ToContentConverterHint();
        contentConverterHint.sourceId = item.getUriPath();
        contentConverterHint.targetContentType = CatalogItemsToToContentConverter.#getTargetContentType(item);
        contentConverterHint.targetFolderUriPath = CatalogItemsToToContentConverter.#getTargetFolderPath(item, options);
        contentConverterHint.cacheable = true;
        return contentConverterHint;
      }));
    });
  }

  static #getTargetFolderPath(catalogObject: CatalogObject, options: any): string {
    if (is(catalogObject, ProductVariant)) {
      return ! !options && ! !options.targetFolder ? as(options.targetFolder, Content).getUriPath() : null;
    }

    return null;
  }

  static #getTargetContentType(item: CatalogObject): string {
    if (is(item, ProductVariant)) {
      return LivecontextStudioPluginBase.CONTENT_TYPE_PRODUCT_TEASER;
    } else if (is(item, Product)) {
      return LivecontextStudioPluginBase.CONTENT_TYPE_EXTERNAL_PRODUCT;
    } else if (is(item, Category)) {
      return LivecontextStudioPluginBase.CONTENT_TYPE_EXTERNAL_CHANNEL;
    }

    return null;
  }

  convert(commerceObjects: Array<any>, options: any = null): Promise<any[]> {
    return new Promise((resolve: AnyFunction): void => {
      const targetFolder = CatalogItemsToToContentConverter.#evaluateTargetFolder(options);

      commerceObjects.forEach((catalogObject: CatalogObject): void => {

        const augmentationJob = new AugmentationJob(catalogObject, targetFolder);
        jobService._.executeJob(augmentationJob, (content: Content): void => {
          content.checkIn();
          resolve([content]);
        }, Ext.emptyFn);
      });
    });
  }

  static #evaluateTargetFolder(options: any): Content {
    if (options && ! !options.targetFolder) {
      return options.targetFolder;
    }

    const preferredSite = sitesService._.getPreferredSite();
    if (preferredSite) {
      return preferredSite.getSiteRootFolder();
    }

    return null;
  }
}
mixin(CatalogItemsToToContentConverter, ItemConverter);

export default CatalogItemsToToContentConverter;
