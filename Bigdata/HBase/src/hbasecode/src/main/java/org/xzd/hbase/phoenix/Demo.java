package org.xzd.hbase.phoenix;

import java.sql.*;
import java.util.Properties;
import java.util.Random;

/**
 * Created by xuzd on 2017/12/11.
 */
public class Demo {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String driver = "org.apache.phoenix.jdbc.PhoenixDriver";
        String url = "jdbc:phoenix:192.168.3.151,192.168.3.152,192.168.3.153;2181;/hbase";

        Class.forName(driver);

        Properties properties = new Properties();
        properties.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");

        DriverManager.getConnection(url, properties);
        Connection connection = DriverManager.getConnection(url);
        connection.setAutoCommit(false);

        String sql = "upsert into \"xuzd.test_split\" (id, name, age, salary) values (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        int batchSize = 0;
        int commitSize = 1000;
        for (int j = 0; j < 20; j++) {
            Random random = new Random(34);

            for (int i = j * 100000; i < j * 100000 + 10000; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "xuzd");
                preparedStatement.setInt(3, random.nextInt(100));
                preparedStatement.setInt(4, random.nextInt(20000));
                preparedStatement.executeUpdate();
                batchSize++;

                if (batchSize % commitSize == 0) {
                    long start = System.currentTimeMillis();
                    connection.commit();
                    System.out.println("commit cost:" + (System.currentTimeMillis() - start));
                }

            }
        }

        connection.commit();

        preparedStatement.close();
        connection.close();
    }

}
