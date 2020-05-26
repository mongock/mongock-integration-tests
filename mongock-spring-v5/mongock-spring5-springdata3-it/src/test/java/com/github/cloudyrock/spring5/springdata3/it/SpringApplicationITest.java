package com.github.cloudyrock.spring5.springdata3.it;

import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.Mongock4Spring5SpringData3App;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.ClientChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringApplicationITest extends IndependentDbIntegrationTestBase {

    @Test
    public void SpringApplicationShouldRunChangeLogs() {
        Assert.assertEquals(ClientChangeLog.INITIAL_CLIENTS, runSpringApp().getBean(ClientRepository.class).count());
    }

    private ConfigurableApplicationContext runSpringApp() {
        return Mongock4Spring5SpringData3App.getSpringAppBuilder()
                .properties(
                        "spring.data.mongodb.uri=" + String.format("mongodb://%s:%d", mongo.getContainerIpAddress(), mongo.getFirstMappedPort()),
                        "spring.data.mongodb.database=" + DEFAULT_DATABASE_NAME
                ).run();
    }
}
