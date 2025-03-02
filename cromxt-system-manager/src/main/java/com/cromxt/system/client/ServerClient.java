package com.cromxt.system.client;


import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cromxt.system.client.response.LaunchedInstanceResponse;

import reactor.core.publisher.Mono;

@Service
public class ServerClient {
    private final WebClient webClient;
    private final String SERVER_CLIENT_BASE_URL;

    public ServerClient(WebClient.Builder webClient, Environment environment) {
        this.SERVER_CLIENT_BASE_URL = environment.getProperty("CROMXT_SYSTEM_MANAGER_CONFIG_SERVER_CLIENT_ADDRESS",String.class);
        this.webClient = webClient.build();
    }

    public Mono<LaunchedInstanceResponse> launchNewBucket(
            String buketId,
            String hostName,
            Integer port) {


        return Mono.just(new LaunchedInstanceResponse(9090,9091));
    }


    private String generateUrl(String protocol, String bucketId, String hostName, Integer port) {
        return "http://" + hostName + ":" + port + "/api/v1/bucket/" + bucketId;
    }
}
