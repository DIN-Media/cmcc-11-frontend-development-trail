package com.coremedia.blueprint.training.headless.adapter;

import com.coremedia.cap.content.Content;

import java.util.List;

public class GenericLinkListAdapter {

  private Content root;
  private String propertyName;

  protected GenericLinkListAdapter(Content root, String propertyName) {
    this.root = root;
    this.propertyName = propertyName;
  }

  public List<Content> list() {
    return root.getLinks(propertyName);
  }

  public Content first() {
    return root.getLink(propertyName);
  }
}
