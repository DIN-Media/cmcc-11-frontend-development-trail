package com.coremedia.blueprint.caefeeder.corporate;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration(proxyBeanMethods = false)
@PropertySource(value = "classpath:/META-INF/coremedia/corporate-caefeeder.properties")
class CorporateCaeFeederPropertiesConfiguration {
}
