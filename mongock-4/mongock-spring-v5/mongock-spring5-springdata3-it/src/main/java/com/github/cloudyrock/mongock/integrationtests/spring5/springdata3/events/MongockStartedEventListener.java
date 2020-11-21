package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3.events;

import io.changock.runner.spring.util.events.SpringMigrationStartedEvent;
import io.changock.runner.spring.util.events.SpringMigrationSuccessEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MongockStartedEventListener implements ApplicationListener<SpringMigrationStartedEvent> {

    @Override
    public void onApplicationEvent(SpringMigrationStartedEvent event) {
        System.out.println("[EVENT LISTENER] - Mongock STARTED successfully");
    }

}
