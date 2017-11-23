# HBase Thrift

标签：HBase Thrift

---

## 概述


## 启动参数

启动 `thrift` 的参数如下：
```shell
These are the command line arguments the Thrift server understands in addition to start and stop:
-b, --bind
Address to bind the Thrift server to. Not supported by the Nonblocking and HsHa server [default: 0.0.0.0]
-p, --port
Port to bind to [default: 9090]
-f, --framed
Use framed transport (implied when using one of the non-blocking servers)
-c, --compact
Use the compact protocol [default: binary protocol]
-h, --help
Displays usage information for the Thrift server
-threadpool
Use the TThreadPoolServer. This is the default.
-hsha
Use the THsHaServer. This implies the framed transport.
-nonblocking
Use the TNonblockingServer. This implies the framed transport.
```

## 常见问题

HBase 中的 Thrift Server 会在内存中分配空间用来检测介绍到的数据是否合法，因此如果接收到大量的无效数据，则 Thrift Server 有可能会挂掉。

可以通过配置如下的参数来避免以上问题的发生：
```shell
<property> 
  <name>hbase.regionserver.thrift.framed</name> 
  <value>true</value> 
</property> 
<property> 
  <name>hbase.regionserver.thrift.framed.max_frame_size_in_mb</name> 
  <value>2</value> 
</property> 
<property> 
  <name>hbase.regionserver.thrift.compact</name> 
  <value>true</value> 
</property>
```

也可以通过在启动时指定参数的方式解决：
```shell
$HBASE_HOME/bin/hbase-daemon.sh start thrift -p 9090 --infoport 8067 -f -c
```


