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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Feature {

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


    public Feature(Socket socket, long payloadLength) {
        start_time = System.nanoTime();
        f_socket = socket;
        forp_psec += 1;
        forb_psec += payloadLength;
        for_time.add(start_time);
        twoway_time.add(start_time);
    }

    public void Addsocket(Socket socket, long payloadLength) {
        long arr_time = System.nanoTime();
        if ((arr_time - start_time)/1000000000 < 15)
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
                    String flow_class = sendPost(features);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private double[] getTimefeature(List<Long> list)
    {
        long min,max,avg,stdev,term;

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

    private String sendPost(double[] features) throws Exception {
        String ADD_URL = "http://localhost:9090/";
        try {
            //创建连接
            URL url = new URL(ADD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.connect();
            //POST请求
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            //double [] s = {0.185057126078,0.00645419651858,0.481939760647,0.283092759311,0.18505331384,0.00644995074112,0.481938563054,0.290628159183,0.181845218205,7.77736795561e-06,0.481938954245,0.287651977725,0.00115449149874,4.15050485232e-05,0.00102669357802,0.000873092663746,5.99918280016e-05,9.93023420161e-09,0.481938954245};
            JSONArray jsonArray=new JSONArray();
            for(int i = 0 ; i < features.length ;i++){  //依次将数组元素添加进JSONArray对象中
                jsonArray.put(features[i]);
            }
            out.write(jsonArray.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            System.out.println(sb);
            reader.close();
            // 断开连接
            connection.disconnect();
            return sb.toString();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "failed";
    }
}
