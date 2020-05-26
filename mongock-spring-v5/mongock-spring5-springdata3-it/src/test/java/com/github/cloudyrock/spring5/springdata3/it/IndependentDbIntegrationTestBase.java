package com.github.cloudyrock.spring5.springdata3.it;

import org.junit.Rule;
import org.testcontainers.containers.GenericContainer;

public abstract class IndependentDbIntegrationTestBase {

  protected static final String MONGO_CONTAINER = "mongo:4.2.0";
  protected static final Integer MONGO_PORT = 27017;
  protected static final String DEFAULT_DATABASE_NAME = "mongocktest";

  @Rule
  public GenericContainer mongo = new GenericContainer(MONGO_CONTAINER).withExposedPorts(MONGO_PORT);


}
