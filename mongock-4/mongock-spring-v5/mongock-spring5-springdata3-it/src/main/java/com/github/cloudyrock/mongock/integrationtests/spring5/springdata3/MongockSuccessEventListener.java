package com.github.cloudyrock.mongock.integrationtests.spring5.springdata3;

import io.changock.runner.spring.util.events.DbMigrationFailEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MongockSuccessEventListener implements ApplicationListener<DbMigrationFailEvent>{

    @Override
    public void onApplicationEvent(DbMigrationFailEvent event) {
        System.out.println(event);
    }

}
