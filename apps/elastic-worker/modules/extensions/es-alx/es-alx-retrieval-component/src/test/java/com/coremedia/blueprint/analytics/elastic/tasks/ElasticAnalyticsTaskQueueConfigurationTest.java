package com.coremedia.blueprint.analytics.elastic.tasks;


import com.coremedia.elastic.core.api.tasks.configuration.TaskQueueConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.coremedia.blueprint.analytics.elastic.tasks.ElasticAnalyticsTaskQueueConfiguration.FETCH_INTERVAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticAnalyticsTaskQueueConfigurationTest {
  @InjectMocks
  private ElasticAnalyticsTaskQueueConfiguration config = new ElasticAnalyticsTaskQueueConfiguration();

  @Mock
  private TaskQueueConfigurationBuilder builder;

  @Test
  void getTaskQueues() {
    //noinspection unchecked
    when(builder.configureTask(anyString(), any(Class.class), anyLong())).thenReturn(builder);

    config.getTaskQueues();

    verify(builder).configureTask("elasticAnalyticsTaskQueue", FetchReportsTask.class, FETCH_INTERVAL);
    verify(builder).configureTask("elasticAnalyticsTaskQueue", FetchPageViewHistoryTask.class, FETCH_INTERVAL);
    verify(builder).configureTask("elasticAnalyticsTaskQueue", FetchPublicationsHistoryTask.class, FETCH_INTERVAL);
    verify(builder).build();
  }
}

