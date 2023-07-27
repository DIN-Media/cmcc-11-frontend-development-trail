package com.coremedia.blueprint.boot.elasticworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        SolrAutoConfiguration.class,
})
@EnableScheduling
public class ElasticWorkerApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(ElasticWorkerApp.class, args);
  }
}
