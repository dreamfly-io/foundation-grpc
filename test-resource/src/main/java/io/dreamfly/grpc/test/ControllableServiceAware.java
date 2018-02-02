package io.dreamfly.grpc.test;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ControllableServiceAware {
    private static Logger logger = LoggerFactory.getLogger(ControllableServiceAware.class);
    protected final int[] ports;
    protected List<Server> servers = new ArrayList<>();

    public ControllableServiceAware(int port) {
        checkPort(port);
        this.ports = new int[]{port};
    }

    public ControllableServiceAware(int[] ports) {
        checkArgument(ports.length > 0, "please set at least one port");
        for(int port : ports) {
            checkPort(port);
        }

        this.ports = ports;
    }

    private void checkPort(int port) {
        checkArgument(port > 0 && port < 65536, "invalid port " + port);
    }

    @BeforeAll
    public void startControllableServers() {
        for(int port : ports) {
            logger.debug("begin to start Controllable Server on port " + port);
            try {
                Server server = ServerBuilder.forPort(port)
                        .addService(new ControllableServiceImpl())
                        .build()
                        .start();
                servers.add(server);
                logger.info("Success to start Controllable Server on port " + port);
            } catch (IOException e) {
                throw new RuntimeException("Fail to start Controllable Server on port " + port, e);
            }
        }

        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopControllableServers();
        }));
    }

    @AfterAll
    public void stopControllableServers() {
        if (servers.size() == 0) {
            return;
        }

        for(Server server : servers) {
            final int port = server.getPort();
            logger.debug("begin to stop Controllable Server on port " + port);
            try {
                server.shutdownNow();
                server.awaitTermination(10, TimeUnit.SECONDS);
                logger.info("Success to stop Controllable Server on port " + port);
            } catch (Exception e) {
                logger.error("Fail to stop Controllable Server on port " + port, e);
            }
        }

        servers.clear();
    }
}