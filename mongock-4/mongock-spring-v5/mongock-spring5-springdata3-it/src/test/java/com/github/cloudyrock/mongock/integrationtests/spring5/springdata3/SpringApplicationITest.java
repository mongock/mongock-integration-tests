package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client.initializer.ClientInitializerChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import io.changock.migration.api.exception.ChangockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO add enum for mongo versions
// TODO add methodSources to automatize parametrization

@Testcontainers
class SpringApplicationITest {

    private ConfigurableApplicationContext ctx;


    @AfterEach
    void closingSpringApp() {
        if(ctx != null) {
            ctx.close();
            await().atMost(1, TimeUnit.MINUTES)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .until(()-> !ctx.isActive());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0", "mongo:3.6.3"})
    void SpringApplicationShouldRunChangeLogs(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        assertEquals(ClientInitializerChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void ApplicationRunnerShouldBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        ctx.getBean(MongockSpring5.MongockApplicationRunner.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void InitializingBeanShouldNotBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        Exception ex = assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ctx.getBean(MongockSpring5.MongockInitializingBeanRunner.class),
                "MongockInitializingBeanRunner should not be injected to the context as runner-type is not set");
        assertEquals(
                "No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpring5$MongockInitializingBeanRunner' available",
                ex.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldThrowExceptionWhenScanPackageNotSpecified(String mongoVersion) {
        Exception ex = assertThrows(
                IllegalStateException.class,
                () -> RuntimeTestUtil.startSpringAppWithMongoDbVersionAndNoPackage(mongoVersion));
        assertEquals(ChangockException.class, ex.getCause().getClass());
        assertEquals("Scan package for changeLogs is not set: use appropriate setter", ex.getCause().getMessage());
    }


}
