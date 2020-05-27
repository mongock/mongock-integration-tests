package com.github.cloudyrock.spring5.springdata3.it;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.Mongock4Spring5SpringData3App;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.ClientChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class SpringApplicationITest extends NotSharedMongoContainerTestBase {


    private ConfigurableApplicationContext ctx;

    @BeforeEach
    void setUpApplication() {

        ctx = Mongock4Spring5SpringData3App.getSpringAppBuilder()
                .properties(
                        "server.port=0",// random port
                        "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", mongo.getContainerIpAddress(), mongo.getFirstMappedPort()),
                        "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
                ).run();
    }

    @AfterEach
    void closingSpringApp() {
        ctx.close();
        await()
                .atMost(1, TimeUnit.MINUTES)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(()-> !ctx.isActive());
    }

    @Test
    void SpringApplicationShouldRunChangeLogs() {
        assertEquals(ClientChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }

    @Test
    void ApplicationRunnerShouldBeInjected() {
        ctx.getBean(MongockSpring5.MongockApplicationRunner.class);
    }

    @Test
    void InitializingBeanShouldNotBeInjected() {
        Exception ex = assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ctx.getBean(MongockSpring5.MongockInitializingBeanRunner.class),
                "MongockInitializingBeanRunner should not be injected to the context as runner-type is not set");
        assertEquals(
                "No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpring5$MongockInitializingBeanRunner' available",
                ex.getMessage()
        );
    }
}
