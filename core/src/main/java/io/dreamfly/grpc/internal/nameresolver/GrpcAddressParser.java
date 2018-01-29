package io.dreamfly.grpc.internal.nameresolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to parse gRPC address.
 */
public class GrpcAddressParser {

    public static class IpPortPair {
        private final String ip;
        private final int port;

        IpPortPair(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        String getIp() {
            return ip;
        }

        int getPort() {
            return port;
        }
    }

    private GrpcAddressParser() {
        // no instance
    }

    static List<IpPortPair> parse(String addressContent, int defaultPort) throws UnknownHostException {
        String[] addressArray = addressContent.split(",");
        List<IpPortPair> resolvedAddress = new ArrayList<>(addressArray.length);
        for (String address : addressArray) {
            resolvedAddress.addAll(parseOneAddress(address, defaultPort));
        }
        return resolvedAddress;
    }

    private static List<IpPortPair> parseOneAddress(String address, int defaultPort) throws UnknownHostException {
        String[] addressPortPair = address.split(":");
        final String host = addressPortPair[0].trim();
        final int port = parsePort(addressPortPair, defaultPort);

        List<IpPortPair> ipPortPair = new ArrayList<>();
        List<String> ipList = parseIpFromHost(host);
        for (String ip : ipList) {
            ipPortPair.add(new IpPortPair(ip, port));
        }

        return ipPortPair;
    }

    static List<String> parseIpFromHost(String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        List<String> ipList = new ArrayList<>(addresses.length);
        for (InetAddress inetAddress : addresses) {
            ipList.add(inetAddress.getHostAddress());
        }
        return ipList;
    }

    private static int parsePort(String[] addressPortPair, int defaultPort) {
        int port = defaultPort;
        if (addressPortPair.length > 1) {
            try {
                port = Integer.valueOf(addressPortPair[1].trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid port in address: port=" + addressPortPair[1], e);
            }
        }
        return port;
    }
}
