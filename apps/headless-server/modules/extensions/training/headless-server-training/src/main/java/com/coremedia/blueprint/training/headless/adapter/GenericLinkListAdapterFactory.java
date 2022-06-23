package com.coremedia.blueprint.training.headless.adapter;

import com.coremedia.cap.content.Content;

public class GenericLinkListAdapterFactory {

  public GenericLinkListAdapter to(Content root, String propertyName) {
    return new GenericLinkListAdapter(root, propertyName);
  }
}
