package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialPlugin;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CMALXBaseListServiceTest {

  private ElasticSocialPlugin elasticSocialPlugin;

  private static final String TYPE_NAME = "CMALXBaseList";
  private ContentRepository contentRepository;
  private CMALXBaseListService cmalxBaseListService;
  private ContentType contentType;

  @BeforeEach
  public void setup() {
    elasticSocialPlugin = mock(ElasticSocialPlugin.class);
    contentType = mock(ContentType.class);
    contentRepository = mock(ContentRepository.class);
    when(contentRepository.getContentType(TYPE_NAME)).thenReturn(contentType);

    cmalxBaseListService = new CMALXBaseListService(contentRepository, elasticSocialPlugin);
  }

  @Test
  void testGetCMALXBaseLists() {
    cmalxBaseListService.initialize();
    verify(contentRepository).getContentType(TYPE_NAME);

    final Content c1 = mock(Content.class);
    when(c1.isInProduction()).thenReturn(true);
    final Content c2 = mock(Content.class);
    when(c2.isInProduction()).thenReturn(true);
    final Content c3 = mock(Content.class);
    when(c3.isInProduction()).thenReturn(false);

    when(contentType.getInstances()).thenReturn(
            new HashSet<>(Arrays.asList(c1, c2))
    );

    ElasticSocialConfiguration config = mock(ElasticSocialConfiguration.class);
    when(elasticSocialPlugin.getElasticSocialConfiguration(c1, null)).thenReturn(config);
    when(config.getTenant()).thenReturn("test");
    ElasticSocialConfiguration config2 = mock(ElasticSocialConfiguration.class);
    when(elasticSocialPlugin.getElasticSocialConfiguration(c2, null)).thenReturn(config2);
    when(config2.getTenant()).thenReturn("unknown");

    assertThat(cmalxBaseListService.getCMALXBaseLists(null, "test")).containsExactly(c1);
    assertThat(cmalxBaseListService.getCMALXBaseLists(null, "doesNotExist")).isEmpty();
  }

}
