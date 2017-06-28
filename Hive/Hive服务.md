# Hive 服务

标签：Hive

---

## cli

Command Line Interface 的缩写，是 Hive 的命令行界面。

cli 是胖客户端，需要本地具有所有的 Hive 组件，包括配置，同时还需要一个 Hadoop 客户端极其配置。

使用 cli 不需要启动 hiveserver2 和 metastore 服务。

## hiveserver

可以作为 Thrift Server 来运行，能够和不同语言的客户端进行通信，不通过 cli 来对 Hive 数据进行操作。

缺点是无法处理多于一个客户端的并发请求。

## hiveserver2

实现的功能与 hiveserver 类似，优点在于支持多客户端的并发请求，同时为开放 API 如 JDBC、ODBC 提供了更好的支持。

## metastore

可选的 ThriftMetastore 组件，当 Hive 客户端连接到 ThriftMetastore 组件时，会和 JDBCMetastore 进行通信，进而获取元数据信息。

通常，只有非 Java 客户端需要获取元数据存储信息时才会使用该组件。

