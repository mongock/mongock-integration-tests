package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import io.changock.runner.spring.util.events.DbMigrationSuccessEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MongockFailEventListener implements ApplicationListener<DbMigrationSuccessEvent>{

    @Override
    public void onApplicationEvent(DbMigrationSuccessEvent event) {
        System.out.println(event);
    }

}
