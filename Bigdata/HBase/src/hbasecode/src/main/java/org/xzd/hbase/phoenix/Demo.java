package org.xzd.hbase.phoenix;

import java.sql.*;
import java.util.Properties;
import java.util.Random;

/**
 * Created by xuzd on 2017/12/11.
 */
public class Demo {
    /**
     *
     * @param url 如果有多个ZK IP，则url的格式为：jdbc:phoenix:192.168.3.151,192.168.3.152,192.168.3.153;2181;/hbase
     * @return
     * @throws ClassNotFoundException
     */
    private static Connection getConn(String url) throws ClassNotFoundException {
        String driver = "org.apache.phoenix.jdbc.PhoenixDriver";

        Class.forName(driver);

        Properties properties = new Properties();
        properties.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static void main(String[] args) {
        String url = "jdbc:phoenix:192.168.3.151,192.168.3.152,192.168.3.153;2181;/hbase";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConn(url);
            //如果要批量提交，需要禁止自动提交功能
            connection.setAutoCommit(false);

            //指定表名的时候需要注意，Phoenix中是严格区分大小写的，所以需要跟Phoenix中保持一致，如果Phoenix
            //中的表名为小写，这里必须要用双引号包括表名
            String sql = "upsert into \"xuzd.test_split\" (id, name, age, salary) values (?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);

            int batchSize = 0;
            //设置一次提交的数量，超过该数量后，执行实际的提交操作
            int commitSize = 1000;
            for (int j = 0; j < 10; j++) {
                Random random = new Random(34);

                for (int i = j * 100000; i < j * 100000 + 10000; i++) {
                    preparedStatement.setInt(1, i);
                    preparedStatement.setString(2, "xuzd");
                    preparedStatement.setInt(3, random.nextInt(100));
                    preparedStatement.setInt(4, random.nextInt(20000));
                    preparedStatement.executeUpdate();
                    batchSize++;

                    if (batchSize % commitSize == 0) {
                        //在这里才执行实际的提交操作
                        connection.commit();
                    }
                }
            }

            connection.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
