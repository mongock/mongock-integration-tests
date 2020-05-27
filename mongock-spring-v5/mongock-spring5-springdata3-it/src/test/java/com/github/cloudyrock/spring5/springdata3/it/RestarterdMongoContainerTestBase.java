package com.github.cloudyrock.spring5.springdata3.it;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class RestarterdMongoContainerTestBase {

  protected static final Integer MONGO_PORT = 27017;
  protected static final String DEFAULT_DATABASE_NAME = "mongocktest";




  protected GenericContainer  startMongoContainer(String mongoVersion) {
    GenericContainer container = new GenericContainer(mongoVersion).withExposedPorts(MONGO_PORT);
    container.start();
    return container;
  }


}
