package io.dreamfly.grpc;

import com.google.common.base.Strings;

import io.dreamfly.grpc.internal.nameresolver.GrpcNameResolverProvider;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * gRPC client builder.
 */
public class GrpcConnectionBuilder {

    private String uri;

    private GrpcConnectionBuilder() {
        // no public constructor
    }

    /**
     * create new grpc Connection builder.
     *
     * @return GrpcConnectionBuilder.
     */
    public static GrpcConnectionBuilder newBuilder() {
        return new GrpcConnectionBuilder();
    }

    /**
     * set grpc uri
     *
     * @param uri grpc uri address
     * @return GrpcConnectionBuilder
     */
    public GrpcConnectionBuilder uri(String uri) {
        checkArgument(!Strings.isNullOrEmpty(uri), "uri should not be null or empty");
        checkScheme(uri);

        this.uri = uri;
        return this;
    }

    private void checkScheme(String uriContent) {
        URI uri;
        try {
            uri = new URI(uriContent);
        } catch (Exception e) {
            throw new IllegalArgumentException("uri is not a valid URI: uri=" + uriContent, e);
        }
        checkArgument(uri.getScheme().equals(GrpcNameResolverProvider.GRPC_SCHEME),
                "Scheme of uri should be '%s': uri=%s",
                GrpcNameResolverProvider.GRPC_SCHEME, uriContent);
    }

    /**
     * build managedChannel with GrpcConnectionBuilder
     *
     * @return ManagedChannel
     */
    public ManagedChannel build() {
        checkState(uri != null, "please set uri before build");

        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forTarget(uri)
                .keepAliveWithoutCalls(true)
                // we use SPI to load GrpcNameResolverProvider, so comment this line
                //.nameResolverFactory(new GrpcNameResolverProvider())
                .usePlaintext(true);
        return nettyChannelBuilder.build();
    }
}
