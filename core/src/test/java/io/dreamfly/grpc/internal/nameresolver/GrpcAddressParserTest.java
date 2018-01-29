package io.dreamfly.grpc.internal.nameresolver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcAddressParserTest {

    private int defaultPort = 2379;

    @Test
    public void parse_ipWithPort() throws Exception {
        final String input = "127.0.0.1:1111";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);
        assertThat(ipPortPairList.size()).isEqualTo(1);
        GrpcAddressParser.IpPortPair ipPortPair = ipPortPairList.get(0);
        assertThat(ipPortPair.getIp()).isEqualTo("127.0.0.1");
        assertThat(ipPortPair.getPort()).isEqualTo(1111);
    }


    @Test
    public void parse_ipWithoutPort() throws Exception {
        final String input = "127.0.0.1";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);
        assertThat(ipPortPairList.size()).isEqualTo(1);
        GrpcAddressParser.IpPortPair ipPortPair = ipPortPairList.get(0);
        assertThat(ipPortPair.getIp()).isEqualTo("127.0.0.1");
        assertThat(ipPortPair.getPort()).isEqualTo(defaultPort);
    }

    @Test
    public void parse_hostWithPort() throws Exception {
        final String input = "www.baidu.com:1111";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);

        System.out.println("input = " + input);
        for(GrpcAddressParser.IpPortPair ipPortPair : ipPortPairList) {
            System.out.println("parsed result = " + ipPortPair.getIp() + ":" + ipPortPair.getPort());
        }

        assertThat(ipPortPairList.size()).isGreaterThan(1);
        GrpcAddressParser.IpPortPair ipPortPair = ipPortPairList.get(0);
        assertThat(ipPortPair.getPort()).isEqualTo(1111);
    }

    @Test
    public void parse_hostWithoutPort() throws Exception {
        final String input = "www.baidu.com";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);

        System.out.println("input = " + input);
        for(GrpcAddressParser.IpPortPair ipPortPair : ipPortPairList) {
            System.out.println("parsed result = " + ipPortPair.getIp() + ":" + ipPortPair.getPort());
        }

        assertThat(ipPortPairList.size()).isGreaterThan(1);
        GrpcAddressParser.IpPortPair ipPortPair = ipPortPairList.get(0);
        assertThat(ipPortPair.getPort()).isEqualTo(defaultPort);
    }

    @Test
    public void parse_mix() throws Exception {
        final String input = "www.baidu.com:22222, 127.0.0.1:1111, www.baidu.com, 127.0.0.1";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);
        assertThat(ipPortPairList.size()).isGreaterThan(1);
        System.out.println("input = " + input);
        for(GrpcAddressParser.IpPortPair ipPortPair : ipPortPairList) {
            System.out.println("parsed result = " + ipPortPair.getIp() + ":" + ipPortPair.getPort());
        }
    }

    @Test
    public void parse_wrong_emptyPort() throws Exception {
        final String input = "127.0.0.1:";
        List<GrpcAddressParser.IpPortPair> ipPortPairList = GrpcAddressParser.parse(input, defaultPort);
        assertThat(ipPortPairList.size()).isEqualTo(1);
        GrpcAddressParser.IpPortPair ipPortPair = ipPortPairList.get(0);
        assertThat(ipPortPair.getIp()).isEqualTo("127.0.0.1");
        assertThat(ipPortPair.getPort()).isEqualTo(defaultPort);
    }

    @Test
    public void parse_wrong_invalidPort() throws Exception {
        final String input = "127.0.0.1:sdf";
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GrpcAddressParser.parse(input, defaultPort));
    }

    @Test
    public void parse_wrong_invalidHost() throws Exception {
        final String input = "invalid_host:123";
        Assertions.assertThrows(UnknownHostException.class,
                () -> GrpcAddressParser.parse(input, defaultPort));
    }

    @Test
    public void parseIpFromHost_hostname() throws Exception {
        final String input = "www.baidu.com";
        List<String> ipList = GrpcAddressParser.parseIpFromHost(input);
        assertThat(ipList.size()).isGreaterThan(1);
        for(String ip : ipList) {
            System.out.println(input + " is parsed to ip：" + ip);
        }
    }

    @Test
    public void parseIpFromHost_ip() throws Exception {
        final String input = "14.215.177.37";
        List<String> ipList = GrpcAddressParser.parseIpFromHost(input);
        assertThat(ipList.size()).isEqualTo(1);
        assertThat(input).isEqualTo(ipList.get(0));
    }
}