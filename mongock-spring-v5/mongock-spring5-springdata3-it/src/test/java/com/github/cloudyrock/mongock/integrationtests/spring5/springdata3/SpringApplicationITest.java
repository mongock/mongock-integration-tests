package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client.ClientChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        ctx = RuntimeTestUtil.startSpringAppWithMongoVersionAndDefaultPackage(mongoVersion);
        assertEquals(ClientChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void ApplicationRunnerShouldBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoVersionAndDefaultPackage(mongoVersion);
        ctx.getBean(MongockSpring5.MongockApplicationRunner.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void InitializingBeanShouldNotBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoVersionAndDefaultPackage(mongoVersion);
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
                BeanCreationException.class,
                () -> RuntimeTestUtil.startSpringAppWithMongoVersionAndNoPackage(mongoVersion));
        assertTrue(ex.getMessage().contains("Mongock: You need to specify property: spring.mongock.changeLogsScanPackage"));
    }


}
