/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getallflow.output.Flowlist;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getallflow.output.FlowlistBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class RPCImpl implements TrafficIdService {

    private Map<Socket, Feature> socketFeatureMap;
    private Map<String,String> cateName = new HashMap<>();

    public RPCImpl(Map<Socket, Feature> socketFeatureMap) {
        this.socketFeatureMap = socketFeatureMap;
        cateName.put("0","Browsing");
        cateName.put("1","Email");
        cateName.put("2","Chat");
        cateName.put("3","Audio");
        cateName.put("4","Video");
        cateName.put("5","FTP");
        cateName.put("6","VoIP");
        cateName.put("7","P2P");
    }


    @Override
    public Future<RpcResult<GetALLFlowOutput>> getALLFlow() {

        GetALLFlowOutputBuilder getALLFlowOutputBuilder = new GetALLFlowOutputBuilder();
        List<Flowlist> flowlists = new ArrayList<>();
        System.out.println("data has send");

        System.out.println(socketFeatureMap.size());

        for (Socket socket:socketFeatureMap.keySet())
        {
            if(socketFeatureMap.get(socket).getFlow_category()==null)
                continue;
            FlowlistBuilder flowListBuilder = new FlowlistBuilder();
            flowListBuilder.setDstIP(socket.getDestAddress().getValue());
            flowListBuilder.setDstPort(socket.getDestPort());
            flowListBuilder.setSrcIP(socket.getSrcAddress().getValue());
            flowListBuilder.setSrcPort(socket.getSrcPort());
            flowListBuilder.setProtocol(socket.getProtocol().getName());
            String categoryKey = socketFeatureMap.get(socket).getFlow_category();
            flowListBuilder.setCategory(cateName.get(categoryKey));
            flowlists.add(flowListBuilder.build());
        }
        getALLFlowOutputBuilder.setFlowlist(flowlists);
        return RpcResultBuilder.success(getALLFlowOutputBuilder.build()).buildFuture();
    }


    //这里flowlist和getallflow Rpc 有相同的名字方便于中端解析`
    @Override
    public Future<RpcResult<GetFlowOutput>> getFlow(GetFlowInput input) {
        String srcip = input.getParent();
        String dstip = input.getId();

        GetFlowOutputBuilder getFlowOutputBuilder = new GetFlowOutputBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getflow.output.Flowlist> ipflowLists = new ArrayList<>();

        //当选中的是treeJs的根节点
        if(srcip.equals("#"))
        {
            for (Socket key : socketFeatureMap.keySet()) {
                if (socketFeatureMap.get(key).getFlow_category() == null)
                    continue;
                if (key.getSrcAddress().getValue().equals(dstip))
                {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getflow.output.FlowlistBuilder flowListBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getflow.output.FlowlistBuilder();
                    flowListBuilder.setDstPort(key.getDestPort());
                    flowListBuilder.setSrcPort(key.getSrcPort());
                    flowListBuilder.setProtocol(key.getProtocol().toString());
                    flowListBuilder.setSrcIP(key.getSrcAddress().getValue());
                    flowListBuilder.setDstIP(key.getDestAddress().getValue());
                    String categoryKey = socketFeatureMap.get(key).getFlow_category();
                    flowListBuilder.setCategory(cateName.get(categoryKey));
                    ipflowLists.add(flowListBuilder.build());
                }
            }
        }
        else
        {
            for (Socket key : socketFeatureMap.keySet()) {
                if (socketFeatureMap.get(key).getFlow_category() == null)
                    continue;
                if (key.getSrcAddress().getValue().equals(srcip)&&key.getDestAddress().getValue().equals(dstip))
                {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getflow.output.FlowlistBuilder flowListBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.trafficid.rev150105.getflow.output.FlowlistBuilder();
                    flowListBuilder.setDstPort(key.getDestPort());
                    flowListBuilder.setSrcPort(key.getSrcPort());
                    flowListBuilder.setProtocol(key.getProtocol().toString());
                    flowListBuilder.setSrcIP(key.getSrcAddress().getValue());
                    flowListBuilder.setDstIP(key.getDestAddress().getValue());
                    String categoryKey = socketFeatureMap.get(key).getFlow_category();
                    flowListBuilder.setCategory(cateName.get(categoryKey));
                    ipflowLists.add(flowListBuilder.build());
                }
            }
        }
        getFlowOutputBuilder.setFlowlist(ipflowLists);
        return  RpcResultBuilder.success(getFlowOutputBuilder.build()).buildFuture();
    }
}
