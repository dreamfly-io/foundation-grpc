package io.dreamfly.grpc.test;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ControllableServiceAwareTest extends ControllableServiceAware {
    private static final int PORT = 10011;

    public ControllableServiceAwareTest() {
        super(PORT);
    }

    @Test
    public void trigger() {
        assertThat(this.servers.size()).isEqualTo(1);
        assertThat(this.servers.get(0)).isNotNull();
        assertThat(this.servers.get(0).getPort()).isEqualTo(PORT);
    }
}