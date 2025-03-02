package com.cromxt.system.service.impl;



import com.cromxt.common.crombucket.kafka.BucketUpdateRequest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateBucketKafkaProducer {

    private final String UPDATE_BUCKET_TOPIC;
    private final KafkaTemplate<String, BucketUpdateRequest> kafkaTemplate;

    public UpdateBucketKafkaProducer(KafkaTemplate<String, BucketUpdateRequest> kafkaTemplate,
                                     Environment environment) {
        this.kafkaTemplate = kafkaTemplate;
        String topic = environment.getProperty("CROMXT_SYSTEM_MANAGER_CONFIG_BUCKET_UPDATE_TOPIC", String.class);
        assert topic != null;
        this.UPDATE_BUCKET_TOPIC = topic;

    }

    public Mono<Void> updateBucket(BucketUpdateRequest bucketsUpdateRequest) {
        return Mono.create((sink) -> {
            try {
                kafkaTemplate.send(UPDATE_BUCKET_TOPIC, bucketsUpdateRequest);
            } catch (Exception e) {
                sink.error(e);
            }
            sink.success();
        });
    }
}
