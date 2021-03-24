package com.github.cloudyrock.mongock.integrationtests.client;

import com.github.cloudyrock.mongock.integrationtests.App;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository(App.CLIENTS_COLLECTION_NAME)
public interface ClientRepository extends MongoRepository<Client, String> {

}
