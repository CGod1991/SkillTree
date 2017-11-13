# Region Compact

标签：HBase Region Compact

---

HBase 版本：1.2.0-cdh5.8.2

## 简介

我们知道，HBase 是基于 LSM 架构的，也就是说数据写入的时候，先写入缓存，然后等待一段时间后再将缓存中的数据刷写到磁盘。触发缓存刷写磁盘的情况有很多，比如 Memstore 大小达到上限、后台线程定时刷写、用户手动触发等。这样一来，就会产生许多大小不一的数据文件，会在一定程度上影响数据的读取效率（因为需要跨文件读取，会产生额外的 IO 操作）。因此，HBase 采用 Compact 来防止数据文件堆积过多。

Compaction 会从 Region 中的某个 Store 中选择一些 StoreFile 进行合并，合并执行的具体操作就是从这些文件中读出 KeyValue 值，然后按照大小排序后写入新的文件。

## 分类

Compaction 主要有两种：Minor Compaction 和 Major。

### Minor Compaction

选取一些相邻的、小的 StoreFile 进行合并，在这个过程中，不会处理被删除和过期的数据。

一次 Minor Compaction 的结果是产生数量更少、大小更大的 StoreFile 。

### Major Compaction

将一个 Store 下的所有 StoreFile 合并成一个大的 StoreFile。在这个过程中，会清理三类无意义的数据：标记被删除的数据、过期数据以及版本号超过设定版本号的数据。

通常情况下，Major Compaction 会持续较长的时间，消耗大量的系统资源。

## 触发时机

Compaction 的触发主要有三种情况：Memstore Flush、周期性检查和用户手动触发。

### Memstore Flush

由于每次 Memstore Flush 都会产生一个新的 StoreFile，因此在每次 Flush 之后都会检查是否需要进行 Compaction。

### 周期性检查

后台线程 CompactionChecker 会定期触发检查是否需要进行 Compaction。

### 手动触发

用户可以手动执行 Compaction 命令。

通常是为了执行 Major Compaction，在业务低峰期的时候执行。

## 选择合并文件

对于 Major Compaction，因为是合并 Store 下的所有文件，所以并不存在如何选择合并文件的问题。

对于 Minor Compaction，主要有三种选择合并文件的策略：RatioBasedCompactionPolicy、ExploringCompactionPolicy 和 StripeCompactionPolicy。