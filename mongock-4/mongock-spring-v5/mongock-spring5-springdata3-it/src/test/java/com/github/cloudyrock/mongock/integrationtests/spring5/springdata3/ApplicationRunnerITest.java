package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;


//TODO move to JUnit 5 and add parameterized tests for different versions of MongoDb
// - replicaset on testcontainers : https://github.com/testcontainers/testcontainers-java/issues/1387
// - 3.x With no transactions
// - 4.0.X With transactions in replica sets
// - 4.2.X with transactions in sharded collections


import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.changelogs.MongockSync4LegacyMigrationChangeLog;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.general.AnotherMongockTestResource;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.general.MongockTestResource;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.withChangockAnnotations.ChangeLogwithChangockAnnotations;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class ApplicationRunnerITest {


    private static final String CHANGELOG_COLLECTION_NAME = "mongockChangeLog";
    private static final String TEST_RESOURCE_CLASSPATH = MongockTestResource.class.getPackage().getName();
    private static String LEGACY_CHANGELOG_COLLECTION_NAME = "dbchangelog";

    private MongoClient mongoClient;
    private MongoTemplate mongoTemplate;


    void start(String mongoVersion) {
        GenericContainer mongo = RuntimeTestUtil.startMongoContainer(mongoVersion);
        String connectionString = String.format("mongodb://%s:%d", mongo.getContainerIpAddress(), mongo.getFirstMappedPort());
        mongoClient = MongoClients.create(connectionString);
        mongoTemplate = new MongoTemplate(mongoClient, RuntimeTestUtil.DEFAULT_DATABASE_NAME);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldBuildInitializingBeanRunner(String mongoVersion) {
        start(mongoVersion);
        // given
        assertEquals(
                MongockSpring5.MongockApplicationRunner.class,
                getBasicBuilder(TEST_RESOURCE_CLASSPATH).buildApplicationRunner().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldBuildApplicationRunner(String mongoVersion) {
        start(mongoVersion);
        // given
        assertEquals(
                MongockSpring5.MongockInitializingBeanRunner.class,
                getBasicBuilder(TEST_RESOURCE_CLASSPATH).buildInitializingBeanRunner().getClass());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldExecuteAllChangeSets(String mongoVersion) {
        start(mongoVersion);
        // given, then
        getBasicBuilder(TEST_RESOURCE_CLASSPATH).buildApplicationRunner().execute();

        // db changelog collection checking
        long change1 = this.mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME)
                .countDocuments(new Document().append("changeId", "test1").append("author", "testuser"));
        assertEquals(1, change1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldStoreMetadata_WhenChangeSetIsTrack_IfAddedInBuilder(String mongoVersion) {
        start(mongoVersion);
        // given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("string_key", "string_value");
        metadata.put("integer_key", 10);
        metadata.put("float_key", 11.11F);
        metadata.put("double_key", 12.12D);
        metadata.put("long_key", 13L);
        metadata.put("boolean_key", true);

        // then
        getBasicBuilder(TEST_RESOURCE_CLASSPATH)
                .withMetadata(metadata)
                .buildApplicationRunner()
                .execute();

        // then
        Map metadataResult = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).find().first().get("metadata", Map.class);
        assertEquals("string_value", metadataResult.get("string_key"));
        assertEquals(10, metadataResult.get("integer_key"));
        assertEquals(11.11F, (Double) metadataResult.get("float_key"), 0.01);
        assertEquals(12.12D, (Double) metadataResult.get("double_key"), 0.01);
        assertEquals(13L, metadataResult.get("long_key"));
        assertEquals(true, metadataResult.get("boolean_key"));

    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldTwoExecutedChangeSet_whenRunningTwice_ifRunAlways(String mongoVersion) {
        start(mongoVersion);
        // given
        MongockSpring5.MongockApplicationRunner runner = getBasicBuilder(TEST_RESOURCE_CLASSPATH).buildApplicationRunner();

        // when
        runner.execute();
        runner.execute();

        // then
        List<Document> documentList = new ArrayList<>();

        mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME)
                .find(new Document().append("changeSetMethod", "testChangeSetWithAlways").append("state", "EXECUTED"))
                .forEach(documentList::add);

        assertEquals(2, documentList.size());

    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldOneExecutedAndOneIgnoredChangeSet_whenRunningTwice_ifNotRunAlwaysAndTrackIgnore(String mongoVersion) {
        start(mongoVersion);
        // given
        MongockSpring5.MongockApplicationRunner runner = getBasicBuilder(TEST_RESOURCE_CLASSPATH)
                .setTrackIgnored(true)
                .buildApplicationRunner();


        // when
        runner.execute();
        runner.execute();

        // then
        List<String> stateList = new ArrayList<>();
        mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME)
                .find(new Document()
                        .append("changeLogClass", AnotherMongockTestResource.class.getName())
                        .append("changeSetMethod", "testChangeSet"))
                .map(document -> document.getString("state"))
                .forEach(stateList::add);
        assertEquals(2, stateList.size());
        assertTrue(stateList.contains("EXECUTED"));
        assertTrue(stateList.contains("IGNORED"));
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldOneExecutedAndNoIgnoredChangeSet_whenRunningTwice_ifNotRunAlwaysAndNotTrackIgnore(String mongoVersion) {
        start(mongoVersion);
        // given
        MongockSpring5.MongockApplicationRunner runner = getBasicBuilder(TEST_RESOURCE_CLASSPATH)
                .buildApplicationRunner();


        // when
        runner.execute();
        runner.execute();

        // then
        List<String> stateList = new ArrayList<>();
        mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME)
                .find(new Document()
                        .append("changeLogClass", AnotherMongockTestResource.class.getName())
                        .append("changeSetMethod", "testChangeSet"))
                .map(document -> document.getString("state"))
                .forEach(stateList::add);
        assertEquals(1, stateList.size());
        assertTrue(stateList.contains("EXECUTED"));
        assertFalse(stateList.contains("IGNORED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldExecuteChangockAnnotations(String mongoVersion) {
        start(mongoVersion);
        // given, then
        getBasicBuilder(ChangeLogwithChangockAnnotations.class.getPackage().getName()).buildApplicationRunner().execute();

        // then
        long changeWithChangockAnnotations = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document()
                .append("changeId", "withChangockAnnotations")
                .append("author", "testuser")
                .append("state", "EXECUTED"));
        assertEquals(1, changeWithChangockAnnotations);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldPerformLegacyMigration(String mongoVersion) {
        legacyMigrationTest(mongoVersion, 1);

        // then too
        long change1Count = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document()
                .append("changeId", "mongock-legacy-migration")
                .append("author", "mongock")
                .append("state", "EXECUTED")
                .append("changeLogClass", "com.github.cloudyrock.mongock.driver.mongodb.sync.v4.changelogs.MongockSync4LegacyMigrationChangeLog")
                .append("changeSetMethod", "mongockSpringLegacyMigration"));
        assertEquals(1, change1Count);
    }


    @ParameterizedTest
    @ValueSource(strings = {"mongo:4.2.0"})
    void shouldNotDuplicateLegacyChangeLogs_WhenLegacyMigrationReapplied(String mongoVersion) {
        legacyMigrationTest(mongoVersion, 2);

        // then too
        long change1Count = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document()
                .append("changeId", "mongock-legacy-migration")
                .append("author", "mongock")
                .append("state", "EXECUTED")
                .append("changeLogClass", "com.github.cloudyrock.mongock.driver.mongodb.sync.v4.changelogs.MongockSync4LegacyMigrationChangeLog")
                .append("changeSetMethod", "mongockSpringLegacyMigration"));
        assertEquals(2, change1Count);
    }


    void legacyMigrationTest(String mongoVersion, int executions) {
        start(mongoVersion);
        // given, then
        setUpLegacyMigration();
        MongockSpring5.MongockApplicationRunner runner = getBasicBuilder(MongockSync4LegacyMigrationChangeLog.class.getPackage().getName())
                .setLegacyMigration(new MongockLegacyMigration(LEGACY_CHANGELOG_COLLECTION_NAME))
                .buildApplicationRunner();

        for(int i=0 ; i< executions; i++) {
            runner.execute();
        }

        // then
        long change1Count = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document()
                .append("changeId", "01-addAuthorities")
                .append("author", "initiator")
                .append("changeLogClass", "io.cloudyrock.mongock.legacy.dbmigrations.InitialSetupMigration")
                .append("changeSetMethod", "addAuthorities"));
        assertEquals(1, change1Count);

        long change2Count = mongoTemplate.getDb().getCollection(CHANGELOG_COLLECTION_NAME).countDocuments(new Document()
                .append("changeId", "02-addUsers")
                .append("author", "initiator")
                .append("changeLogClass", "io.cloudyrock.mongock.legacy.dbmigrations.InitialSetupMigration")
                .append("changeSetMethod", "addUsers"));
        assertEquals(1, change2Count);
    }

    private SpringDataMongo3Driver buildDriver() {
        SpringDataMongo3Driver driver = new SpringDataMongo3Driver(mongoTemplate);
        driver.setChangeLogCollectionName(CHANGELOG_COLLECTION_NAME);
        return driver;
    }

    private MongockSpring5.Builder getBasicBuilder(String packagePath) {
        return MongockSpring5.builder()
                .setDriver(buildDriver())
                .addChangeLogsScanPackage(packagePath)
                .setSpringContext(getApplicationContext())
                .setDefaultLock();
    }

    private ApplicationContext getApplicationContext() {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        Mockito.when(context.getBean(Environment.class)).thenReturn(Mockito.mock(Environment.class));
        return context;
    }

    private void setUpLegacyMigration() {

        MongoCollection<Document> collection = mongoTemplate.getCollection(LEGACY_CHANGELOG_COLLECTION_NAME);
        collection.insertOne(new Document()
                .append("changeId", "01-addAuthorities")
                .append("author", "initiator")
                .append("changeLogClass", "io.cloudyrock.mongock.legacy.dbmigrations.InitialSetupMigration")
                .append("changeSetMethod", "addAuthorities")
                .append("timestamp", new Date()));

        collection.insertOne(new Document()
                .append("changeId", "02-addUsers")
                .append("author", "initiator")
                .append("changeLogClass", "io.cloudyrock.mongock.legacy.dbmigrations.InitialSetupMigration")
                .append("changeSetMethod", "addUsers")
                .append("timestamp", new Date()));


    }
}
