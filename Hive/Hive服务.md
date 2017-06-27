# Hive 服务

标签：Hive

---

## cli

Command Line Interface 的缩写，是 Hive 的命令行界面。

使用 cli 不需要启动 hiveserver2 和 metastore 服务。

## hiveserver

可以作为 Thrift Server 来运行，能够和不同语言的客户端进行通信，不通过 cli 来对 Hive 数据进行操作。

缺点是无法处理多于一个客户端的并发请求。

## hiveserver2

实现的功能与 hiveserver 类似，优点在于支持多客户端的并发请求，同时为开放 API 如 JDBC、ODBC 提供了更好的支持。

## metastore

Hive 的元数据服务，主要负责在关系型数据库中管理表、分区的元数据，同时提供 API 供客户端访问这些信息。

