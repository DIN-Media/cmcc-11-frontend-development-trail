package com.coremedia.blueprint.training.headless.model;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;

import java.util.Set;

public class TrainingRoot {

  public static final String CMVIDEOTUTORIAL = "CMVideoTutorial";
  private ContentRepository contentRepository;

  public TrainingRoot(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public Set<Content> getTutorials() {
    ContentType type =  contentRepository.getContentType(CMVIDEOTUTORIAL);
    return type.getInstances();
  }

  public Content getTutorial(String id) {
    Content content = contentRepository.getContent(id);
    if (content!=null && content.getType().isSubtypeOf(CMVIDEOTUTORIAL)) {
      return content;
    }
    return null;
  }
}
