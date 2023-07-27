import AbstractCatalogTest from "@coremedia-blueprint/studio-client.main.ec-studio-test-helper/AbstractCatalogTest";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import Marketing from "../../src/model/Marketing";

class MarketingTest extends AbstractCatalogTest {

  #marketing: Marketing = null;

  override setUp(): void {
    super.setUp();
    this.#marketing = beanFactory._.getRemoteBeanOfType("livecontext/marketing/HeliosSiteId", Marketing);
  }

  async testMarketing(): Promise<void> {
    await this.#marketing.load();
    Assert.assertEquals(3, this.#marketing.getMarketingSpots().length);
  }
}

export default MarketingTest;
