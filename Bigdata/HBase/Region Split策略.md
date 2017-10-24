# Region Split 策略

标签：HBase Region Split

---

本文主要简单介绍一下 HBase 中对于 Region split 采用的策略，基于 hbase-1.2.0-cdh5.8.2 版本。

## 概述

HBase 中 Region 的 Split 主要是为了防止单个 Region 过大，使得某个 RegionServer 上的负载过高，进而影响整个集群的性能。

对于 Region 的 Split 过程，需要关注的有两点：Split 的时机以及 Split Point 的选择。也就是说，当某个 Region 的大小增长到多少时应该进行 Split，Split 的时候从文件的什么位置进行。

目前 HBase 主要提供了三种 Split 策略：ConstantSizeRegionSplitPolicy、IncreasingToUpperBoundRegionSplitPolicy 和 KeyPrefixRegionSplitPolicy，下面就依次对这三种策略做一个简单的介绍。

## ConstantSizeRegionSplitPolicy

该策略是 0.94.0 版本之前默认的 Split 策略。

从名字可以看出来，该策略是根据 Region 的文件大小是否大于某个固定的阈值来判断是否需要进行 Split。判断逻辑如下：
- 如果某个 Region 下的有一个或多个 StoreFile 的大小大于阈值 ，则对该 Region 进行 Split。阈值的计算公式为：
```shell
hbase.hregion.max.filesize * (1 + (RANDOM.nextFloat() - 0.5) * hbase.hregion.max.filesize.jitter)
``` 
其中，`hbase.hregion.max.filesize` 的默认值为 10GB，`hbase.hregion.max.filesize.jitter` 的默认值为 0.25。
- Split Point 选择该 Region 中最大的一个 Store 中的最大的一个文件的最中心的 block 的 start rowkey。

## IncreasingToUpperBoundRegionSplitPolicy

该策略是 0.94.0 版本之后的默认策略。

该策略主要是根据 Region 的数量来决定阈值的大小，需要注意的是，这里的 Region 数量指的是该 Region 所在的 RegionServer 上所有的与该 Region 属于同一个表的 Region 数量。

该策略的主要逻辑和 Split Point 的选取与 ConstantSizeRegionSplitPolicy 大体相似，唯一不同的是阈值的计算。该策略的阈值计算逻辑如下：
- 如果 Region 数量为 0 或 大于 100，则阈值计算公式与 ConstantSizeRegionSplitPolicy 相同。
- 否则，阈值计算公式为：
	```shell
	min(ConstantSize, hbase.increasing.policy.initial.size * RegionCounts * RegionCounts * RegionCounts)
	```
其中，`ConstantSize` 为 ConstantSizeRegionSplitPolicy 计算出的阈值，`RegionCounts` 为 Region 数量，`hbase.increasing.policy.initial.size` 默认值为 `2 * hbase.hregion.memstore.flush.size（默认为 128MB）` 。

## KeyPrefixRegionSplitPolicy

该策略中阈值的选取与 IncreasingToUpperBoundRegionSplitPolicy 相同，唯一不同的是 Split Point 的选择。

从名字可以看出来，KeyPrefixRegionSplitPolicy 主要是根据 rowkey 的前缀来选择 Split Point。主要逻辑如下：
- 如果在建表的时候配置了 `KeyPrefixRegionSplitPolicy.prefix_length` 的大小，则前缀大小设置为该值，否则选取 `prefix_split_key_policy.prefix_length` 的值，如果该值也没有配置，则采用默认的 IncreasingToUpperBoundRegionSplitPolicy 策略。
- 选择该 Region 中最大的一个 Store 中的最大的一个文件的最中心的 block 的 start rowkey，然后截取该 rowkey 的前面部分，大小与前缀大小一致，以该部分的内容作为 Split Point。比如：中间点的 rowkey 为 `bbccc123`，前缀大小为 5，则 Split Point 为 `bbccc`。这样，所有 rowkey 以 `bbccc` 为前缀的数据都会存在同一个 Region 中。


