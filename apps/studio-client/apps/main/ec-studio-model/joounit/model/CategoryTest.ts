import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import Category from "../../src/model/Category";

class CategoryTest extends AbstractCatalogTest {

  #rootCategory: Category = null;

  #topCategory: Category = null;

  #leafCategory: Category = null;

  override setUp(): void {
    super.setUp();
    this.#rootCategory = beanFactory._.getRemoteBeanOfType("livecontext/category/HeliosSiteId/catalog/ROOT", Category);
    this.#topCategory = beanFactory._.getRemoteBeanOfType("livecontext/category/HeliosSiteId/catalog/Grocery", Category);
    this.#leafCategory = beanFactory._.getRemoteBeanOfType("livecontext/category/HeliosSiteId/catalog/Fruit", Category);
  }

  // noinspection JSUnusedGlobalSymbols
  async testTopCategory(): Promise<void> {
    await this.#topCategory.load();
    Assert.assertEquals("Grocery", this.#topCategory.getName());
    Assert.assertTrue(this.#topCategory.getId().indexOf("ibm:///catalog/category/Grocery") == 0);
    Assert.assertEquals(2, this.#topCategory.getChildren().length);
    Assert.assertEquals(this.#rootCategory, this.#topCategory.getParent());
  }

  // noinspection JSUnusedGlobalSymbols
  async testLeafCategory(): Promise<void> {
    await this.#leafCategory.load();
    Assert.assertEquals("Fruit", this.#leafCategory.getName());
    Assert.assertTrue(this.#leafCategory.getId().indexOf("ibm:///catalog/category/Fruit") == 0);
    Assert.assertEquals(3, this.#leafCategory.getChildren().length);
    Assert.assertNotNull(this.#leafCategory.getParent());
  }
}

export default CategoryTest;
