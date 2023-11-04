package com.czp.utils;

import com.czp.exception.NetWorkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class NetUtils {

    public static String getIp(){
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            String localHostAddress = "";
            while(allNetInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = allNetInterfaces.nextElement();
                if (networkInterface.isLoopback()||networkInterface.isVirtual()||!networkInterface.isUp()){
                    continue;
                }
                Enumeration<InetAddress> address = networkInterface.getInetAddresses();
                while(address.hasMoreElements()){
                    InetAddress inetAddress = address.nextElement();
                    if(inetAddress != null && inetAddress instanceof Inet4Address){
                        localHostAddress = inetAddress.getHostAddress();
                    }
                }
            }
            return localHostAddress;
        }catch (SocketException ex){
            ex.printStackTrace();
            log.error("获取局域网ip时发生异常",ex);
            throw new NetWorkException();
        }
    }


}
