# Apache Phoenix 安装部署

标签：Apache Phoenix

---

## 简介

Apache Phoenix 是构建在 HBase 之上的一个 SQL 层，能让我们用标准的 JDBC API 而不是 HBase 的客户端 API 来对 HBase 的数据进行操作。

Apache Phoenix 使用 Java 编写，作为 HBase 内嵌的 JDBC 驱动。Phoenix 查询引擎会将 SQL 查询转换为一个或多个 HBase 扫描，并编排执行以生成标准的 JDBC 结果集。

Apache Phoenix 直接使用 HBase API、协同处理器和自定义过滤器，对于简单查询来说，性能是毫秒级的；对于百万级别的行数来说，其性能是秒级的。

## 编译

由于现在使用的是组件时 CDH5.8.2 版本，原生的 Apache Phoenix 与 CDH 版本不兼容，需要下载 phoenix-for-cloudera 的源码自行编译。

从 [这里](https://github.com/chiastic-security/phoenix-for-cloudera.git) 下载对应版本的源码。

下载源码之后，修改 pom.xml 文件中的 `cdh-root` 的版本为 5.8.2 。

在 $PHOENIX_HOME 目录下执行以下命令进行编译：
```shell
mvn clean package –DskipTests -Dcdh.flume.version=1.6.0-cdh5.8.2
```

编译完成后，在 `phoenix-assembly/target` 目录下会生成安装包 `phoenix-4.8.0-cdh5.8.0.tar.gz`。

## 部署

### 拷贝 jar 包

编译生成的安装包解压后，将 `phoenix-4.8.0-cdh5.8.0-server.jar` 拷贝到 HBase 集群中所有节点的 $HBASE_HOME/lib 目录。

### 配置

Apache Phoenix 默认会将用户创建的表（包括 Phoenix 用到的元数据表）创建在 HBase 的默认 namespace 中，如果需要将 Phoenix 中自定义的 scheme 映射到 HBase 中的 namespace 或者想让 Phoenix 的元数据表存储在单独的 namespace （默认为 SYSTEM）中，需要配置属性 `phoenix.schema.isNamespaceMappingEnabled`。

修改 $HBASE_HONE/conf/hbase-site.xml 和 $PHOENIX_HONE/bin/hbase-site.xml 配置文件，增加以下内容：
```shell
  <property>
    <name>phoenix.schema.isNamespaceMappingEnabled</name>
    <value>true</value>
  </property>
```

上述操作完成后，需要重启整个 HBase 集群。

## 测试

可以通过 Apache Phoenix 自带的 `sqlline.py` 来验证 Phoenix 是否安装成功。

执行以下命令，进入 Apache Phoenix 的命令行：
```shell
$PHOENIX_HOME/bin/sqlline.py hadoop1:2181:/hbase
```

其中，`hadoop1:2181:/hbase` 为 HBase 的 ZK 地址。

进入命令行后，执行 `!table` 命令，显示如下结果说明安装成功：
```shell
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
89/89 (100%) Done
Done
sqlline version 1.1.9
0: jdbc:phoenix:hadoop1:2181:/hbase> !table
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+--+
| TABLE_CAT  | TABLE_SCHEM  | TABLE_NAME  |  TABLE_TYPE   | REMARKS  | TYPE_NAME  | SELF_REFERENCING_COL_NAME  | REF_GENERATION  | INDEX_STATE  |  |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+--+
|            | SYSTEM       | CATALOG     | SYSTEM TABLE  |          |            |                            |                 |              |  |
|            | SYSTEM       | FUNCTION    | SYSTEM TABLE  |          |            |                            |                 |              |  |
|            | SYSTEM       | SEQUENCE    | SYSTEM TABLE  |          |            |                            |                 |              |  |
|            | SYSTEM       | STATS       | SYSTEM TABLE  |          |            |                            |                 |              |  |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+--+
0: jdbc:phoenix:hadoop1:2181:/hbase>
```