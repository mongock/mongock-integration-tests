package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class RuntimeTestUtil {

  public static final Integer MONGO_PORT = 27017;
  public static final String DEFAULT_DATABASE_NAME = "mongocktest";


  public static GenericContainer  startMongoContainer(String mongoVersion) {
    GenericContainer container = new GenericContainer(mongoVersion).withExposedPorts(MONGO_PORT);
    container.start();
    return container;
  }

  public static ConfigurableApplicationContext startSpringAppWithMongoVersionAndDefaultPackage(String mongoVersion) {
    return startSpringAppWithMongoVersionAndPackage(
            mongoVersion,
            "com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client"
    );
  }
  public static ConfigurableApplicationContext startSpringAppWithMongoVersionAndNoPackage(String mongoVersion) {
    return startSpringAppWithMongoVersionAndPackage(mongoVersion, "");
  }

  public static ConfigurableApplicationContext startSpringAppWithMongoVersionAndPackage(String mongoVersion, String packagePath) {
    GenericContainer container = startMongoContainer(mongoVersion);
    return Mongock4Spring5SpringData3App.getSpringAppBuilder()
            .properties(
                    "server.port=0",// random port
                    !StringUtils.isEmpty(packagePath) ? "spring.mongock.changeLogsScanPackage=" + packagePath : "",
                    "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", container.getContainerIpAddress(), container.getFirstMappedPort()),
                    "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
            ).run();
  }


}
