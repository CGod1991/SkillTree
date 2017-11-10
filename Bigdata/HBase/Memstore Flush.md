# Memstore Flush

标签：HBase Memstore Flush

---

HBase 版本：1.2.0-cdh5.8.2

## 概述

在 HBase 中，Region 是集群中的数据服务单元，一个用户表对应了一个或多个 Region。在 Region 中，每个列族都对应了一个 Store，而 每个 Store 都由一个 Memstore 和一个或多个 HFile 组成。

由于 HBase 是基于 LSM 模型，所有数据都是先写入 Memstore 中，然后当 Memstore 中的数据达到一定大小时再统一落地写入磁盘。可以看出，采用这种方式可以大大提高数据的写入性能。

当用户读取数据时，根据局部性原理，会先去 Memstore 中查找，如果 Memstore 中存在用户请求的数据，则返回给用户；否则，再去读缓存和 HFile 中查找。

由此可见，Memstore 是 HBase 中非常重要的一部分，而 Memstore 的 Flush 操作更是其中的核心操作，下面就对 Memstore Flush 做一个简单介绍。

## 触发条件

Memstore Flush 主要包括以下几种情况

### Memstore 级别限制

当 Region 中的任意一个 Memstore 的大小达到了上限（由参数 hbase.hregion.memstore.flush.size 配置，默认为 128MB）时，会触发 Memstore Flush 操作。

具体的 Flush 策略可以通过建表时配置 FLUSH_POLICY 来进行制定，目前支持 FlushAllStoresPolicy 和 FlushLargeStoresPolicy 两种 Flush 策略。

### Region 级别限制

当 Region 中的所有 Memstore 的大小总和超过上限（由 hbase.hregion.memstore.block.multiplier `* `hbase.hregion.memstore.flush.size 的结果决定，默认为 4 `*` 128MB）时，会触发 Memstore Flush 操作。 

同样，也可以选择不同的 Flush 策略。

### Region Server 级别限制

当 Region Server 上的所有 Memstore 的大小总和超过上限（由 hbase.regionserver.global.memstore.upperLimit `＊` hbase_heapsize 的结果决定，默认为 40% JVM 的内存使用量）时，会触发部分 Memstore 的 Flush 操作。

此时，具体的 Flush 策略为：按照 Memstore 的大小顺序来，先 Flush Memstore 最大的 Region，然后 Flush 次大的，直至整个 Region Server 上的 Memstore 总和降至低于下限（由 hbase.regionserver.global.memstore.lowerLimit `＊` hbase_heapsize 的结果决定，默认为 38% JVM 的内存使用量）。

### WAL 级别限制

当某个 Region Server 中的 HLog 数量达到上限（由参数 hbase.regionserver.maxlogs 配置）时，会选择时间最早的一个 HLog 对应的一个或多个 Region 进行 Flush。

### 后台线程定期检查

HBase 会在后台运行一个线程，定期 Flush Memstore，确保 Memstore 不会长时间没有持久化。默认时间为 1 小时。

同时，为了避免所有的 Memstore 都在同一时刻进行 Flush，不同 Memstore 的定期 Flush 之间会有 20 秒的随机延迟。

### 手动执行

用户可以在 hbase shell 中手动对某个表或某个 Region 进行 Flush。