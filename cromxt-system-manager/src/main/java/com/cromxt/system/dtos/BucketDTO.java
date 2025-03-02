package com.cromxt.system.dtos;



public record BucketDTO(
        String bucketId,
        String hostname,
        Integer httpPort,
        Integer rpcPort
){
}
