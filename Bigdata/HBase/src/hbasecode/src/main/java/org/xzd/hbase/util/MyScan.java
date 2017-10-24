package org.xzd.hbase.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by xuzd on 2017/10/13.
 */
public class MyScan {
    public static void main(String[] args) throws IOException {
        System.setProperty("hadoop.home.dir", "F:\\tmp\\hadoop-2.6.0-cdh5.8.2");

        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "hadoop1:2181");
        conf.set("hbase.master", "hadoop1:60010");

        Connection conn = ConnectionFactory.createConnection(conf);
        Table table = conn.getTable(TableName.valueOf("Person"));

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setMaxResultSize(10);
        ResultScanner scanner = table.getScanner(scan);
        FileWriter out = new FileWriter(new File("F:\\tmp\\out.txt"));
        for (Result result : scanner) {
            for (Cell cell : result.rawCells()) {
                out.write(new String(cell.getValueArray()));
                out.write("\n");
            }
        }

        out.flush();
        out.close();
    }
}
