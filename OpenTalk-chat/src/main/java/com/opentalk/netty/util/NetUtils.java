package com.opentalk.netty.util;

import com.google.common.net.InetAddresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class NetUtils {

    public static final String LOCAL_IP_ADDR = getLocalIpAddress().trim();

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("IP地址获取失败 {}", e);
        }
        return "";
    }

    public static InetSocketAddress getWsRemoteAddrFromHeader(HttpHeaders requestHeaders, Channel ch) {
        InetSocketAddress remoteAddr = (InetSocketAddress) ch.remoteAddress();
        try {
            String ipForwarded = requestHeaders.get("X-Forwarded-For");
            if (!StringUtils.isEmpty(ipForwarded)) {
                String[] ipArr = ipForwarded.split(",");
                String ip = ipArr[0];
                if ("0:0:0:0:0:0:0:1".equals(ip)) {
                    ip = "127.0.0.1";
                }
                remoteAddr = new InetSocketAddress(InetAddresses.forString(ip.trim()), remoteAddr.getPort());
            }
        } catch (Throwable e) {
            log.error("getWsRemoteAddrFromHeader error! requestHeaders={}", requestHeaders, e);
        }
        return remoteAddr;
    }
}