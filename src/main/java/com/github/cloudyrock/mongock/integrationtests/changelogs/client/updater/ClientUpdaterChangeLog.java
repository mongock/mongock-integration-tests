package com.github.cloudyrock.mongock.integrationtests.changelogs.client.updater;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.github.cloudyrock.mongock.integrationtests.App;
import com.github.cloudyrock.mongock.integrationtests.client.Client;

import java.util.List;

@ChangeLog(order = "2")
public class ClientUpdaterChangeLog {

    public final static int INITIAL_CLIENTS = 10;


    @ChangeSet(id = "data-updater-with-mongockTemplate", order = "001", author = "mongock")
    public void dataUpdater(MongockTemplate template) {
        List<Client> clients = template.findAll(Client.class, App.CLIENTS_COLLECTION_NAME);
        clients.stream()
                .map(client -> client.setName(client.getName() + "_updated"))
                .forEach(client -> template.save(client, App.CLIENTS_COLLECTION_NAME));

    }


}
