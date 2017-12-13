# Apache Phoenix 操作

标签：Apache Phoenix

---

版本：phoenix-4.8.0-cdh5.8.0

## ZooKeeper URL 格式

Phoenix 提供了类似 JDBC 的连接方式来获取 HBase 集群的信息，如果需要使用多个 ZK 的地址，则 URL 的具体格式如下：
> `jdbc:phoenix:192.168.3.151,192.168.3.152,192.168.3.153;2181;/hbase`

## 基本操作

建表：
>  create table "xuzd.test_salt" (id integer primary key, name varchar(20), age integer, salary integer) salt_buckets = 3;
>  create table "xuzd.test_salt" (id integer primary key, name varchar(20), age integer, salary integer) split on ('a', 'b', 'c');

删表：
> DROP TABLE "xuzd.phoenix";

增加列：
> ALTER TABLE "xuzd.phoenix" ADD age integer;

新增：
> upsert into "xuzd.phoenix" (ID, "name") values (1, 'xuzd');

更新：
> UPSERT INTO "xuzd.phoenix" (ID, "name") values (1, 'abc');

查询：
> select * from "xuzd.phoenix" where age = 12878;

## 预分区

Phoenix 可以在建表时创建预分区，语法如下：
> CREATE TABLE TEST (HOST VARCHAR NOT NULL PRIMARY KEY, DESCRIPTION VARCHAR) SPLIT ON ('CS','EU','NA')

## 索引

Phoenix 会在查询时自动判断是否使用全局索引或本地索引。

想要使用索引，需要在 HBase 集群中的 hbase-site.xml 中增加以下属相：
```shell
<property>
	<name>hbase.regionserver.wal.codec</name>
	<value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value>
</property>
```

### Local Indexes

本地索引适用于写多读少，且空间有限的场景。

为避免进行写操作带来的网络开销，本地索引数据和数据表是放在同一个服务器上。因为所有的本地索引都单独存储在同一张共享表中由于无法预先确定 region 的位置，因此在读取数据的时候会检查所有的 region 从而带来一定的性能开销。

创建本地索引：
> CREATE LOCAL INDEX MYINDEX ON CSVTABLES(USERID);

### Global Indexes

全局索引适用于读多写少的场景，在写操作上会给性能带来很大开销，因为所有的更新和写操作都会带来索引的更新。

对于全局索引，如果查询字段或条件字段包括索引之外的字段，则会进行全表扫面，所以会导致性能很差。

如果想要使用全局索引，需要在所有 Region Server 上的 hbase-site.xml 中增加如下属性：
```shell

<property>
  <name>hbase.region.server.rpc.scheduler.factory.class</name>
  <value>org.apache.hadoop.hbase.ipc.PhoenixRpcSchedulerFactory</value>
  <description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
</property>
<property>
  <name>hbase.rpc.controllerfactory.class</name>
  <value>org.apache.hadoop.hbase.ipc.controller.ServerRpcControllerFactory</value>
  <description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
</property>
```

创建全局索引：
> CREATE INDEX USERIDINDEX ON CSVTABLES(USERID) INCLUDE（SALARY）;









