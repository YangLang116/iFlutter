package com.xtu.plugin.flutter.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HttpUtils {

    public static String getHostIp() throws UnknownHostException {
        InetAddress address = Inet4Address.getLocalHost();
        return address.getHostAddress();
    }
}
