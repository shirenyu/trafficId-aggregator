/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.basepacket.rev140528.packet.chain.grp.PacketChain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.basepacket.rev140528.packet.chain.grp.packet.chain.packet.RawPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.ethernet.packet.received.packet.chain.packet.EthernetPacket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.Ipv4PacketListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.Ipv4PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.KnownIpProtocols;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.ipv4.packet.received.packet.chain.packet.Ipv4Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PacketFromIpv4 implements Ipv4PacketListener {
    private static final Logger LOG = LoggerFactory.getLogger(PacketFromIpv4.class);

//    private Map<String,Long[]> loopMap = new ConcurrentHashMap<>();
//    private Map<String, Long> echoMap = new ConcurrentHashMap<>();
//    private Map<String, Long> delayMap;

    private Map<Socket, Feature> socketFeatureMap;

    public PacketFromIpv4(Map<Socket, Feature> socketFeatureMap) {
        // TODO Auto-generated constructor stub
        this.socketFeatureMap = socketFeatureMap;
        //	if(config.getInfluxdbAddress()!=null)
        //	influxDB=InfluxDBFactory.connect(config.getInfluxdbAddress());
    }

    @Override
    public void onIpv4PacketReceived(Ipv4PacketReceived packetReceived) {
        // TODO Auto-generated method stub
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

        if (!socket.getSrcAddress().getValue().equals("10.108.124.97")&&!socket.getDestAddress().getValue().equals("10.108.124.97"))
            return ;

        Socket re_socket= socket.reverse();
        if (!socketFeatureMap.containsKey(socket) && !socketFeatureMap.containsKey(re_socket))     //Map中包含相应的流
        {
            Feature feature = new Feature(socket, ipv4Packet.getPayloadLength());
            socketFeatureMap.put(socket, feature);
        } else {
            Feature feature;
            if (socketFeatureMap.containsKey(socket))
                feature = socketFeatureMap.get(socket);
            else
                feature = socketFeatureMap.get(re_socket);
            feature.Addsocket(socket, ipv4Packet.getPayloadLength());
        }
    }
}
