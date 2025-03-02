package com.cromxt.system.service.impl;

import com.cromxt.common.crombucket.dtos.CromxtResponseStatus;
import com.cromxt.common.crombucket.kafka.BucketObject;
import com.cromxt.common.crombucket.kafka.BucketUpdateRequest;
import com.cromxt.common.crombucket.kafka.Method;
import com.cromxt.system.client.ServerClient;
import com.cromxt.system.dtos.BucketDTO;
import com.cromxt.system.dtos.BucketRequestDTO;
import com.cromxt.system.dtos.response.BucketListResponse;
import com.cromxt.system.dtos.response.BucketResponse;
import com.cromxt.system.exception.InvalidBucketDetails;
import com.cromxt.system.exception.InvalidServerJSONFile;
import com.cromxt.system.models.Buckets;
import com.cromxt.system.repository.BucketsRepository;
import com.cromxt.system.service.BucketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BucketServiceImpl implements BucketService {

    private final BucketsRepository bucketsRepository;
    private final ServerClient serverClient;
    private final UpdateBucketKafkaProducer updateBucketKafkaProducer;

    @Override
    public Mono<BucketListResponse> findAllBuckets() {
        return bucketsRepository.findAll().map(
                buckets -> new BucketDTO(
                        buckets.getId(),
                        buckets.getHostname(),
                        buckets.getHttpPort(),
                        buckets.getRpcPort()
                )
        ).collectList().map(bucketsList -> new BucketListResponse(
                        CromxtResponseStatus.SUCCESS,
                        bucketsList
                )
        );

    }

    @Override
    public Flux<BucketObject> getAllRegisteredBuckets() {
        return bucketsRepository.findAll().map(savedBuckets->BucketObject.builder()
                .bucketId(savedBuckets.getId())
                .hostName(savedBuckets.getHostname())
                .httpPort(savedBuckets.getHttpPort())
                .rpcPort(savedBuckets.getRpcPort())
                .build());
    }

    @Override
    public Mono<BucketListResponse> saveBucketsFromServerJSONFile(FilePart serverJsonFile) {
        // TODO: Implement later
        return Mono.empty();
    }

    @Override
    public Mono<BucketResponse> createBucket(BucketRequestDTO bucketRequestDTO) {
        String hostName = bucketRequestDTO.hostname();
        Buckets buckets = Buckets.builder()
                .hostname(hostName)
                .build();

        return bucketsRepository.save(buckets)
                .flatMap(savedBucket -> {

                    String bucketId = savedBucket.getId();

                    return serverClient.launchNewBucket(bucketId, hostName, bucketRequestDTO.port())
                            .flatMap(launchedInstance -> {

                                savedBucket.setHttpPort(launchedInstance.httpPort());
                                savedBucket.setRpcPort(launchedInstance.rpcPort());

                                return bucketsRepository.save(savedBucket).flatMap(updatedBucket -> {

                                    return updateBucketKafkaProducer.updateBucket(new BucketUpdateRequest(
                                            Method.ADD,
                                            BucketObject.builder()
                                                    .bucketId(bucketId)
                                                    .hostName(hostName)
                                                    .httpPort(updatedBucket.getHttpPort())
                                                    .rpcPort(updatedBucket.getHttpPort())
                                                    .build()
                                    )).then(Mono.just(new BucketResponse(CromxtResponseStatus.SUCCESS, new BucketDTO(
                                            savedBucket.getId(),
                                            savedBucket.getHostname(),
                                            savedBucket.getHttpPort(),
                                            savedBucket.getRpcPort()))));
                                });

                            });

                });

    }

    @Override
    public Mono<Void> deleteBucketById(String bucketId) {
        return bucketsRepository.findById(bucketId)
                .switchIfEmpty(Mono.error(new InvalidBucketDetails("Bucket not found")))
                .flatMap(bucket -> bucketsRepository.delete(bucket).then(updateBucketKafkaProducer.updateBucket(
                        new BucketUpdateRequest(
                                Method.DELETE,
                                BucketObject.builder()
                                        .bucketId(bucketId)
                                        .build()
                        ))));
    }

    @Override
    public Mono<BucketResponse> updateBucket(String bucketId, BucketRequestDTO bucketRequestDTO) {
        String hostName = bucketRequestDTO.hostname();
        return bucketsRepository.findById(bucketId)
                .flatMap(savedBucket -> serverClient.launchNewBucket(bucketId, hostName, bucketRequestDTO.port())
                        .flatMap(launchedInstance -> {

                            savedBucket.setHostname(hostName);
                            savedBucket.setRpcPort(launchedInstance.rpcPort());
                            savedBucket.setHttpPort(launchedInstance.httpPort());

                            return bucketsRepository.save(savedBucket).map(updatedBucket -> {

                                updateBucketKafkaProducer.updateBucket(
                                        new BucketUpdateRequest(
                                                Method.UPDATE,
                                                BucketObject.builder()
                                                        .hostName(hostName)
                                                        .httpPort(updatedBucket.getHttpPort())
                                                        .rpcPort(updatedBucket.getRpcPort())
                                                        .build()
                                        )
                                );

                                return new BucketResponse(CromxtResponseStatus.SUCCESS,
                                        new BucketDTO(bucketId,
                                                hostName,
                                                updatedBucket.getHttpPort(),
                                                updatedBucket.getRpcPort())
                                );

                            });
                        }));
    }

    @Override
    public Mono<Void> updateBucketsFromServerJSON() {
        // TODO: Implement later.
        return null;
    }

    @Override
    public Mono<Void> deleteAllBuckets() {
        return bucketsRepository.findAll().doOnNext(bukcetObject -> {
            updateBucketKafkaProducer.updateBucket(new BucketUpdateRequest(
                    Method.DELETE,
                    BucketObject.builder().bucketId(bukcetObject.getId()).build()
            ));
        }).then(bucketsRepository.deleteAll());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class BucketsJSONData {
        private List<BucketsEntities> buckets;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class BucketsEntities {
        private String hostName;
        private Integer port;
    }

    private <T> Mono<T> parseServerJsonFile(FilePart jsonFile, Class<T> parseIn) {
        ObjectMapper objectMapper = new ObjectMapper();
        return jsonFile.content()
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return Mono.just(new String(bytes, StandardCharsets.UTF_8));
                })
                .collectList()
                .map(strings -> String.join("", strings))
                .mapNotNull(dataString -> {
                    try {
                        return objectMapper.readValue(dataString, parseIn);
                    } catch (JsonProcessingException e) {
                        throw new InvalidServerJSONFile(e.getMessage());
                    }
                });
    }

}
