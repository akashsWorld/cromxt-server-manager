package com.cromxt.system.config;

import com.cromxt.system.models.Buckets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
@RequiredArgsConstructor
public class MongoConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final ReactiveMongoTemplate mongoTemplate;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent ignored) {
        mongoTemplate.indexOps(Buckets.class).ensureIndex(new Index().on("hostname", Sort.Direction.ASC).unique()).subscribe();
    }
}
