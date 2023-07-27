package com.coremedia.blueprint.analytics.elastic;

import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@ImportResource(value = "classpath:META-INF/coremedia/es-alx-retrieval.xml",
        reader = ResourceAwareXmlBeanDefinitionReader.class)
@PropertySource("classpath:META-INF/coremedia/es-alx-retrieval.properties")
public class EsAlxRetrievalAutoConfiguration {
}
