import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import Category from "../../src/model/Category";
import Product from "../../src/model/Product";

class ProductTest extends AbstractCatalogTest {

  #product: Product = null;

  #leafCategory: Category = null;

  override setUp(): void {
    super.setUp();
    this.#product = beanFactory._.getRemoteBeanOfType("livecontext/product/HeliosSiteId/catalog/" + AbstractCatalogTest.ORANGES_EXTERNAL_ID, Product);
    this.#leafCategory = beanFactory._.getRemoteBeanOfType("livecontext/category/HeliosSiteId/catalog/Fruit", Category);
  }

  // noinspection JSUnusedGlobalSymbols
  async testProduct(): Promise<void> {
    await this.#product.load();
    Assert.assertEquals(AbstractCatalogTest.ORANGES_NAME, this.#product.getName());
    Assert.assertEquals(AbstractCatalogTest.ORANGES_EXTERNAL_ID, this.#product.getExternalId());
    Assert.assertTrue(this.#product.getId().indexOf(AbstractCatalogTest.ORANGES_ID) == 0);
    Assert.assertEquals(this.#leafCategory, this.#product.getCategory());
  }
}

export default ProductTest;
