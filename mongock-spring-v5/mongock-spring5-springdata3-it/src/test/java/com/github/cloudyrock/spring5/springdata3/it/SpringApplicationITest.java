package com.github.cloudyrock.spring5.springdata3.it;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.Mongock4Spring5SpringData3App;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.ClientChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class SpringApplicationITest {

    private static final String MONGO_CONTAINER = "mongo:4.2.0";
    private static final Integer MONGO_PORT = 27017;
    private static final String DEFAULT_DATABASE_NAME = "mongocktest";

    @ClassRule
    public static GenericContainer mongo = new GenericContainer(MONGO_CONTAINER).withExposedPorts(MONGO_PORT);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static ConfigurableApplicationContext ctx;

    @BeforeClass
    public static void setUpApplication() {

        ctx = Mongock4Spring5SpringData3App.getSpringAppBuilder()
                .properties(
                        "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", mongo.getContainerIpAddress(), mongo.getFirstMappedPort()),
                        "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
                ).run();
    }

    @Test
    public void SpringApplicationShouldRunChangeLogs() {
        Assert.assertEquals(ClientChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }

    @Test
    public void ApplicationRunnerShouldBeInjected() {
        ctx.getBean(MongockSpring5.MongockApplicationRunner.class);
    }

    @Test
    public void InitializingBeanShouldNotBeInjected() {
        exceptionRule.expect(NoSuchBeanDefinitionException.class);
        exceptionRule.expectMessage("No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpring5$MongockInitializingBeanRunner' available");
        ctx.getBean(MongockSpring5.MongockInitializingBeanRunner.class);
    }
}
