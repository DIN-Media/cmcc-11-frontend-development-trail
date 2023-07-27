package com.coremedia.blueprint.studio.blueprint.component.boot;

import com.coremedia.blueprint.segments.SegmentsConfiguration;
import com.coremedia.collaboration.notifications.WorkflowNotificationsConfiguration;
import com.coremedia.collaboration.userchanges.UserChangesConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;

@AutoConfiguration
@ConditionalOnProperty(name = "elastic.core.persistence", havingValue = "memory")
@ImportResource(value = {
        "classpath:/framework/spring/mediatransform.xml",
        "classpath:/com/coremedia/blueprint/common/multisite/translation-config.xml",
        "classpath:/META-INF/coremedia/studio-in-memory-cap-list.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@Import({
        SegmentsConfiguration.class,
        WorkflowNotificationsConfiguration.class,
        UserChangesConfiguration.class})
public class StudioInMemoryAutoConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(StudioInMemoryAutoConfiguration.class);

  @PostConstruct
  void initialize() {
    LOG.info("Initializing in-memory configuration for studio-webapp.");
  }
}
