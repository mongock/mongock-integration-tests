package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;


import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.spring.DateToZonedDateTimeConverter;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.spring.ZonedDateTimeToDateConverter;
import com.github.cloudyrock.spring.v5.EnableMongock;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Using @EnableMongock with minimal configuration only requires changeLog package to scan
 * in property file
 *
 * RUN this application with dev profile
 */
@EnableMongock
@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = ClientRepository.class)
public class Mongock4Spring5SpringData3App {

    public final static String CLIENTS_COLLECTION_NAME = "clientCollection";

    public static void main(String[] args) {
        getSpringAppBuilder().run(args);
    }


    public static SpringApplicationBuilder getSpringAppBuilder() {
        return new SpringApplicationBuilder().sources(Mongock4Spring5SpringData3App.class);
    }

    // It requires MongoDb with a replicaSet
    @Bean
    @ConditionalOnExpression("${mongock.transactionable:false}")
    MongoTransactionManager transactionManager(MongoTemplate mongoTemplate) {
        mongoTemplate.createCollection("clientCollection");
        return new MongoTransactionManager(mongoTemplate.getMongoDbFactory());
    }

    /**
     * This method has been modified in order to use Changock runner instead of Mongock(deprecated).
     * This bean will be injected if SpringBoot application class(Mongock4Spring5SpringData3App) is not annotated with @EnableChangock
     */
    @Bean
    @ConditionalOnMissingBean(MongockSpring5.MongockApplicationRunner.class)
    @ConditionalOnExpression("${mongock.enabled:true}")
    public MongockSpring5.MongockApplicationRunner mongockApplicationRunner(
            ApplicationContext springContext,
            MongoTemplate mongoTemplate,
            ApplicationEventPublisher eventPublisher) {

        return MongockSpring5.builder()
                .setDriver(SpringDataMongoV3Driver.withDefaultLock(mongoTemplate))
                .addChangeLogClass(com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client.ClientUpdater2ChangeLog.class)
                .addChangeLogsScanPackage("com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client.initializer")
                .setSpringContext(springContext)
                .setEventPublisher(eventPublisher)
                .buildApplicationRunner();
    }




    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(DateToZonedDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }
}
