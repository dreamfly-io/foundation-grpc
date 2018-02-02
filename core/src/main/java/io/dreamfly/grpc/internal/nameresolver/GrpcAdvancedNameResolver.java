package io.dreamfly.grpc.internal.nameresolver;

import com.google.common.base.Strings;
import io.grpc.*;
import io.grpc.internal.LogExceptionRunnable;
import io.grpc.internal.SharedResourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * gRPC NameResolver Implementation with advanced features.
 *
 * <p>Difference to DnsNameResolver, the implementation can handle multiple addresses in one line like:
 *
 * <p>grpc://192.168.0.1:2379,192.168.0.2:2379,192.168.0.3:2379
 *
 * <p>You can also use host, this resolver will act like DnsNameResolver to parse the host to ip address：
 *
 * <p>grpc://grpc.dreamfly.io:2379
 */
public class GrpcAdvancedNameResolver extends NameResolver {

    private static Logger logger = LoggerFactory.getLogger(GrpcAdvancedNameResolver.class);

    private final String scheme;
    private final String authority;
    private final String addresses;
    private final int defaultPort;

    private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;

    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private ScheduledExecutorService timerService;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ScheduledFuture<?> resolutionTask;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    private final Runnable resolutionRunnable = new ResolutionRunnable();
    private final Runnable resolutionRunnableOnExecutor = new Runnable() {

        @Override
        public void run() {
            synchronized (GrpcAdvancedNameResolver.this) {
                if (!shutdown) {
                    executor.execute(resolutionRunnable);
                }
            }
        }
    };

    public GrpcAdvancedNameResolver(String scheme,
                                    String addresses,
                                    int defaultPort,
                                    SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource,
                                    SharedResourceHolder.Resource<ExecutorService> executorResource) {
        checkArgument(!Strings.isNullOrEmpty(scheme), "scheme should not be null or empty");
        checkArgument(!Strings.isNullOrEmpty(addresses), "addresses should not be null or empty");
        checkArgument(defaultPort > 0 && defaultPort < 65536, "invalid port " + defaultPort);
        checkNotNull(timerServiceResource, "timerServiceResource should not be null");
        checkNotNull(executorResource, "executorResource should not be null");

        this.scheme = scheme;
        this.addresses = addresses;
        this.defaultPort = defaultPort;
        this.timerServiceResource = timerServiceResource;
        this.executorResource = executorResource;

        // Must prepend a "//" to the name when constructing a URI, otherwise it will be treated as an
        // opaque URI, thus the authority and host of the resulted URI would be null.
        URI nameUri = URI.create("//" + addresses);
        authority = checkNotNull(nameUri.getAuthority(),
                "nameUri (%s) doesn't have an authority", nameUri);
    }

    @Override
    public final String getServiceAuthority() {
        return authority;
    }

    @Override
    public final synchronized void start(Listener listener) {
        try {
            checkState(this.listener == null, "already started");
            timerService = SharedResourceHolder.get(timerServiceResource);
            executor = SharedResourceHolder.get(executorResource);
            this.listener = checkNotNull(listener, "listener");
            resolve();
        } catch (Exception t) {
            logger.error("fail to start name resolver", t);
            throw new GrpcNameResolverException("fail to start name resolver", t);
        }
    }

    @Override
    public final synchronized void refresh() {
        checkState(listener != null, "not started");
        resolve();
    }


    @GuardedBy("this")
    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }

    @Override
    public final synchronized void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (resolutionTask != null) {
            resolutionTask.cancel(false);
        }
        if (timerService != null) {
            timerService = SharedResourceHolder.release(timerServiceResource, timerService);
        }
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }

    private class GrpcNameResolverException extends RuntimeException {
        GrpcNameResolverException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private class ResolutionRunnable implements Runnable {

        @Override
        public void run() {
            Listener savedListener;
            synchronized (GrpcAdvancedNameResolver.this) {
                // If this task is started by refresh(), there might already be a scheduled task.
                if (resolutionTask != null) {
                    resolutionTask.cancel(false);
                    resolutionTask = null;
                }
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }

            try {
                List<GrpcAddressParser.IpPortPair> ipPortPairList;
                try {
                    ipPortPairList = GrpcAddressParser.parse(addresses, defaultPort);
                } catch (UnknownHostException e) {
                    synchronized (GrpcAdvancedNameResolver.this) {
                        if (shutdown) {
                            return;
                        }
                        // Because timerService is the single-threaded GrpcUtil.TIMER_SERVICE in production,
                        // we need to delegate the blocking work to the executor
                        resolutionTask = timerService.schedule(
                                new LogExceptionRunnable(resolutionRunnableOnExecutor), 1, TimeUnit.MINUTES);
                    }
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }
                List<EquivalentAddressGroup> servers = new ArrayList<>();
                if (ipPortPairList != null) {
                    for (GrpcAddressParser.IpPortPair ipPortPair : ipPortPairList) {
                        servers.add(new EquivalentAddressGroup(new InetSocketAddress(
                                ipPortPair.getIp(), ipPortPair.getPort())));
                    }
                }

                savedListener.onAddresses(servers, Attributes.EMPTY);
            } finally {
                synchronized (GrpcAdvancedNameResolver.this) {
                    resolving = false;
                }
            }
        }
    }
}
