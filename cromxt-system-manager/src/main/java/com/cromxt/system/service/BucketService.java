package com.cromxt.system.service;

import com.cromxt.common.crombucket.kafka.BucketObject;
import com.cromxt.system.dtos.BucketRequestDTO;
import com.cromxt.system.dtos.response.BucketListResponse;
import com.cromxt.system.dtos.response.BucketResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BucketService {
    Mono<BucketListResponse> findAllBuckets();

    Flux<BucketObject> getAllRegisteredBuckets();

    Mono<BucketListResponse> saveBucketsFromServerJSONFile(FilePart serverJsonFile);

    Mono<BucketResponse> createBucket(BucketRequestDTO bucketRequestDTO);

    Mono<Void> deleteBucketById(String bucketId);

    Mono<BucketResponse> updateBucket(String bucketId, BucketRequestDTO bucketRequestDTO);

    Mono<Void> updateBucketsFromServerJSON();

    Mono<Void> deleteAllBuckets();
}
