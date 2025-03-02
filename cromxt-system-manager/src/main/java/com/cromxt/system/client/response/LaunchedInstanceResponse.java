package com.cromxt.system.client.response;

public record LaunchedInstanceResponse(
        Integer httpPort,
        Integer rpcPort
) {
}
