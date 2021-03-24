package com.github.cloudyrock.mongock.integrationtests;

import com.github.cloudyrock.mongock.exception.MongockException;
import com.github.cloudyrock.mongock.integrationtests.changelogs.client.initializer.ClientInitializerChangeLog;
import com.github.cloudyrock.mongock.integrationtests.changelogs.transaction.commitNonFailFast.CommitNonFailFastChangeLog;
import com.github.cloudyrock.mongock.integrationtests.changelogs.transaction.rollback.RollbackChangeLog;
import com.github.cloudyrock.mongock.integrationtests.changelogs.transaction.successful.TransactionSuccessfulChangeLog;
import com.github.cloudyrock.mongock.integrationtests.client.ClientRepository;
import com.github.cloudyrock.mongock.integrationtests.util.MongoContainer;
import com.github.cloudyrock.springboot.v2_2.MongockSpringbootV2_2;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO add methodSources to automatize parametrization

@Testcontainers
class SpringApplicationITest {

    private ConfigurableApplicationContext ctx;


    @AfterEach
    void closingSpringApp() {
        if (ctx != null) {
            ctx.close();
            await().atMost(1, TimeUnit.MINUTES)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .until(() -> !ctx.isActive());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6", "mongo:3.6.3"})
    void SpringApplicationShouldRunChangeLogs(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        assertEquals(ClientInitializerChangeLog.INITIAL_CLIENTS, ctx.getBean(ClientRepository.class).count());
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void ApplicationRunnerShouldBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        ctx.getBean(MongockSpringbootV2_2.MongockApplicationRunner.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void ApplicationRunnerShouldNotBeInjected_IfDisabledByProperties(String mongoVersion) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("mongock.enabled", "false");
        parameters.put("mongock.changeLogsScanPackage", "com.github.cloudyrock.mongock.integrationtests.changelogs.client");
        parameters.put("mongock.transactionable", "false");
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndParameters(mongoVersion, parameters);
        Exception ex = assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ctx.getBean(MongockSpringbootV2_2.MongockApplicationRunner.class));
        assertEquals(
                "No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpringbootV2_2$MongockApplicationRunner' available",
                ex.getMessage()
        );
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void InitializingBeanShouldNotBeInjected(String mongoVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndDefaultPackage(mongoVersion);
        Exception ex = assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> ctx.getBean(MongockSpringbootV2_2.MongockInitializingBeanRunner.class),
                "MongockInitializingBeanRunner should not be injected to the context as runner-type is not set");
        assertEquals(
                "No qualifying bean of type 'com.github.cloudyrock.spring.v5.MongockSpringbootV2_2$MongockInitializingBeanRunner' available",
                ex.getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void shouldThrowExceptionWhenScanPackageNotSpecified(String mongoVersion) {
        Exception ex = assertThrows(
                IllegalStateException.class,
                () -> RuntimeTestUtil.startSpringAppWithMongoDbVersionAndNoPackage(mongoVersion));
        assertEquals(MongockException.class, ex.getCause().getClass());
        assertEquals("Scan package for changeLogs is not set: use appropriate setter", ex.getCause().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void shouldRollBack_IfTransaction_WhenExceptionInChangeLog(String mongoDbVersion) {
        MongoContainer mongoContainer = RuntimeTestUtil.startMongoDbContainer(mongoDbVersion);


        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("mongock.changeLogsScanPackage", RollbackChangeLog.class.getPackage().getName());
            ctx = RuntimeTestUtil.startSpringAppWithParameters(mongoContainer, parameters);
        } catch (Exception ex) {
            //ignore
        }

        // then
        String replicaSetUrl = mongoContainer.getReplicaSetUrl();
        MongoDatabase database = MongoClients.create(replicaSetUrl).getDatabase(RuntimeTestUtil.DEFAULT_DATABASE_NAME);
        long actual = database.getCollection(App.CLIENTS_COLLECTION_NAME).countDocuments();
        assertEquals(0, actual);

        MongoCollection changeLogCollection = database.getCollection("mongockChangeLog");
        FindIterable changeLogs = changeLogCollection.find();
        MongoCursor cursor = changeLogs.iterator();
        while(cursor.hasNext()) {
            System.out.println(cursor.next());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void shouldCommit_IfTransaction_WhenChangeLogOK(String mongoDbVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndPackage(mongoDbVersion, TransactionSuccessfulChangeLog.class.getPackage().getName());

        // then
        assertEquals(10, ctx.getBean(ClientRepository.class).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void shouldCommit_IfChangeLogFail_WhenNonFailFast(String mongoDbVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithMongoDbVersionAndPackage(mongoDbVersion, CommitNonFailFastChangeLog.class.getPackage().getName());

        // then
        assertEquals(10, ctx.getBean(ClientRepository.class).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.6"})
    void shouldNotExecuteTransaction_IfConfigurationTransactionDisabled(String mongoDbVersion) {
        ctx = RuntimeTestUtil.startSpringAppWithTransactionDisabledMongoDbVersionAndPackage(mongoDbVersion, CommitNonFailFastChangeLog.class.getPackage().getName());

        // then
        assertEquals(10, ctx.getBean(ClientRepository.class).count());
    }


}
