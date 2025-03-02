package com.cromxt.system.repository;


import com.cromxt.system.models.Buckets;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BucketsRepository extends ReactiveMongoRepository<Buckets, String> {
}
