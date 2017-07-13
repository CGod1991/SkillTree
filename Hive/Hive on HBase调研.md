# Hive On HBase 调研

标签：Hive HBase

---

## 背景

目前有非常多的用户个性化推荐需求,最基础的部分就是给用户打标签,这些标签目前是在 Hive 上离线计算的,然后搜索的同事通过程序去解析 HDFS 对应目录下的文件，生成一个xml文件,方便加载到solr里。

按目前学科的个性化推荐需求来说,生成的文件有 27G,解析成 xml 需要 1 个小时,加载到 solr 需要一小时。流程比较繁琐,效率比较低。

现在希望通过搭建一套 Hive On HBase 的环境, 在 HBase 上建一张空表, 在 Hive 上建立好 HBase 表与 Hive 表字段的对应关系, 后续搜索能直接通过 HBase 访问用户标签的数据。

## 解决方案

Hive 本身支持将 Hive 中的表与 HBase 中的表进行关联，主要是通过存储处理程序来实现。

存储处理程序是一个结合 InputFormat、OutputFormat、SerDe 和 Hive 需要使用的特定代码，来讲外部实体作为标准的 Hive 表进行处理的整体。

无论表是以文本文件的方式存储在 Hadoop 中，还是以列族的方式存储在如 Apache HBase、Apache Cassandra 或 Amazon DynamoDB 这样的 NoSQL 数据库中，用户都可以通过存储处理器无缝的直接执行查询，解决问题。

对于和 HBase 的整合，Hive 是通过存储处理器（也就是 hive-hbase-handler-1.1.0-cdh5.8.2.jar）来操作的。简单的说，基本原理是：当 Hive 将用户的指令解析编译之后，具体执行时调用 HBase 的客户端接口，来对 HBase 中的表进行相应的操作。

具体的解决思路是：首先在 HBase 中创建一个表，然后在 Hive 中创建一个表，将其与 HBase 中的表关联起来，这样后续向 Hive 中的表中加载数据时，数据会被实时的更新到 HBase 的表中。而且，Hive 中并不会保存实际的数据，类似于外部表，所有数据都是保存在 HBase 中，在 Hive 中删除表也不会删除 HBase 中的表。

## 具体配置

1、拷贝 jar 包：将 $HBASE_HOME/lib/protobuf-java-2.5.0.jar 拷贝到 $HIVE_HOME/lib 下。
2、将 $HBASE_HOME/conf/hbase-site.xml 拷贝到 Hadoop 集群中所有节点的 $HADOOP_HOME/etc/hadoop 目录下。
3、修改配置文件 $HIVE_HOME/conf/hive-site.xml，增加以下内容($HIVE_HOME 替换成实际的路径)：

```shell
<property>
  <name>hive.aux.jars.path</name>
  <value>$HIVE_HOME/lib/hbase-protocol-1.2.0-cdh5.8.2.jar,$HIVE_HOME/lib/guava-14.0.1.jar,$HIVE_HOME/lib/hbase-common-1.2.0-cdh5.8.2.jar,$HIVE_HOME/lib/hive-hbase-handler-1.1.0-cdh5.8.2.jar,$HIVE_HOME/lib/zookeeper-3.4.5-cdh5.8.2.jar,$HIVE_HOME/lib/protobuf-java-2.5.0.jar</value>
</property>
<property>
  <name>hbase.zookeeper.quorum</name>
  <value>cdh3-151,cdh3-152,cdh3-153</value>
</property>

```

4、在 HBase 中新建 namespace：`create_namespace 'hive'`。
5、在 HBase 给新建的 namespace 授予所有权限：`grant 'hive' , 'RWXCA', '@hive'`。
6、在 HBase 中创建表：`create 'hive:hive_on_hbase', 'info', 'content'`。
7、在 Hive 中创建对应的表，并与 HBase 中的表关联起来：
```shell
create external table hive_hbase (key int, name string, text string) 
stored by  'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
with serdeproperties ("hbase.columns.mapping" = ":key, info:name, content:text")
tblproperties ("hbase.table.name" = "hive:hive_on_hbase");
```

对于 Hive 中的建表语句，需要注意的是如何将 Hive 表中的字段与 HBase 表中的字段对应起来。

对于上面的建表语句来说，Hive 表中的 key 字段映射到了 HBase 中的 Row Key，这个映射关系是必须要有的。name 和 text 字段分别对应 HBase 表中的 info:name 和 context:text 列。

此时，Hive 中的 hive_hbase 表就和 HBase 中的 hive:hive_on_hbase 表对应起来了，后续在 Hive 中向 hive_hbase 表中插入的数据都会实时更新到 HBase 中的 hive:hive_on_hbase 表中。
