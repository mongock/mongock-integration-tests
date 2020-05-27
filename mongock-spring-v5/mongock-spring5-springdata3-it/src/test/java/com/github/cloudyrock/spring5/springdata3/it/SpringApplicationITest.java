package com.github.cloudyrock.spring5.springdata3.it;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.Mongock4Spring5SpringData3App;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.ClientChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;

import static org.awaitility.Awaitility.await;

import com.github.cloudyrock.spring.v5.MongockSpring5;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO add enum for mongo versions
// TODO add methodSources to automatize parametrization

@Testcontainers
class SpringApplicationITest  extends RestarterdMongoContainerTestBase{

    private ConfigurableApplicationContext ctx;


    @AfterEach
    void closingSpringApp() {
        ctx.close();
        await()
                .atMost(1, TimeUnit.MINUTES)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(()-> !ctx.isActive());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0", "mongo:3.6.3"})
    void SpringApplicationShouldRunChangeLogs(String mongoVersion) {
        startSpringAppWithMongoVersion(mongoVersion);
        assertEquals(ClientChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0", "mongo:3.6.3"})
    void ApplicationRunnerShouldBeInjected(String mongoVersion) {
        startSpringAppWithMongoVersion(mongoVersion);
        ctx.getBean(MongockSpring5.MongockApplicationRunner.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0", "mongo:3.6.3"})
    void InitializingBeanShouldNotBeInjected(String mongoVersion) {
        startSpringAppWithMongoVersion(mongoVersion);
        Exception ex = assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ctx.getBean(MongockSpring5.MongockInitializingBeanRunner.class),
                "MongockInitializingBeanRunner should not be injected to the context as runner-type is not set");
        assertEquals(
                "No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpring5$MongockInitializingBeanRunner' available",
                ex.getMessage()
        );
    }



    private void startSpringAppWithMongoVersion(String mongoVersion) {
        GenericContainer container = startMongoContainer(mongoVersion);
        ctx = Mongock4Spring5SpringData3App.getSpringAppBuilder()
                .properties(
                        "server.port=0",// random port
                        "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", container.getContainerIpAddress(), container.getFirstMappedPort()),
                        "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
                ).run();
    }
}
