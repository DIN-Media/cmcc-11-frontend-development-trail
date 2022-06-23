package com.coremedia.blueprint.training.headless;

import com.coremedia.blueprint.training.headless.adapter.GenericLinkListAdapterFactory;
import com.coremedia.blueprint.training.headless.model.TrainingRoot;
import com.coremedia.cap.content.ContentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Main configuration class of the training headless server extension.
 */
@Configuration
public class TrainingConfig {

  // Add your customizations...

  @Bean
  public GenericLinkListAdapterFactory genericLinkListAdapter() {
    return new GenericLinkListAdapterFactory();
  }

  @Bean
  @Qualifier("queryRoot")
  public TrainingRoot training(ContentRepository contentRepository) {
    return new TrainingRoot(contentRepository);
  }

}
