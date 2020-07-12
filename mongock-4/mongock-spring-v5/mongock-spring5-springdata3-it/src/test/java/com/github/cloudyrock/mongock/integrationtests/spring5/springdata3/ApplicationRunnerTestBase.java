package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.changelogs.runalways.MongockSync4LegacyMigrationChangeRunAlwaysLog;
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.changelogs.runonce.MongockSync4LegacyMigrationChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.util.Constants;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.util.LegacyMigrationUtils;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.util.MongoContainer;
import com.github.cloudyrock.mongock.migration.MongockLegacyMigration;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class ApplicationRunnerTestBase {

    protected MongoClient mongoClient;
    protected MongoTemplate mongoTemplate;

    protected void start(String mongoVersion) {
        MongoContainer mongoDBContainer = RuntimeTestUtil.startMongoDbContainer(mongoVersion);
        mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(mongoClient, RuntimeTestUtil.DEFAULT_DATABASE_NAME);
    }

    protected SpringDataMongo3Driver buildDriver() {
        SpringDataMongo3Driver driver = SpringDataMongo3Driver.withDefaultLock(mongoTemplate);
        driver.setChangeLogCollectionName(Constants.CHANGELOG_COLLECTION_NAME);
        return driver;
    }

    protected MongockSpring5.Builder getBasicBuilder(String packagePath) {
        return MongockSpring5.builder()
                .setDriver(buildDriver())
                .addChangeLogsScanPackage(packagePath)
                .setSpringContext(getApplicationContext());
    }

    protected ApplicationContext getApplicationContext() {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        Mockito.when(context.getBean(Environment.class)).thenReturn(Mockito.mock(Environment.class));
        return context;
    }
}
