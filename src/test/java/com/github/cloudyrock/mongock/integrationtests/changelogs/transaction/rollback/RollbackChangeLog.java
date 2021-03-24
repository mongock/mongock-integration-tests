package com.github.cloudyrock.mongock.integrationtests.changelogs.transaction.rollback;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.integrationtests.client.Client;
import com.github.cloudyrock.mongock.integrationtests.client.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ChangeLog(order = "2")
public class RollbackChangeLog {

    @ChangeSet(id = "method-successful", order = "001", author = "mongock")
    public void methodSuccessful(ClientRepository clientRepository) {
        List<Client> clients = IntStream.range(0, 10)
                .mapToObj(i -> new Client("name-" + i, "email-" + i, "phone" + i, "country" + i))
                .collect(Collectors.toList());
        List<Client> result = clientRepository.saveAll(clients);
    }

    @ChangeSet(id = "method-failing", order = "002", author = "mongock")
    public void methodFailing(ClientRepository clientRepository) {
        if(true) {
            throw new RuntimeException("Transaction error");
        }
    }


}
