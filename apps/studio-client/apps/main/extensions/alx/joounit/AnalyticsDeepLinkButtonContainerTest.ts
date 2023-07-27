import AbstractRemoteTest from "@coremedia/studio-client.client-core-test-helper/AbstractRemoteTest";
import { waitUntil } from "@coremedia/studio-client.client-core-test-helper/async";
import { MockCall } from "@coremedia/studio-client.client-core-test-helper/MockFetch";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import EditorContextImpl from "@coremedia/studio-client.main.editor-components/sdk/EditorContextImpl";
import Button from "@jangaroo/ext-ts/button/Button";
import Item from "@jangaroo/ext-ts/menu/Item";
import Assert from "@jangaroo/joounit/flexunit/framework/Assert";
import { asConfig, bind, cast, is } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import int from "@jangaroo/runtime/int";
import { AnyFunction } from "@jangaroo/runtime/types";
import AnalyticsDeepLinkButtonContainer from "../src/AnalyticsDeepLinkButtonContainer";
import OpenAnalyticsDeepLinkUrlButton from "../src/OpenAnalyticsDeepLinkUrlButton";
import ContentProvidingTestContainerBase from "./ContentProvidingTestContainerBase";
import MultipleAnalyticsUrlButtonsTestView from "./MultipleAnalyticsUrlButtonsTestView";
import SingleAnalyticsUrlButtonTestView from "./SingleAnalyticsUrlButtonTestView";

class AnalyticsDeepLinkButtonContainerTest extends AbstractRemoteTest {

  static readonly #URL_1: string = "http://host.domain.net/drilldown";

  static readonly #URL_2: string = "http://my.url";

  static readonly #MY_ID: number = 666;

  static readonly #content: Record<string, any> = {
    getNumericId: (): int =>
      AnalyticsDeepLinkButtonContainerTest.#MY_ID
    ,
    type: {
      getNumericId: (): int =>
        AnalyticsDeepLinkButtonContainerTest.#MY_ID
      ,
      name: "typeWithPreview",
    },
  };

  #window_open: AnyFunction = null;

  #args: any = null;

  override setUp(): void {
    super.setUp();
    this.#window_open = bind(window, window.open);
    window.open = ((... myArgs): Window => {
      this.#args = myArgs;
      return window;
    });

    EditorContextImpl.initEditorContext();
  }

  override tearDown(): void {
    super.tearDown();
    window.open = this.#window_open;
    this.#args = null;
  }

  // noinspection JSUnusedGlobalSymbols
  async testAnalyticsDeepLinkButtonContainerSingleView(): Promise<void> {
    const testView = new SingleAnalyticsUrlButtonTestView(
      Config(SingleAnalyticsUrlButtonTestView, { contentValueExpression: ValueExpressionFactory.createFromValue() }));

    const contentContainer = cast(ContentProvidingTestContainerBase, testView.getComponent("contentContainer"));
    const container = cast(AnalyticsDeepLinkButtonContainer, contentContainer.getComponent("alxDeepLinkButtonContainer"));
    const item = cast(Button, container.items.get(0));

    Assert.assertTrue(is(item, OpenAnalyticsDeepLinkUrlButton));
    Assert.assertTrue(item.disabled);

    contentContainer.setContent(AnalyticsDeepLinkButtonContainerTest.#content);

    // button still disabled:
    await waitUntil((): boolean => !item.disabled);
    // invoke handler on enabled buttons:
    cast(Function, item.handler)();
    Assert.assertNotNull(this.#args);
    Assert.assertEquals(AnalyticsDeepLinkButtonContainerTest.#URL_1, this.#args[0]);
  }

  // noinspection JSUnusedGlobalSymbols
  async testAnalyticsDeepLinkButtonContainerMultiView(): Promise<void> {
    const testView = new MultipleAnalyticsUrlButtonsTestView(
      Config(MultipleAnalyticsUrlButtonsTestView, { contentValueExpression: ValueExpressionFactory.createFromValue() }));
    const contentContainer = cast(ContentProvidingTestContainerBase, testView.getComponent("contentContainer"));
    const container = cast(AnalyticsDeepLinkButtonContainer, contentContainer.getComponent("alxDeepLinkButtonContainer"));

    // the menu button should be disabled initially
    const item = cast(Button, container.items.get(3));

    Assert.assertTrue(is(item, IconButton));
    Assert.assertNotNull(asConfig(item).menu);
    Assert.assertTrue("menu button should be initially disabled", item.disabled);

    const items = item.menu.items.getRange();
    Assert.assertEquals(3, items.length);
    items.forEach((item: Item, index: int): void =>
      Assert.assertTrue("item " + index + " should be disabled", item.disabled),
    );

    contentContainer.setContent(AnalyticsDeepLinkButtonContainerTest.#content);

    // button still disabled:
    await waitUntil((): boolean => !item.disabled);
    // state of menu items:
    Assert.assertTrue("first menu item should be disabled", items[0].disabled);
    Assert.assertFalse("second menu item should be enabled", items[1].disabled);
    Assert.assertFalse("third menu item should be enabled", items[2].disabled);

    // invoke handler on enabled menu items:
    items[1]["handler"]();
    Assert.assertNotNull(this.#args);
    Assert.assertEquals(AnalyticsDeepLinkButtonContainerTest.#URL_1, this.#args[0]);
    this.#args = null;
    items[2]["handler"]();
    Assert.assertNotNull(this.#args);
    Assert.assertEquals(AnalyticsDeepLinkButtonContainerTest.#URL_2, this.#args[0]);
  }

  protected override getMockCalls(): MockCall[] {
    return [
      {
        "request": { "uri": "alxservice/" + AnalyticsDeepLinkButtonContainerTest.#MY_ID },
        "response": {
          "body": {
            "testService1": "invalidUrl",
            "testService2": AnalyticsDeepLinkButtonContainerTest.#URL_1,
            "testService3": AnalyticsDeepLinkButtonContainerTest.#URL_2,
          },
        },
      },
    ];
  }

}

export default AnalyticsDeepLinkButtonContainerTest;
