package com.cromxt.system.controller;


import com.cromxt.common.crombucket.dtos.BaseResponse;
import com.cromxt.common.crombucket.dtos.CromxtResponseStatus;
import com.cromxt.common.crombucket.dtos.ErrorResponse;
import com.cromxt.system.dtos.BucketRequestDTO;
import com.cromxt.system.dtos.response.BucketListResponse;
import com.cromxt.system.service.BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {


    private final BucketService bucketService;


    @GetMapping
    public Mono<ResponseEntity<BaseResponse>> getAllBuckets() {
        Mono<BucketListResponse> allBuckets = bucketService.findAllBuckets();
        return allBuckets
                .map(bucketListResponse -> new ResponseEntity<BaseResponse>(bucketListResponse, HttpStatus.OK))
                .onErrorResume(e-> Mono.just(
                        new ResponseEntity<>(
                                new ErrorResponse(e.getMessage(), CromxtResponseStatus.ERROR),
                                HttpStatus.BAD_REQUEST
                        )
                ));

    }

    @PostMapping
    public Mono<ResponseEntity<BaseResponse>> addBucket(@RequestBody BucketRequestDTO bucketRequestDTO) {
        return bucketService.createBucket(bucketRequestDTO)
                .map(bucketResponse -> new ResponseEntity<BaseResponse>(bucketResponse,HttpStatus.CREATED))
                .onErrorResume(e->Mono.just(
                        new ResponseEntity<>(
                                new ErrorResponse(e.getMessage(), CromxtResponseStatus.ERROR),
                                HttpStatus.BAD_REQUEST
                )));
    }

    @DeleteMapping("/{bucketId}")
    public Mono<ResponseEntity<BaseResponse>> deleteBucket(@PathVariable String bucketId) {
        return bucketService.deleteBucketById(bucketId)
                .then(Mono.just(new ResponseEntity<>(new BaseResponse(CromxtResponseStatus.SUCCESS), HttpStatus.ACCEPTED)))
                .onErrorResume(e->Mono.just(
                        new ResponseEntity<>(
                                new ErrorResponse(e.getMessage(), CromxtResponseStatus.ERROR),
                                HttpStatus.BAD_REQUEST
                        )));
    }

    @PutMapping("/{bucketId}")
    public Mono<ResponseEntity<BaseResponse>> updateBucket(@PathVariable String bucketId,
                                                                 @RequestBody BucketRequestDTO bucketRequestDTO) {
        return bucketService.updateBucket(bucketId, bucketRequestDTO).map(bucketResponse ->
                new ResponseEntity<BaseResponse>(bucketResponse, HttpStatus.ACCEPTED))
                .onErrorResume(e->Mono.just(
                        new ResponseEntity<>(
                                new ErrorResponse(e.getMessage(), CromxtResponseStatus.ERROR),
                                HttpStatus.BAD_REQUEST
                )));
    }

    @DeleteMapping
    public Mono<ResponseEntity<BaseResponse>> deleteAllBuckets() {
        return bucketService.deleteAllBuckets()
                .then(Mono.just(new ResponseEntity<>(new BaseResponse(CromxtResponseStatus.SUCCESS), HttpStatus.ACCEPTED)))
                .onErrorResume(e -> Mono.just(new ResponseEntity<>(new ErrorResponse(
                        e.getMessage(),
                        CromxtResponseStatus.ERROR
                ), HttpStatus.BAD_REQUEST)));
    }

}
