package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;

public abstract class RuntimeTestUtil {

  public static final Integer MONGO_PORT = 27017;
  public static final String DEFAULT_DATABASE_NAME = "mongocktest";


  public static GenericContainer startMongoDbContainer(String mongoVersion) {
    GenericContainer container = new GenericContainer(mongoVersion).withExposedPorts(MONGO_PORT);
    container.start();
    return container;
  }

  public static ConfigurableApplicationContext startSpringAppWithMongoDbVersionAndDefaultPackage(String mongoVersion) {
    return startSpringAppWithMongoDbVersionAndPackage(
            mongoVersion,
            "com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client"
    );
  }
  public static ConfigurableApplicationContext startSpringAppWithMongoDbVersionAndNoPackage(String mongoVersion) {
    return startSpringAppWithMongoDbVersionAndPackage(mongoVersion, "");
  }

  public static ConfigurableApplicationContext startSpringAppWithMongoDbVersionAndPackage(String mongoDbVersion, String packagePath) {
    GenericContainer container = startMongoDbContainer(mongoDbVersion);
    return Mongock4Spring5SpringData3App.getSpringAppBuilder()
            .properties(
                    "server.port=0",// random port
                    !StringUtils.isEmpty(packagePath) ? "spring.mongock.changeLogsScanPackage=" + packagePath : "",
                    "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", container.getContainerIpAddress(), container.getFirstMappedPort()),
                    "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
            ).run();
  }


}
