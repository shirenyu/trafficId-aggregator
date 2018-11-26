/*
 * Copyright © 2017 shirenyu.Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrafficIdProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficIdProvider.class);

    private final DataBroker dataBroker;
    private final NotificationProviderService notificationProviderService;



    private static Registration registration;

    public TrafficIdProvider(final DataBroker dataBroker,  final NotificationProviderService notificationProviderService1) {
        this.dataBroker = dataBroker;
        this.notificationProviderService = notificationProviderService1;
    }


    private static Map<Socket, Feature> socketFeatureMap= new ConcurrentHashMap<>();
    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("TrafficIdProvider Session Initiated");
        PacketFromIpv4 packetFromIpv4 = new PacketFromIpv4(socketFeatureMap);
        registration = notificationProviderService.registerNotificationListener(packetFromIpv4);



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
}