import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import Store from "../../src/model/Store";

class StoreTest extends AbstractCatalogTest {

  #store: Store = null;

  override setUp(): void {
    super.setUp();
    this.#store = beanFactory._.getRemoteBeanOfType("livecontext/store/HeliosSiteId", Store);
  }

  async testStore(): Promise<void> {
    await this.#store.load();
    Assert.assertEquals("PerfectChefESite", this.#store.getName());
    Assert.assertEquals(2, this.#store.getTopLevel().length);
    Assert.assertEquals("10851", this.#store.getStoreId());
  }
}

export default StoreTest;
