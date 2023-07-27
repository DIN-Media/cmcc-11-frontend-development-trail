import CapPropertyDescriptorUtil
  from "@coremedia/studio-client.cap-rest-client-impl/common/descriptors/impl/CapPropertyDescriptorUtil";
import ContentImpl from "@coremedia/studio-client.cap-rest-client-impl/content/impl/ContentImpl";
import ContentRepositoryImpl from "@coremedia/studio-client.cap-rest-client-impl/content/impl/ContentRepositoryImpl";
import {
  $BooleanPropertyDescriptor,
  $StringListPropertyDescriptor,
  $StructPropertyDescriptor,
} from "@coremedia/studio-client.cap-rest-client-test-helper/protocol/common";
import {
  $ContentRef,
  $ContentTypeRef,
  ContentRightsForUserUri,
  ContentUri,
  makeDocumentCall,
  makeContentRightsForUserCall,
} from "@coremedia/studio-client.cap-rest-client-test-helper/protocol/content";
import CapSession from "@coremedia/studio-client.cap-rest-client/common/CapSession";
import session from "@coremedia/studio-client.cap-rest-client/common/session";
import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import ContentRepository from "@coremedia/studio-client.cap-rest-client/content/ContentRepository";
import ContentType from "@coremedia/studio-client.cap-rest-client/content/ContentType";
import ContentTypeNames from "@coremedia/studio-client.cap-rest-client/content/ContentTypeNames";
import Right from "@coremedia/studio-client.cap-rest-client/content/authorization/Right";
import Group from "@coremedia/studio-client.cap-rest-client/user/Group";
import User from "@coremedia/studio-client.cap-rest-client/user/User";
import WorkflowContentService from "@coremedia/studio-client.cap-rest-client/workflow/WorkflowContentService";
import WorkflowRepository from "@coremedia/studio-client.cap-rest-client/workflow/WorkflowRepository";
import BeanFactoryImpl from "@coremedia/studio-client.client-core-impl/data/impl/BeanFactoryImpl";
import RemoteBeanCache from "@coremedia/studio-client.client-core-impl/data/impl/RemoteBeanCache";
import RemoteService from "@coremedia/studio-client.client-core-impl/data/impl/RemoteService";
import AbstractRemoteTest from "@coremedia/studio-client.client-core-test-helper/AbstractRemoteTest";
import MockFetch, { MockCall } from "@coremedia/studio-client.client-core-test-helper/MockFetch";
import Locale from "@coremedia/studio-client.client-core/data/Locale";
import RemoteBeanUtil from "@coremedia/studio-client.client-core/data/RemoteBeanUtil";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import RequestCounter from "@coremedia/studio-client.client-core/util/RequestCounter";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Site from "@coremedia/studio-client.multi-site-models/Site";
import { as, mixin } from "@jangaroo/runtime";
import Class from "@jangaroo/runtime/Class";
import joo from "@jangaroo/runtime/joo";
import { AnyFunction } from "@jangaroo/runtime/types";

class AbstractCatalogTest extends AbstractRemoteTest {

  static readonly ORANGES_EXTERNAL_ID: string = "GFR033_3301";

  static readonly ORANGES_SKU_EXTERNAL_ID: string = "GFR033_330101";

  static readonly BABY_SHOES_EXTERNAL_ID: string = "BSH016_1605";

  static readonly MARKETING_SPOT_EXTERNAL_ID: string = "spot1";

  static readonly MARKETING_SPOT_ID: string = "ibm:///catalog/marketingspot/" + AbstractCatalogTest.MARKETING_SPOT_EXTERNAL_ID;

  static readonly ORANGES_ID: string = "ibm:///catalog/product/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID;

  static readonly SKU_ID_PREFIX: string = "ibm:///catalog/sku/";

  static readonly ORANGES_SKU_ID: string = AbstractCatalogTest.SKU_ID_PREFIX + AbstractCatalogTest.ORANGES_SKU_EXTERNAL_ID;

  static readonly ORANGES_NAME: string = "Oranges";

  static readonly ORANGES_SHORT_DESC: string = "Organic and full of flavor oranges";

  static readonly ORANGES_SKU_NAME: string = "Oranges SKU";

  static readonly ORANGES_SKU_SHORT_DESC: string = "Organic and full of flavor oranges SKU";

  static readonly ORANGES_IMAGE_URI: string = "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/homefurnishings/hta029_tableware/200x310/hta029_2932.jpg";

  static readonly HERMITAGE_RUCHED_BODICE_COCKTAIL_DRESS: string = "Hermitage Ruched Bodice Cocktail Dress";

  static readonly PRODUCT1_FROM_XMP_ID: string = "ibm:///catalog/product/xmp1";

  static readonly PRODUCT2_FROM_XMP_ID: string = "ibm:///catalog/product/xmp2";

  #contentRepository: ContentRepositoryImpl = null;

  #preferredSiteExpression: ValueExpression = null;

  static {
    EditorContextImpl.initEditorContext();
  }

  protected resetCatalogHelper(): void {
    const clazz: Class = joo.getQualifiedObject("com.coremedia.ecommerce.studio.helper.CatalogHelper");
    joo.getQualifiedObject("com.coremedia.ecommerce.studio").catalogHelper = new clazz();
  }

  override setUp(): void {
    super.setUp();
    const CONTENT_ROOT: Content = new ContentImpl("content/1", { id: 1 });
    RequestCounter.reset();
    BeanFactoryImpl.initBeanFactory();
    CapPropertyDescriptorUtil.registerResolver();
    this.#contentRepository = as(beanFactory._.getRemoteBean("content"), ContentRepositoryImpl);
    this.#contentRepository.getRoot = ((): Content =>
      CONTENT_ROOT
    );

    const preferredSite: Record<string, any> = {
      getName: (): string =>
        this.#preferredSiteExpression.getValue()
      ,
      getSiteRootFolder: (): Content =>
        this.#contentRepository.getRoot()
      ,
      getLocale: (): Locale =>
        new Locale({ "displayName": "English" })
      ,
      getMasterSite: (): Site =>
        null
      ,
      getDerivedSites: (): Array<any> =>
        []
      ,
      getId: (): string =>
        this.#preferredSiteExpression.getValue(),

    };

    this.#contentRepository.getAccessControl = ((): any =>
      ({
        mayPerform: (_content: Content, _right: Right): any =>
          true
        ,
        mayPerformForType: (_content: Content, _contentType: ContentType, _right: Right): any =>
          true
        ,
        mayCreate: (_content: Content, _right: Right): any =>
          true
        ,
        filterReadableContents: (contents: Array<any>): any =>
          contents
        ,
        mayCopy: (_contents: Array<any>, _target: Content): any =>
          true
        ,
        mayMove: (_contents: Array<any>, _target: Content): any =>
          true
        ,
        mayWrite: (_contents: Array<any>, _target: Content): any =>
          true
        ,
        mayRename: (_contents: Array<any>, _target: Content): any =>
          true
        ,
        isWritable: (_content: Content): any =>
          true,

      })
    );

    session._ = Object.setPrototypeOf({
      getConnection: (): any =>
        ({
          getContentRepository: (): ContentRepository =>
            this.#contentRepository
          ,
          getWorkflowRepository: (): WorkflowRepository =>
            Object.setPrototypeOf({
              getWorkflowContentService: (): WorkflowContentService =>
                Object.setPrototypeOf({
                  isLockedForUser: (_content: Content): boolean =>
                    false,

                }, mixin(class {}, WorkflowContentService).prototype),

            }, mixin(class {}, WorkflowRepository).prototype),

          calculateRequestURI: (uri: string): string =>
            RemoteService.calculateRequestURI(uri)
          ,
        })
      ,
      getUser: (): User =>
        //noinspection JSUnusedGlobalSymbols
        Object.setPrototypeOf({
          isMemberOf: (group: Group, callback: AnyFunction): void =>
            EventUtil.invokeLater(callback, true)
          ,
          getHomeFolder: (): Content =>
            new ContentImpl("content/5", { id: 5 })
          ,
          getUriPath: (): string =>
            "user/1"
          ,
          isAdministrative: (): boolean =>
            true,

        }, mixin(class {}, User).prototype),

    }, mixin(class {}, CapSession).prototype);
    this.#preferredSiteExpression = ValueExpressionFactory.createFromValue("HeliosSiteId"); //HELIOS

    (editorContext._ as unknown)["getSitesService"] = ((): any =>
      ({
        getPreferredSiteIdExpression: (): ValueExpression =>
          this.#preferredSiteExpression
        ,
        getPreferredSiteId: (): string =>
          this.#preferredSiteExpression.getValue()
        ,
        getPreferredSiteName: (): string =>
          this.#preferredSiteExpression.getValue()
        ,
        getPreferredSite: (): any =>
          preferredSite
        ,
        getSiteIdFor: (_content: Content): string =>
          this.#preferredSiteExpression.getValue()
        ,
        getSiteFor: (_content: Content): any =>
          preferredSite
        ,
        getSites: (): Array<any> =>
          [preferredSite]
        ,
        getSite: (_siteId: string): any =>
          preferredSite,

      })
    );
    //Reset the collection view model
    EditorContextImpl.getInstance().getCollectionViewModel(true);
    RemoteBeanCache.disposeAll();

    const workArea: Record<string, any> = {};
    workArea["getEntityTabTypes"] = ((): Array<any> =>
      []
    );
    (editorContext._ as unknown)["getWorkArea"] = ((): any =>
      workArea
    );
  }

  protected override getMockCalls(): MockCall[] {
    return AbstractCatalogTest.MOCK_CALLS;
  }

  override tearDown(): void {
    super.tearDown();
    MockFetch.destroyMock();
    RequestCounter.reset();
    RemoteBeanCache.disposeAll();
  }

  protected async waitForContentRepositoryLoaded(): Promise<void> {
    await this.#contentRepository.load();
  }

  protected async waitForContentTypesLoaded(): Promise<void> {
    // load content types:
    await RemoteBeanUtil.loadAll(...this.#contentRepository.getContentTypes());
  }

  static readonly MOCK_CALLS: MockCall[] = [
    {
      "request": {
        "uri": "upload/config?site=HeliosSiteId",
        "method": "GET",
      },
      "response": { "body": {} },
    },
    {
      "request": {
        "uri": "livecontext/store/HeliosSiteId",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "PerfectChefESite",
          "id": "ibm:///catalog/store/10851",
          "topLevel": [
            { "$Ref": "livecontext/marketing/HeliosSiteId" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/ROOT" },
          ],
          "vendorName": "IBM",
          "childrenData": [{
            "displayName": "Product Catalog",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/ROOT" },
          },
          {
            "displayName": "store-marketing",
            "child": { "$Ref": "livecontext/marketing/HeliosSiteId" },
          }]

          ,
          "storeId": "10851",
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/store/TestSiteId",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "TestSite",
          "id": "test:///catalog/store/10851",
          "topLevel": [
            { "$Ref": "livecontext/marketing/TestSiteId" },
            { "$Ref": "livecontext/category/TestSiteId/ROOT" },
          ],
          "vendorName": "test",
          "storeId": "10851",
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/marketing/HeliosSiteId",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Marketing spots",
          "id": "marketing-perfectchefesite",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "storeId": "10851",
          "marketingSpots": [
            { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot1" },
            { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot2" },
            { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot3" },
          ],
          "childrenData": [{
            "displayName": "Spot1",
            "child": { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot1" },
          },
          {
            "displayName": "Spot2",
            "child": { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot2" },
          },
          {
            "displayName": "Spot3",
            "child": { "$Ref": "livecontext/marketingspot/HeliosSiteId/spot3" },
          }],
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/ROOT",
        "method": "GET",
      },
      "response": {
        "body": {
          "id": "ibm:///catalog/category/ROOT",
          "externalId": "ROOT",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "storeId": "10851",
          "children": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Apparel" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Home%20Furnishings" },
          ],
          "childrenData": [{
            "displayName": "Grocery",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
          },
          {
            "displayName": "Home & Furnishing",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Home%20Furnishings" },
          },
          {
            "displayName": "Apparel",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Apparel" },
          }],
          "subCategories": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Apparel" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Home%20Furnishings" },
          ],
          "displayName": "Product Catalog",
          "name": "Product Catalog",
          "parent": null,
          "content": { "$Ref": "content/500" },
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/store/Media",
        "method": "GET",
      },
      "response": { "code": 404 },
    },

    {
      "request": {
        "uri": "livecontext/marketingspot/HeliosSiteId/spot1",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "spot1",
          "id": "ibm:///catalog/marketingspot/spot1",
          "description": "spot1",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "marketing": { "$Ref": "livecontext/marketing/HeliosSiteId" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/marketingspot/HeliosSiteId/spot2",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "spot2",
          "id": "ibm:///catalog/marketingspot/spot2",
          "description": "spot2",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "marketing": { "$Ref": "livecontext/marketing/HeliosSiteId" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/marketingspot/HeliosSiteId/spot3",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "spot3",
          "id": "ibm:///catalog/marketingspot/spot3",
          "description": "spot3",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "marketing": { "$Ref": "livecontext/marketing/HeliosSiteId" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Grocery",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Grocery",
          "id": "ibm:///catalog/category/Grocery",
          "children": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Vegetables" },
          ],
          "childrenData": [{
            "displayName": "Vegetables",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Vegetables" },
          }, {
            "displayName": "Fruit",
            "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
          }],
          "externalId": "Grocery",
          "displayName": "Grocery",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Vegetables" },
          ],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/ROOT" },
          "content": null,
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Vegetables",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Vegetables",
          "id": "ibm:///catalog/category/Vegetables",
          "children": [],
          "childrenData": [],
          "externalId": "Vegetables",
          "displayName": "Vegetables",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Apparel",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Apparel",
          "id": "ibm:///catalog/category/Apparel",
          "children": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Women" },
          ],
          "childrenData": [
            {
              "displayName": "Women",
              "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Women" },
            },
          ],
          "externalId": "Apparel",
          "displayName": "Apparel",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Women" },
          ],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/ROOT" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Women",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Women",
          "id": "ibm:///catalog/category/Women",
          "children": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          ],
          "childrenData": [
            {
              "displayName": "Dresses",
              "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
            },
          ],
          "externalId": "Women",
          "displayName": "Women",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          ],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Apparel" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Dresses",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Dresses",
          "id": "ibm:///catalog/category/Dresses",
          "children": [
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/AuroraWMDRS-1" },
          ],
          "childrenData": [
            {
              "displayName": "AuroraWMDRS-1",
              "child": { "$Ref": "livecontext/product/HeliosSiteId/catalog/AuroraWMDRS-1" },
            },
          ],
          "externalId": "Dresses",
          "displayName": "Dresses",
          "externalTechId": "10006",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Women" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/AuroraWMDRS-1",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Hermitage Ruched Bodice Cocktail Dress",
          "id": "ibm:///catalog/product/AuroraWMDRS-1",
          "externalId": "AuroraWMDRS-1",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg",
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/sku/HeliosSiteId/catalog/AuroraWMDRS-001",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": AbstractCatalogTest.HERMITAGE_RUCHED_BODICE_COCKTAIL_DRESS,
          "id": "ibm:///catalog/sku/AuroraWMDRS-001",
          "externalId": "AuroraWMDRS-001",
          "externalTechId": "10040",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg",
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/sku/HeliosSiteId/catalog/AuroraWMDRS-002",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Hermitage Ruched Bodice Cocktail Dress",
          "id": "ibm:///catalog/sku/AuroraWMDRS-002",
          "externalId": "AuroraWMDRS-002",
          "externalTechId": "10041",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg",
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Home%20Furnishings",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Home & Furnishing",
          "id": "ibm:///catalog/category/Home%20Furnishings",
          "children": [],
          "childrenData": [],
          "externalId": "Home & Furnishing",
          "displayName": "Home & Furnishing",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/ROOT" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Fruit",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Fruit",
          "id": "ibm:///catalog/category/Fruit",
          "children": [
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID },
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/GFR033_3302" },
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/GFR033_3303" },
          ],
          "childrenData": [
            {
              "displayName": "Oranges",
              "child": { "$Ref": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID },
            },
            {
              "displayName": "Blackberries",
              "child": { "$Ref": "livecontext/product/HeliosSiteId/catalog/GFR033_3302" },
            },
            {
              "displayName": "Mangoes",
              "child": { "$Ref": "livecontext/product/HeliosSiteId/catalog/GFR033_3303" },
            },
          ],
          "externalId": "Grocery Fruit",
          "displayName": "Grocery Fruit",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
          "content": { "$Ref": "content/700" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/category/HeliosSiteId/catalog/Link",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Link",
          "id": "ibm:///catalog/category/Link",
          "children": [
            { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
          ],
          "childrenData": [
            {
              "displayName": "Oranges",
              "child": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
              "isVirtual": true,
            },
          ],
          "externalId": "Link",
          "displayName": "Link",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "subCategories": [{ "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" }],
          "parent": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Grocery" },
          "content": { "$Ref": "content/706" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID,
        "method": "GET",
      },
      "response": {
        "body": {
          "name": AbstractCatalogTest.ORANGES_NAME,
          "shortDescription": AbstractCatalogTest.ORANGES_SHORT_DESC,
          "id": AbstractCatalogTest.ORANGES_ID,
          "externalId": AbstractCatalogTest.ORANGES_EXTERNAL_ID,
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
          "thumbnailUrl": AbstractCatalogTest.ORANGES_IMAGE_URI,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_SKU_EXTERNAL_ID,
        "method": "GET",
      },
      "response": {
        "body": {
          "name": AbstractCatalogTest.ORANGES_SKU_NAME,
          "shortDescription": AbstractCatalogTest.ORANGES_SKU_SHORT_DESC,
          "id": AbstractCatalogTest.ORANGES_SKU_ID,
          "externalId": AbstractCatalogTest.ORANGES_SKU_EXTERNAL_ID,
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
          "thumbnailUrl": AbstractCatalogTest.ORANGES_IMAGE_URI,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "02",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": AbstractCatalogTest.ORANGES_NAME,
          "id": AbstractCatalogTest.SKU_ID_PREFIX + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "02",
          "externalId": AbstractCatalogTest.ORANGES_EXTERNAL_ID + "02",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
          "thumbnailUrl": AbstractCatalogTest.ORANGES_IMAGE_URI,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "03",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": AbstractCatalogTest.ORANGES_NAME,
          "id": AbstractCatalogTest.SKU_ID_PREFIX + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "03",
          "externalId": AbstractCatalogTest.ORANGES_EXTERNAL_ID + "03",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Fruit" },
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.BABY_SHOES_EXTERNAL_ID,
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Borsati Orange Baby Shoes",
          "externalId": "BSH016_1605",
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Boys%20Shoes" },
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "externalTechId": "12595",
          "id": "ibm:///catalog/product/BSH016_1605",
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/xmp1",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "XMP 1",
          "id": "ibm:///catalog/product/xmp1",
          "externalId": "xmp1",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg",
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/xmp2",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "XMP 2",
          "id": "ibm:///catalog/product/xmp2",
          "externalId": "xmp2",
          "store": { "$Ref": "livecontext/store/HeliosSiteId" },
          "category": { "$Ref": "livecontext/category/HeliosSiteId/catalog/Dresses" },
          "thumbnailUrl": "http://shop-ref.ecommerce.coremedia.com/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/women/wcl000_dresses/200x310/wcl000_0028_a_red.jpg",
        },
      },
    },
    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + 404,
        "method": "GET",
      },
      "response": { "code": 404 },
    },

    {
      "request": {
        "uri": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + 503,
        "method": "GET",
      },
      "response": { "code": 503 },
    },

    {
      "request": {
        "uri": "livecontext/search/HeliosSiteId?query=*&searchType=Product&siteId=HeliosSiteId&limit=-1&includeSubfolders=true&includeSubtypes=true",
        "method": "GET",
      },
      "response": {
        "body": {
          "hits": [],
          "total": 0,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/search/HeliosSiteId?query=Oranges&searchType=Product&siteId=HeliosSiteId&filterQuery=&limit=-1",
        "method": "GET",
      },
      "response": {
        "body": {
          "hits": [
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID },
            { "$Ref": "livecontext/product/HeliosSiteId/catalog/BSH016_1605" },
          ],
          "total": 2,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/search/HeliosSiteId?category=10006&catalogAlias=catalog&query=AuroraWMDRS-1&searchType=ProductVariant&siteId=HeliosSiteId&filterQuery=&limit=-1",
        "method": "GET",
      },
      "response": {
        "body": {
          "hits": [
            { "$Ref": "livecontext/sku/HeliosSiteId/catalog/AuroraWMDRS-001" },
            { "$Ref": "livecontext/sku/HeliosSiteId/catalog/AuroraWMDRS-002" },
          ],
          "total": 2,
        },
      },
    },

    {
      "request": {
        "uri": "livecontext/search/HeliosSiteId?query=Oranges&searchType=ProductVariant&siteId=HeliosSiteId&filterQuery=&limit=-1",
        "method": "GET",
      },
      "response": {
        "body": {
          "hits": [
            { "$Ref": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_SKU_EXTERNAL_ID },
            { "$Ref": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "02" },
            { "$Ref": "livecontext/sku/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID + "03" },
          ],
          "total": 3,
        },
      },
    },

    {
      "request": {
        "uri": "content",
        "method": "GET",
      },
      "response": {
        "body": {
          "root": { "$Ref": "content/1" },
          "baseHomeFolder": { "$Ref": "content/5" },
          "referrersWithDescriptorUriTemplate": "content/{id:[0-9]+}/referrersWithDescriptor/{contentType:[^/]*}/{propertyName:[^/]*}",
          "referrersUriTemplate": "content/{id:[0-9]+}/referrers",
          "contentTypes": [
            {
              "name": "CMExternalPage",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "externalId",
                    "type": "String",
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "externalUriPath",
                    "type": "String",
                    "atomic": true,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMExternalPage",
              "parent": { "$Ref": "content/type/CMHasContexts" },
              "instancesBean": { "$Ref": "content/type/CMExternalPage/instances" },
              "abstract": false,
              "$Bean": "content/type/CMExternalPage",
            },
            {
              "name": "CMHasContexts",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMHasContexts" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "contexts",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMContext" },
                    "collection": true,
                    "atomic": false,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMHasContexts",
              "parent": { "$Ref": "content/type/CMLinkable" },
              "instancesBean": { "$Ref": "content/type/CMHasContexts/instances" },
              "abstract": true,
              "$Bean": "content/type/CMHasContexts",
            },
            {
              "name": "CMLinkable",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMLinkable" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "keywords",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 1024,
                    "encodedLength": 3072,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "viewtype",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMViewtype" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "localSettings",
                    "type": "STRUCT",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "linkedSettings",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMSettings" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "validFrom",
                    "type": "DATE",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "validTo",
                    "type": "DATE",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "segment",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 64,
                    "encodedLength": 192,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "title",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 512,
                    "encodedLength": 1536,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "subjectTaxonomy",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMTaxonomy" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "locationTaxonomy",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMLocTaxonomy" },
                    "collection": true,
                    "atomic": false,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMLinkable",
              "parent": { "$Ref": "content/type/CMLocalized" },
              "instancesBean": { "$Ref": "content/type/CMLinkable/instances" },
              "abstract": true,
              "$Bean": "content/type/CMLinkable",
            },
            {
              "name": "CMLocalized",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "locale",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 64,
                    "encodedLength": 192,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMLocalized" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "masterVersion",
                    "type": "INTEGER",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "collection": false,
                    "atomic": true,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMLocalized",
              "parent": { "$Ref": "content/type/CMObject" },
              "instancesBean": { "$Ref": "content/type/CMLocalized/instances" },
              "abstract": true,
              "$Bean": "content/type/CMLocalized",
            },
            {
              "name": "CMMarketingSpot",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMMarketingSpot" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "externalId",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 256,
                    "encodedLength": 768,
                    "collection": false,
                    "atomic": true,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMMarketingSpot",
              "parent": { "$Ref": "content/type/CMTeasable" },
              "instancesBean": { "$Ref": "content/type/CMMarketingSpot/instances" },
              "abstract": false,
              "$Bean": "content/type/CMMarketingSpot",
            },
            {
              "name": "CMObject",
              "description": null,
              "directDescriptors": [],
              "id": "coremedia:///cap/contenttype/CMObject",
              "parent": { "$Ref": "content/type/Document_" },
              "instancesBean": { "$Ref": "content/type/CMObject/instances" },
              "abstract": true,
              "$Bean": "content/type/CMObject",
            },
            {
              "name": "CMProductTeaser",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMProductTeaser" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "externalId",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 64,
                    "encodedLength": 192,
                    "collection": false,
                    "atomic": true,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMProductTeaser",
              "parent": { "$Ref": "content/type/CMTeasable" },
              "instancesBean": { "$Ref": "content/type/CMProductTeaser/instances" },
              "abstract": false,
              "$Bean": "content/type/CMProductTeaser",
            },
            {
              "name": "CMPicture",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMPicture" },
                    "collection": true,
                    "atomic": false,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMPicture",
              "parent": { "$Ref": "content/type/CMTeasable" },
              "instancesBean": { "$Ref": "content/type/CMPicture/instances" },
              "abstract": false,
              "$Bean": "content/type/CMPicture",
            },
            {
              "abstract": false,
              "id": "coremedia:///cap/contenttype/CMSite",
              "directDescriptors": [{
                "$CapPropertyDescriptor": {
                  "name": "root",
                  "type": "LINK",
                  "linkType": { "$Ref": "content/type/CMNavigation" },
                  "atomic": false,
                  "collection": true,
                  "minCardinality": 0,
                  "maxCardinality": 1,
                },
              }],
              "description": "???CMSite",
              "$Bean": "content/type/CMSite",
              "name": "CMSite",
              "instancesBean": { "$Ref": "content/type/CMSite/instances" },
              "parent": { "$Ref": "content/type/CMLocalized" },
            },
            {
              "name": "CMTeasable",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMTeasable" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "teaserTitle",
                    "type": "STRING",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "length": 512,
                    "encodedLength": 1536,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "teaserText",
                    "type": "MARKUP",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "grammar": "coremedia-richtext-1.0",
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "detailText",
                    "type": "MARKUP",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "grammar": "coremedia-richtext-1.0",
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "pictures",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMPicture" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "notSearchable",
                    "type": "INTEGER",
                    "minCardinality": 1,
                    "maxCardinality": 1,
                    "collection": false,
                    "atomic": true,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "related",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 2147483647,
                    "linkType": { "$Ref": "content/type/CMTeasable" },
                    "collection": true,
                    "atomic": false,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMTeasable",
              "parent": { "$Ref": "content/type/CMHasContexts" },
              "instancesBean": { "$Ref": "content/type/CMTeasable/instances" },
              "abstract": true,
              "$Bean": "content/type/CMTeasable",
            },
            {
              "name": "CMTeaser",
              "description": null,
              "directDescriptors": [
                {
                  "$CapPropertyDescriptor": {
                    "name": "master",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMTeaser" },
                    "collection": true,
                    "atomic": false,
                  },
                },
                {
                  "$CapPropertyDescriptor": {
                    "name": "target",
                    "type": "LINK",
                    "minCardinality": 0,
                    "maxCardinality": 1,
                    "linkType": { "$Ref": "content/type/CMLinkable" },
                    "collection": true,
                    "atomic": false,
                  },
                },
              ],
              "id": "coremedia:///cap/contenttype/CMTeaser",
              "parent": { "$Ref": "content/type/CMTeasable" },
              "instancesBean": { "$Ref": "content/type/CMTeaser/instances" },
              "abstract": false,
              "$Bean": "content/type/CMTeaser",
            },
            {
              "name": "Content_",
              "description": "The root content type",
              "directDescriptors": [],
              "id": "coremedia:///cap/contenttype/Content_",
              "parent": null,
              "instancesBean": { "$Ref": "content/type/Content_/instances" },
              "abstract": true,
              "$Bean": "content/type/Content_",
            },
            {
              "name": "Document_",
              "description": "The document content type",
              "directDescriptors": [],
              "id": "coremedia:///cap/contenttype/Document_",
              "parent": { "$Ref": "content/type/Content_" },
              "instancesBean": { "$Ref": "content/type/Document_/instances" },
              "abstract": true,
              "$Bean": "content/type/Document_",
            },
            {
              "name": "Folder_",
              "description": "The folder content type",
              "directDescriptors": [],
              "id": "coremedia:///cap/contenttype/Folder_",
              "parent": { "$Ref": "content/type/Content_" },
              "instancesBean": { "$Ref": "content/type/Folder_/instances" },
              "abstract": false,
              "$Bean": "content/type/Folder_",
            },
          ],
          "contentContentType": { "$Ref": "content/type/Content_" },
          "folderContentType": { "$Ref": "content/type/Folder_" },
          "documentContentType": { "$Ref": "content/type/Document_" },
          "bulkRightsUri": "content/bulk/rights",
          "bulkCopyUri": "content/bulk/copy",
          "bulkMoveUri": "content/bulk/move",
          "bulkCheckInUri": "content/bulk/checkIn",
          "bulkRevertUri": "content/bulk/revert",
          "bulkApproveUri": "content/bulk/approve",
          "bulkDisapproveUri": "content/bulk/disapprove",
          "bulkPublishUri": "content/bulk/publish",
          "bulkApprovePublishUri": "content/bulk/approvePublish",
          "bulkWithdrawUri": "content/bulk/withdraw",
          "bulkDeleteUri": "content/bulk/delete",
          "bulkUndeleteUri": "content/bulk/undelete",
          "queryUri": "content/list",
          "searchUri": "content/search",
          "searchSuggestionsUri": "content/suggestions",
          "previewControllerUriPattern": "http://localhost:40081/blueprint/servlet/preview?id={0}",
          "previewUrlWhitelist": [],
          "timeZones": [
            "Europe/Berlin",
            "Europe/London",
            "America/New_York",
            "America/Los_Angeles",
          ],
          "defaultTimeZone": "Europe/Berlin",
          "availableLocalesContentPath": "/Settings/Options/Settings/LocaleSettings",
          "availableLocalesPropertyPath": "settings.availableLocales",
          "useStrictWorkflow": false,
        },
      },
    },
    {
      "request": {
        "uri": "content/1",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "root",
          "id": "coremedia:///cap/content/1",
          "properties": {},
          "type": { "$Ref": "content/type/Folder_" },
        },
      },
    },
    {
      "request": {
        "uri": "content/5",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Home",
          "id": "coremedia:///cap/content/5",
          "properties": {},
          "type": { "$Ref": "content/type/Folder_" },
        },
      },
    },
    {
      "request": {
        "uri": "content/100",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Product Teaser",
          "id": "coremedia:///cap/content/100",
          "properties": {
            "externalId": AbstractCatalogTest.ORANGES_ID,
            "teaserText": null,
          },
          "type": { "$Ref": "content/type/CMProductTeaser" },
        },
      },
    },
    {
      "request": {
        "uri": "content/100/properties/teaserText",
        "method": "GET",
      },
      "response": {
        "contentType": "text/xml",
        "headers": { "content-encoding": "UTF-8" },
        "body": null,
      },
    },
    {
      "request": {
        "uri": "content/100/referrersWithDescriptor/CMSite/root",
        "method": "GET",
      },
      "response": { "body": [] },
    },
    {
      "request": {
        "uri": "content/type/CMProductTeaser",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "CMProductTeaser",
          "description": null,
          "directDescriptors": [
            {
              "$CapPropertyDescriptor": {
                "name": "master",
                "type": "LINK",
                "minCardinality": 0,
                "maxCardinality": 1,
                "linkType": { "$Ref": "content/type/CMProductTeaser" },
                "collection": true,
                "atomic": false,
              },
            },
            {
              "$CapPropertyDescriptor": {
                "name": "externalId",
                "type": "STRING",
                "minCardinality": 1,
                "maxCardinality": 1,
                "length": 64,
                "encodedLength": 192,
                "collection": false,
                "atomic": true,
              },
            },
          ],
          "id": "coremedia:///cap/contenttype/CMProductTeaser",
          "parent": { "$Ref": "content/type/CMTeasable" },
          "instancesBean": { "$Ref": "content/type/CMProductTeaser/instances" },
          "abstract": false,
        },
      },
    },
    {
      "request": {
        "uri": "content/100",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.ORANGES_ID } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/100",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.ORANGES_ID + "503" } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/100",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.ORANGES_ID + "404" } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/100",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.ORANGES_SKU_ID } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/100",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": null } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/101",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "Marketing Spot",
          "id": "coremedia:///cap/content/101",
          "properties": { "externalId": "" },
        },
      },
    },

    {
      "request": {
        "uri": "content/101",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.MARKETING_SPOT_ID } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/101",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.MARKETING_SPOT_ID + "503" } },
      },
      "response": { "code": 200 },
    },

    {
      "request": {
        "uri": "content/101",
        "method": "PUT",
        "contentType": "application/json",
        "body": { "properties": { "externalId": AbstractCatalogTest.MARKETING_SPOT_ID + "404" } },
      },
      "response": { "code": 200 },
    },
    {
      "request": {
        "uri": "content/200",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "CM Picture",
          "id": "coremedia:///cap/content/200",
          "properties": { "localSettings": { "$Ref": "content/200/structs/localSettings" } },
          "type": { "$Ref": "content/type/CMPicture" },
        },
      },
    },
    {
      "request": {
        "uri": "content/200/structs/localSettings",
        "method": "GET",
      },
      "response": {
        "contentType": "application/json",
        "body": {
          $Struct: [
            $StructPropertyDescriptor({ name: "commerce" }),
          ],
          "commerce": {
            $Struct: [
              $BooleanPropertyDescriptor({ name: "inherit" }),
              $StringListPropertyDescriptor({ name: "references" }),
              $StringListPropertyDescriptor({ name: "originReferences" }),
            ],
            "inherit": false,
            "references": [AbstractCatalogTest.ORANGES_ID, AbstractCatalogTest.ORANGES_SKU_ID],
            "originReferences": [AbstractCatalogTest.PRODUCT1_FROM_XMP_ID, AbstractCatalogTest.PRODUCT2_FROM_XMP_ID],
          },
        },
      },
    },
    {
      "request": {
        "uri": "content/200/structs/localSettings",
        "method": "PUT",
        "contentType": "application/json",
      },
      "response": { "code": 200 },
    },
    {
      "request": {
        "uri": "content/202",
        "method": "GET",
      },
      "response": {
        "body": {
          "name": "CM Picture",
          "id": "coremedia:///cap/content/202",
          "properties": { "localSettings": { "$Ref": "content/202/structs/localSettings" } },
          "type": { "$Ref": "content/type/CMPicture" },
        },
      },
    },
    {
      "request": {
        "uri": "content/202/structs/localSettings",
        "method": "GET",
      },
      "response": {
        "contentType": "application/json",
        "body": {
          $Struct: [
            $StructPropertyDescriptor({ name: "commerce" }),
          ],
          "commerce":
            {
              $Struct: [
                $BooleanPropertyDescriptor({ name: "inherit" }),
                $StringListPropertyDescriptor({ name: "references" }),
                $StringListPropertyDescriptor({ name: "originReferences" }),
              ],
              "inherit": true,
              "references": [AbstractCatalogTest.PRODUCT1_FROM_XMP_ID, AbstractCatalogTest.PRODUCT2_FROM_XMP_ID],
              "originReferences": [AbstractCatalogTest.PRODUCT1_FROM_XMP_ID, AbstractCatalogTest.PRODUCT2_FROM_XMP_ID],
            },
        },
      },
    },
    {
      "request": {
        "uri": "content/202/structs/localSettings",
        "method": "PUT",
        "contentType": "application/json",
      },
      "response": { "code": 200 },
    },
    makeDocumentCall(ContentUri(300), {
      name: "Persona",
      parent: $ContentRef(1),
      type: $ContentTypeRef("CMUserProfile"),
      properties: {
        "externalId": "",
        "teaserText": null,
      },
    }),
    makeDocumentCall(ContentUri(400), {
      name: "Site Root Document",
      parent: $ContentRef(1),
      type: $ContentTypeRef(ContentTypeNames.DOCUMENT),
      properties: {
        "externalId": "",
        "teaserText": null,
      },
    }),
    makeDocumentCall(ContentUri(500), {
      name: "Root Category Document",
      parent: $ContentRef(1),
      type: $ContentTypeRef(ContentTypeNames.DOCUMENT),
      properties: { "externalId": "ibm:///catalog/category/ROOT" },
    }),
    makeDocumentCall(ContentUri(600), {
      name: "Grocery Augmented Category",
      parent: $ContentRef(1),
      type: $ContentTypeRef(ContentTypeNames.DOCUMENT),
      properties: { "externalId": "ibm:///catalog/category/Grocery" },
    }),
    makeDocumentCall(ContentUri(700), {
      name: "Fruit Augmented Category",
      parent: $ContentRef(1),
      type: $ContentTypeRef(ContentTypeNames.DOCUMENT),
      properties: { "externalId": "ibm:///catalog/category/Fruit" },
    }),
    makeContentRightsForUserCall(ContentRightsForUserUri(1, 1), [
      {
        type: $ContentTypeRef("CMMarketingSpot"),
        rights: "RMDAPS",
      },
      {
        type: $ContentTypeRef("CMProductTeaser"),
        rights: "RMDAPS",
      },
      {
        type: $ContentTypeRef("CMPicture"),
        rights: "RMDAPS",
      },
      {
        type: $ContentTypeRef("CMTeaser"),
        rights: "R",
      },
    ]),
  ];
}

export default AbstractCatalogTest;
