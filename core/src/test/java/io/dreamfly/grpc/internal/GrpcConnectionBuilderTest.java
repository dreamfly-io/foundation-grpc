package io.dreamfly.grpc.internal;

import io.dreamfly.grpc.GrpcConnectionBuilder;
import io.dreamfly.grpc.test.*;
import io.dreamfly.grpc.test.ControllableServiceGrpc.ControllableServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyChannelBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrpcConnectionBuilderTest extends ControllableServiceAware {
    private static Logger logger = LoggerFactory.getLogger(GrpcConnectionBuilderTest.class);
    private final String url = "grpc://127.0.0.1:10011,127.0.0.1:10012,127.0.0.1:10013";

    public GrpcConnectionBuilderTest() {
        super(new int[]{10011, 10012, 10013});
    }

    @Test
    public void build() {
        ManagedChannel managedChannel = GrpcConnectionBuilder.newBuilder().uri(url).build();

        ControllableServiceBlockingStub controllableService = ControllableServiceGrpc.newBlockingStub(managedChannel);
        ControllableRequest request = ControllableRequest.newBuilder()
                .setExpectedLatency(0)
                .setRequestId(1)
                .build();
        ControllableResponse response = controllableService.execute(request);
        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(1);
    }

}