/*
 * Copyright © 2017 shirenyu.Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ipv4.rev140528.KnownIpProtocols;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.trafficid.config.rev190321.TrafficidConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.TrafficIdService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TrafficIdProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficIdProvider.class);

    private final DataBroker dataBroker;
    private final NotificationProviderService notificationProviderService;
    private final TrafficidConfig trafficidConfig;

    private ConcurrentMap<Socket, Feature> socketFeatureMap = new ConcurrentHashMap<Socket,Feature>();


    private static Registration registration;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<TrafficIdService> rpcRegistration;


    public TrafficIdProvider(final DataBroker dataBroker, final NotificationProviderService notificationProviderService1, RpcProviderRegistry rpcProviderRegistry, TrafficidConfig trafficidConfig) {
        this.dataBroker = dataBroker;
        this.notificationProviderService = notificationProviderService1;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.trafficidConfig = trafficidConfig;
    }


    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        adddata();

        LOG.info("TrafficIdProvider Session Initiated");
        PacketFromIpv4 packetFromIpv4 = new PacketFromIpv4(socketFeatureMap,trafficidConfig);
        registration = notificationProviderService.registerNotificationListener(packetFromIpv4);

        RPCImpl rpcimpl = new RPCImpl(socketFeatureMap);
        rpcProviderRegistry.addRpcImplementation(TrafficIdService.class,rpcimpl);

        System.out.println(socketFeatureMap.size());





//        if(需要分类)
//            date = null;
//            packetFromIpv4(date);
//            15s后： 计算特征
//            发送http

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("TrafficIdProvider Closed");
        try {
            registration.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adddata()
    {
        Socket socket = new Socket();
        Ipv4Address ipv4Address = new Ipv4Address("10.0.0.1");
        socket.setSrcAddress(ipv4Address);
        Ipv4Address ipv4Address2 = new Ipv4Address("4.3.2.1");
        socket.setDestAddress(ipv4Address2);
        socket.setProtocol(KnownIpProtocols.Ipv6Icmp);

        socket.setSrcPort(1);
        socket.setDestPort(2);
        Feature feature = new Feature(socket,12,3);
        feature.setFlow_category("1");
        socketFeatureMap.put(socket,feature);

        Socket socket1 = new Socket();
        socket1.setSrcPort(2);
        socket1.setDestPort(2);

        Ipv4Address ipv4Address1 = new Ipv4Address("10.0.0.1");
        socket1.setSrcAddress(ipv4Address1);
        Ipv4Address ipv4Address12 = new Ipv4Address("4.3.2.4");
        socket1.setDestAddress(ipv4Address12);
        socket1.setProtocol(KnownIpProtocols.Ipv6Icmp);

        Feature feature1 = new Feature(socket1,12,3);
        feature1.setFlow_category("2");
        socketFeatureMap.put(socket1,feature1);

        Socket socket2 = new Socket();
        socket2.setSrcPort(3);
        socket2.setDestPort(2);

        Ipv4Address ipv4Address123 = new Ipv4Address("10.0.0.1");
        socket2.setSrcAddress(ipv4Address123);
        Ipv4Address ipv4Address1232 = new Ipv4Address("4.3.2.6");
        socket2.setDestAddress(ipv4Address1232);

        socket2.setProtocol(KnownIpProtocols.Igmp);

        Feature feature2 = new Feature(socket2,12,3);
        feature2.setFlow_category("4");
        socketFeatureMap.put(socket2,feature2);

        Socket socket3 = new Socket();
        socket3.setSrcPort(3);
        socket3.setDestPort(14);

        socket3.setSrcAddress(ipv4Address123);
        socket3.setDestAddress(ipv4Address1232);

        socket3.setProtocol(KnownIpProtocols.Igmp);

        Feature feature3 = new Feature(socket3,12,4);
        feature3.setFlow_category("1");
        socketFeatureMap.put(socket3,feature3);

        Socket socket4 = new Socket();
        socket4.setSrcPort(3);
        socket4.setDestPort(223);

        Ipv4Address ipv4Address1234 = new Ipv4Address("10.0.0.2");
        socket4.setSrcAddress(ipv4Address1234);
        Ipv4Address ipv4Address12324 = new Ipv4Address("4.3.2.12");
        socket4.setDestAddress(ipv4Address12324);

        socket4.setProtocol(KnownIpProtocols.Igmp);
        Feature feature4 = new Feature(socket3,12,5);

        feature4.setFlow_category("1");

        socketFeatureMap.put(socket4,feature4);

        Socket socket5 = new Socket();
        socket5.setSrcPort(3);
        socket5.setDestPort(223);

        Ipv4Address ipv4Address12345 = new Ipv4Address("10.0.0.2");
        socket5.setSrcAddress(ipv4Address12345);
        Ipv4Address ipv4Address123245 = new Ipv4Address("10.0.0.1");
        socket5.setDestAddress(ipv4Address123245);

        socket5.setProtocol(KnownIpProtocols.Igmp);
        Feature feature5 = new Feature(socket3,12,6);

        feature5.setFlow_category("1");

        socketFeatureMap.put(socket5,feature5);
    }

}