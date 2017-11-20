package com.track.common.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 获取ip地址
 *
 * @author zap
 * @version 1.0, 2017/11/20
 */
@SuppressWarnings("all")
public class Addressing {

    private volatile static String LOCAL_HOST_NAME;

    /**
     * 获取主机名称
     * @return 主机名
     * @throws SocketException
     */
    public static String getHostName() throws SocketException {
        if (null == LOCAL_HOST_NAME) {
            synchronized (Addressing.class) {
                if (null == LOCAL_HOST_NAME) {
                    LOCAL_HOST_NAME = getIp4Address().getHostName();
                }
            }
        }
        return LOCAL_HOST_NAME;
    }

    // Vmware虚拟卡的名称
    private static final Pattern VMWARE_PATTERN = Pattern.compile("(VMware)", Pattern.CASE_INSENSITIVE);

    /**
     * 获取ip地址
     *
     * @return ip地址
     * @throws SocketException
     */
    public static InetAddress getIpAddress() throws SocketException {
        return getIpAddress(addr -> addr instanceof Inet4Address || addr instanceof Inet6Address);
    }

    /**
     * 获取ip地址，ipv4格式
     *
     * @return
     * @throws SocketException
     */
    public static InetAddress getIp4Address() throws SocketException {
        return getIpAddress(addr -> addr instanceof Inet4Address);
    }

    /**
     * 获取ipi，ipv6格式
     *
     * @return
     * @throws SocketException
     */
    public static InetAddress getIp6Address() throws SocketException {
        return getIpAddress(addr -> addr instanceof Inet6Address);
    }

    private static InetAddress getIpAddress(AddressSelectionCondition condition) throws
            SocketException {
        // Before we connect somewhere, we cannot be sure about what we'd be bound to; however,
        // we only connect when the message where client ID is, is long constructed. Thus,
        // just use whichever IP address we can find.
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual()
                    || isVmwareVirtual(current))
                continue;
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr.isLoopbackAddress()) continue;
                if (condition.isAcceptableAddress(addr)) {
                    return addr;
                }
            }
        }

        throw new SocketException("Can't get our ip address, interfaces are: " + interfaces);
    }

    // 是否是vmware的虚拟网卡
    private static boolean isVmwareVirtual(NetworkInterface network) {
        return VMWARE_PATTERN.matcher(network.getDisplayName()).find();
    }

    public interface AddressSelectionCondition {
        boolean isAcceptableAddress(InetAddress addr);
    }

}

