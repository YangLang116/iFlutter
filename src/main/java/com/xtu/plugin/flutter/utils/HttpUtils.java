package com.xtu.plugin.flutter.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class HttpUtils {

    private static Set<String> getLocalIpAddress() {
        Set<String> ipList = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> ipAddressEnum = ni.getInetAddresses();
                while (ipAddressEnum.hasMoreElements()) {
                    InetAddress address = ipAddressEnum.nextElement();
                    if (address.isLoopbackAddress()) continue;
                    String ip = address.getHostAddress();
                    if (ip.contains(":")) continue;
                    ipList.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipList;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static String getLocalIP() {
        Set<String> localIpAddress = getLocalIpAddress();
        if (CollectionUtils.isEmpty(localIpAddress)) return "localhost";
        for (String ipAddress : localIpAddress) {
            if (ipAddress.startsWith("192.168")) return ipAddress;
        }
        return localIpAddress.stream().findFirst().get();
    }
}
