package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;


import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.testConfiguration.TestConfigurationChangeLog;
import com.github.cloudyrock.spring.v5.MongockTestConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


//TODO: Use testContainers here
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = MongockTestConfigurationTest.ApplicationConfiguration.class)
public class MongockTestConfigurationTest {


    @Autowired
    private SpringDataMongo3Driver mongockDriver;

    private MongockTemplate mongockTemplate;

    @Configuration
    @MongockTestConfiguration
    public static class ApplicationConfiguration {
    }

    @BeforeEach
    void beforeEach() {
        this.mongockTemplate = mongockDriver.getMongockTemplate();
        mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).deleteMany(new Document().append("field", "value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldInjectMongockTemplateToContext_wheMongockTestConfigurationAnnotation() {

        new TestConfigurationChangeLog().testConfigurationWithMongockTemplate(mongockTemplate);

        Assertions.assertEquals(
                1,
                mongockTemplate.getCollection(TestConfigurationChangeLog.COLLECTION_NAME).countDocuments(new Document().append("field", "value")));

    }

}
