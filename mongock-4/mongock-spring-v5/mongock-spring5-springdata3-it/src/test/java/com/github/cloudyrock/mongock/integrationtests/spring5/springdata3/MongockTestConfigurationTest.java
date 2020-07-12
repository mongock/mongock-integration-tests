package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;


import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.testConfiguration.TestConfigurationChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.util.MongoContainer;
import com.github.cloudyrock.spring.v5.MongockTestConfiguration;
import org.bson.Document;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@EnableMongoRepositories(basePackageClasses = ClientRepository.class)
@ContextConfiguration(initializers = MongockTestConfigurationTest.ApplicationConfigurationInitializer.class)
public class MongockTestConfigurationTest {

    @Autowired
    private SpringDataMongo3Driver mongockDriver;

    private MongockTemplate mongockTemplate;
    private static MongoContainer mongo;

    @Configuration
    @MongockTestConfiguration
    public static class ApplicationConfigurationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            mongo = RuntimeTestUtil.startMongoDbContainer("mongo:4.2.6");
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, mongo.getReplicaSetUrl());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, "spring.data.mongodb.database=" + RuntimeTestUtil.DEFAULT_DATABASE_NAME);
        }
    }

    @AfterAll
    static void tearDown() {
        mongo.stop();
        mongo.close();
    }

    @BeforeEach
    void beforeEach() {
        this.mongockTemplate = mongockDriver.getMongockTemplate();
    }

//    @Test
    @Disabled
    void shouldInjectMongockTemplateToContext_wheMongockTestConfigurationAnnotation() {
        mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).deleteMany(new Document().append("field", "value"));
        new TestConfigurationChangeLog().testConfigurationWithMongockTemplate(mongockTemplate);
        Assertions.assertEquals(
                1,
                mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).countDocuments(new Document().append("field", "value")));
    }
}

