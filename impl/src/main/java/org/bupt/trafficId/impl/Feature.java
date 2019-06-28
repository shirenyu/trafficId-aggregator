/*
 * Copyright © 2017 shirenyu.Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.trafficId.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Feature {

    private static final Logger LOG = LoggerFactory.getLogger(Feature.class);

    private double forp_psec = 0.0;   //每秒的前向流包数
    private double forb_psec = 0.0;   //每秒的前向流字节数
    private double backp_psec = 0.0;  //每秒的后向流包数
    private double backb_psec = 0.0;  //每秒的后向流字节数
//    private double ratep_psec = 0.0;  //每秒前向/后向包数的比
//    private double rateb_psec = 0.0;  //每秒前向/后向字节数的比
//    private double duration = 0.0;    //流持续时间

    private final double interval = 15.0;
    private long start_time;
    private Socket f_socket;
    private Boolean hasgetfeature = false;

    private List<Long> for_time = new ArrayList<>();    //前向包时间
    private List<Long> back_time = new ArrayList<>();   //后向包时间
    private List<Long> twoway_time = new ArrayList<>(); //双向包时间

    private String flow_category;

    public String getFlow_category() {
        return flow_category;
    }

    public void setFlow_category(String flow_category) {
        this.flow_category = flow_category;
    }

    public Feature(Socket socket, long payloadLength ,long time) {

        if(time == 0L) {
            System.out.println("ovs didn't paste timestamp");
            return;
        }
        start_time = time;
        f_socket = socket;
        forp_psec += 1;
        forb_psec += payloadLength;
        for_time.add(start_time);
        twoway_time.add(start_time);
    }

    public void Addsocket(Socket socket, long payloadLength ,long arr_time) {
        if(arr_time == 0L) {
            System.out.println("ovs didn't paste timestamp");
            return;
        }

        if ((arr_time - start_time)/1000000000.0 < 15)
        {
            if (f_socket.equals(socket))
            {
                forp_psec += 1;
                forb_psec += payloadLength;
                for_time.add(arr_time);
                twoway_time.add(arr_time);
            }
            else
            {
                backp_psec += 1;
                backb_psec += payloadLength;
                back_time.add(arr_time);
                twoway_time.add(arr_time);
            }
        }
        else
        {
            if (!hasgetfeature)
            {
                hasgetfeature = true;
                double[] features = getfeature();
                try {
                    flow_category = SocketClient(features);
                    LOG.info("src ip:"+f_socket.getSrcAddress().toString()+"dest"+f_socket.getDestAddress().toString()+"class: "+flow_category);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private double[] getTimefeature(List<Long> list)
    {
        long min,max,avg,stdev,term;
        Collections.sort(list);

        if (list.size()>=2)
        {
            min = list.get(1) - list.get(0);
            max = list.get(1) - list.get(0);
            long sum = 0;
            for (int i=2;i<list.size();i++)
            {
                long time_d = list.get(i) - list.get(i-1);
                if (min > time_d)
                    min = time_d;
                if (max < time_d)
                    max = time_d;
                sum += time_d;
            }
            avg = sum / (list.size()-1);
            stdev = 0;
            for (int i=1;i<list.size();i++)     //标准差
            {
                long time_d = list.get(i) - list.get(i-1);
                stdev += Math.pow(time_d-avg,2);
            }
            stdev = (long) Math.pow(stdev/(list.size()-1),0.5);
        }
        else
        {
            min = max = avg = stdev = 0;
        }
        double re_list[] = new double[4];
        re_list[0] = (Math.round(avg/1000)/1000000.0);
        re_list[1] = (Math.round(min/1000)/1000000.0);
        re_list[2] = (Math.round(max/1000)/1000000.0);
        re_list[3] = (Math.round(stdev/1000)/1000000.0);
        return  re_list;
    }


    private double[] getfeature()
    {
        double duration = twoway_time.get(twoway_time.size()-1) - twoway_time.get(0);
        double ratep_psec = backp_psec / forp_psec;     //每秒前向/后向包数的比
        double rateb_psec = backb_psec / forb_psec;     //每秒前向/后向字节数的比
        forp_psec = forp_psec / interval;    //求每秒下的值
        forb_psec = forb_psec / interval;
        backp_psec = backp_psec / interval;
        backb_psec = backb_psec / interval;

        double[] re_feature = new double[19];
        System.arraycopy(getTimefeature(for_time),0,re_feature,0,4);
        System.arraycopy(getTimefeature(back_time),0,re_feature,4,4);
        System.arraycopy(getTimefeature(twoway_time),0,re_feature,8,4);
        re_feature[12] = (Math.round(forp_psec*1000000)/1000000.0);       //保留小数点后6位
        re_feature[13] = (Math.round(forb_psec*1000000)/1000000.0);
        re_feature[14] = (Math.round(backp_psec*1000000)/1000000.0);
        re_feature[15] = (Math.round(backb_psec*1000000)/1000000.0);
        re_feature[16] = (Math.round(ratep_psec*1000000)/1000000.0);
        re_feature[17] = (Math.round(rateb_psec*1000000)/1000000.0);
        re_feature[18] = (Math.round(duration/1000)/1000000.0);
        return re_feature;
    }


    public String SocketClient (double[] features)
    {
        try {
            java.net.Socket socket = new java.net.Socket("10.108.126.64", 9991);
            OutputStream os=socket.getOutputStream();//字节输出流
            PrintWriter pw=new PrintWriter(os);//将输出流包装为打印流

            JSONArray jsonArray = new JSONArray(features);

            pw.write(jsonArray.toString());
            pw.flush();
            socket.shutdownOutput();//关闭输出流

            InputStream is=socket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String info=null;
//            while((info=in.readLine())!=null){
//                System.out.println("获得path："+info);
//            }
            info = in.readLine();
            is.close();
            in.close();
            socket.close();
            if (info!=null)
                return info;
            else
                return "err";

        } catch (IOException e) {
            e.printStackTrace();
            return "err";
        }
    }

}
