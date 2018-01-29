package io.dreamfly.grpc.internal;

import io.dreamfly.grpc.GrpcConnectionBuilder;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GrpcConnectionBuilderTest {

    @Test
    // only run locally since it needs an grpc instance run on localhost
    @Disabled
    public void build() throws Exception {
        ManagedChannel managedChannel = GrpcConnectionBuilder.newBuilder().uri("grpc://127.0.0.1").build();

//        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forAddress("127.0.0.1", 2379)
//                //NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forTarget(uri)
//                .usePlaintext(true);
//        ManagedChannel managedChannel = nettyChannelBuilder.build();
    }

}