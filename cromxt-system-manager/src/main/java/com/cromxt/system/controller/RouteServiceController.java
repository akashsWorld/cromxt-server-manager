package com.cromxt.system.controller;


import com.cromxt.common.crombucket.kafka.BucketObject;
import com.cromxt.system.service.BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/service/v1/buckets")
@RequiredArgsConstructor
public class RouteServiceController {

    private final BucketService bucketService;

    @GetMapping
    public Flux<BucketObject> getAllRegisteredBuckets() {
        return bucketService.getAllRegisteredBuckets();
    }
}
