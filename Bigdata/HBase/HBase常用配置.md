# HBase 常用配置

标签：HBase 配置

---

## 客户端

hbase.client.scanner.timeout.period
> 客户端 scanner 的租约时长
> 默认值：60000 ms

hbase.rpc.timeout
> 客户端向服务端进行一侧 rpc 请求的超时时间
> 默认值：60000 ms

hbase.client.operation.timeout
> 客户端总的操作超时时间。当客户端由于 rpc 超时或其他原因超时时，会进行重试直到抛出 RetriesExhaustedException，但当重试的总的超时时间超过该值时，就会提前中断重试并抛出 SocketTimeoutException。
> 默认值：1200000 ms

hbase.client.write.buffer
> BufferedMutator 写缓存大小，当客户端关闭了 autoflush 后，如果客户端缓存的数据超过该大小之后，会将缓存中的数据批量提交到 Region Server 端。
> 默认值：2097152 bytes

hbase.regionserver.logroll.period
> 该参数限制 WAL 的文件大小，当时间超过该值时会创建一个新的 HLog 文件。
> 默认值：3600000 ms