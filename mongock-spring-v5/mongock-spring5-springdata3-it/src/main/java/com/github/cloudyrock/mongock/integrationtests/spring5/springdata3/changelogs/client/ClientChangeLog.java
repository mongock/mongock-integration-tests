package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.changelogs.client;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.Client;
import com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.client.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.Mongock4Spring5SpringData3App.CLIENTS_COLLECTION_NAME;

@ChangeLog
public class ClientChangeLog {

    public final static int INITIAL_CLIENTS = 10;

    @ChangeSet(id = "data-initializer-with-repository", order = "001", author = "mongock")
    public void dataInitializer(ClientRepository clientRepository) {

        java.lang.reflect.Proxy.getInvocationHandler(clientRepository);
        List<Client> clients = IntStream.range(0, INITIAL_CLIENTS)
                .mapToObj(i -> new Client("name-" + i, "email-" + i, "phone" + i, "country" + i))
                .collect(Collectors.toList());
        List<Client> result = clientRepository.saveAll(clients);
        result.forEach(System.out::println);

    }

    @ChangeSet(id = "data-updater-with-mongockTemplate", order = "002", author = "mongock")
    public void dataUpdater(MongockTemplate template) {

        List<Client> clients = template.findAll(Client.class, CLIENTS_COLLECTION_NAME);

        clients.stream()
                .map(client -> client.setName(client.getName() + "_updated"))
                .forEach(client -> template.save(client, CLIENTS_COLLECTION_NAME));

    }


}
