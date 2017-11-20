package org.xzd.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.mapreduce.Job;

import java.nio.charset.Charset;

/**
 * Created by xuzd on 2017/11/14.
 */
public class Scan {
    public static void main(String[] args) {
//        Configuration configuration = HBaseConfiguration.create();
//        Job job = new Job(configuration, "")

        String test = "hello";
        for (byte b : test.getBytes(Charset.forName("UTF-8"))) {
            System.out.println(b);
        }
    }
}
