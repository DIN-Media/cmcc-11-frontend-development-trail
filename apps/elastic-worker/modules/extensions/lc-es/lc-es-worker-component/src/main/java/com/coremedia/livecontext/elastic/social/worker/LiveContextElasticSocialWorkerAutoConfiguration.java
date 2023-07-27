package com.coremedia.livecontext.elastic.social.worker;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/lc-es-worker.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LiveContextElasticSocialWorkerAutoConfiguration {
}
