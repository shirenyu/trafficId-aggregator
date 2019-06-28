/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import org.bupt.trafficId.impl.util.Ipv4Option;
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.l2switch.packethandler.decoders.utils.BitBufferHelper;
import org.opendaylight.l2switch.packethandler.decoders.utils.BufferException;
import org.opendaylight.l2switch.packethandler.decoders.utils.NetUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.basepacket.rev140528.packet.chain.grp.PacketChain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.basepacket.rev140528.packet.chain.grp.packet.chain.packet.RawPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.Ipv4PacketListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.Ipv4PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.KnownIpProtocols;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.ipv4.packet.received.packet.chain.packet.Ipv4Packet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.trafficid.config.rev190321.TrafficidConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PacketFromIpv4 implements Ipv4PacketListener {
    private static final Logger LOG = LoggerFactory.getLogger(PacketFromIpv4.class);

//    private Map<String,Long[]> loopMap = new ConcurrentHashMap<>();
//    private Map<String, Long> echoMap = new ConcurrentHashMap<>();
//    private Map<String, Long> delayMap;

    private ConcurrentMap<Socket, Feature> socketFeatureMap;
    private Map<Ipv4Address,Map<Socket, Feature>> ipv4AddressMapMap;
    private List<String> dhcplist = new ArrayList<>();
    private final TrafficidConfig trafficidConfig;

    public PacketFromIpv4(ConcurrentMap<Socket, Feature> socketFeatureMap,TrafficidConfig trafficidConfig) {
        // TODO Auto-generated constructor stub
        this.socketFeatureMap = socketFeatureMap;
        this.trafficidConfig = trafficidConfig;
        //	if(config.getInfluxdbAddress()!=null)
        //	influxDB=InfluxDBFactory.connect(config.getInfluxdbAddress());
    }


//    public String getDHCPAddr(Ipv4Packet ipv4Packet,Ipv4PacketReceived packetReceived)
//    {
//        int bitOffset = ipv4Packet.getPayloadOffset() * NetUtils.NumBitsInAByte;
//
//        int[] temp = new int[]{0, 0, 0, 0};
//        for (int i = 0; i < 4; i++) {
//            try {
//                temp[i] = BitBufferHelper.getInt(BitBufferHelper.getBits(packetReceived.getPayload(), bitOffset + (24 + i) * 8, 8));
//            } catch (BufferException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return Integer.toString(temp[0]) + "." + Integer.toString(temp[1]) + "." + Integer.toString(temp[2]) + "." + Integer.toString(temp[3]);
//    }


    @Override
    public void onIpv4PacketReceived(Ipv4PacketReceived packetReceived) {
        // TODO Auto-generated method stub
        if (!trafficidConfig.isIsActive())
            return;
        if (packetReceived == null || packetReceived.getPacketChain() == null) {
            return;
        }
        RawPacket rawPacket = null;
        EthernetPacket ethernetPacket = null;
        Ipv4Packet ipv4Packet = null;
        for (PacketChain packetChain : packetReceived.getPacketChain()) {
            if (packetChain.getPacket() instanceof RawPacket) {
                rawPacket = (RawPacket) packetChain.getPacket();
            } else if (packetChain.getPacket() instanceof EthernetPacket) {
                ethernetPacket = (EthernetPacket) packetChain.getPacket();
            } else if (packetChain.getPacket() instanceof Ipv4Packet) {
                ipv4Packet = (Ipv4Packet) packetChain.getPacket();
            }
        }
        if (rawPacket == null || ethernetPacket == null || ipv4Packet == null) {
            return;
        }
        if (!(ipv4Packet.getProtocol()== KnownIpProtocols.Tcp) && !(ipv4Packet.getProtocol()==KnownIpProtocols.Udp))
            return;
        Socket socket = Socket.getSocket(ethernetPacket, ipv4Packet, packetReceived);


//        仅统计dhcp发现的主机相应的ip地址
//        if(socket.getSrcPort()==67)
//        {String DHCPaddr = getDHCPAddr(ipv4Packet, packetReceived);
//            dhcplist.add(DHCPaddr);
//        }
//
//        if (!dhcplist.contains(socket.getSrcAddress().getValue())&&!dhcplist.contains(socket.getDestAddress().getValue()))
//            return ;

//        //提取ovs打入的时间戳
//        Ipv4Option ipv4Option=new Ipv4Option();
//        Long time = 0L;
//        try {
//            ipv4Option=(Ipv4Option)ipv4Option.deserialize(ipv4Packet.getIpv4Options(),0,0);
//            long time1=ipv4Option.getSsecond();
//            long time2=ipv4Option.getSNanosecond();
//            time = time1*1000000000+time2;
//        } catch (PacketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }


        // 由于修改ovs代码对延迟插件有影响，故该版本time按系统时间获得
        long time = System.nanoTime();




//        if (!trafficidConfig.getMonitorIp().equals(socket.getSrcAddress().getValue()) && !trafficidConfig.getMonitorIp().equals(socket.getDestAddress().getValue()))
//            return;

        Socket re_socket= socket.reverse();

        if (!socketFeatureMap.containsKey(socket) && !socketFeatureMap.containsKey(re_socket))     //Map中包含相应的流
        {
            Feature feature = new Feature(socket, ipv4Packet.getIpv4Length(),time);
            socketFeatureMap.put(socket, feature);
        } else {
            Feature feature;
            if (socketFeatureMap.containsKey(socket))
                feature = socketFeatureMap.get(socket);
            else
                feature = socketFeatureMap.get(re_socket);
            feature.Addsocket(socket, ipv4Packet.getIpv4Length(),time);
        }
    }
}
