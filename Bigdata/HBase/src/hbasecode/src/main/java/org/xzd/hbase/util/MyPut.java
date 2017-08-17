package org.xzd.hbase.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuzd on 2017/8/17.
 */
public class MyPut {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "hadoop1:2181");
        conf.set("hbase.master", "hadoop1:60010");

        Connection conn = ConnectionFactory.createConnection(conf);
        Table table = conn.getTable(TableName.valueOf("test:person"));

        long startTime = System.currentTimeMillis();
        List<Put> list = new ArrayList<Put>();
        for (int i = 9000000; i < 9500000; i++) {
            Put put = new Put(Bytes.toBytes(i));
            put.addColumn(Bytes.toBytes("name"), Bytes.toBytes("test"), Bytes.toBytes("zhangsan"));
            list.add(put);
        }

        table.put(list);
        System.out.println("count time:" + (System.currentTimeMillis() - startTime) + " ms");
    }
}
