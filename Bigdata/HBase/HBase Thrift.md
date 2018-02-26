# HBase Thrift

标签：HBase Thrift

---

## 概述

## 连接机制

对于HBase的Thrift服务，控制客户端连接的创建和销毁，主要流程如下：
1. 创建：HBase主要有三个参数来控制Thrift连接的创建：hbase.thrift.minWorkerThreads（默认值16）、hbase.thrift.maxQueuedRequests（默认值1000）和hbase.thrift.maxWorkerThreads（默认值1000）。

	其中，hbase.thrift.minWorkerThreads表示连接池的初始化大小，当有新的连接请求时，首先使用该连接池内的连接。当连接池内的连接用完了，再有新的连接请求时，并不会立即创建新的连接，而是将请求放到一个队列中，只有该队列的大小超过hbase.thrift.maxQueuedRequests的值之后，才会为最开始等待的请求创建新的连接。在这个过程中，如果线程池中有可用的连接，那么会立即将该连接分配给等待的请求。

	也就是说，如果最开始的16个连接一直被占用没有释放，那么第17个连接需要等到新的请求超过1000之后才会创建。同理，这1000个请求后面的请求需要等到前面的所有请求都被处理之后才会被处理，也就是会有一定的延迟，这个延迟的时间取决于前面1000个等待请求的处理时间。

	hbase.thrift.maxWorkerThreads控制了连接的上限，如果连接数达到该上限，那么将等待其他连接释放而不会再创建新的连接。

2. 销毁；服务端有自动的连接清理机制，以防止有大量的空闲连接占用资源和连接数。
	hbase.thrift.connection.cleanup-interval控制了清理线程的检查周期，默认是10秒检查一次。
	hbase.thrift.connection.max-idletime控制了空闲连接的存活时间，超过该时间的空闲连接会被服务端关闭，默认是10分钟。


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


