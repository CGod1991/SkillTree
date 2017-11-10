# Region Split

标签：HBase Region Split

---

本文主要简单介绍一下 HBase 中对于 Region Split 采用的策略以及大体的 Split 流程，基于 hbase-1.2.0-cdh5.8.2 版本。

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

## Split 流程

当 regionX 进行切分时，具体的切分流程如下：
1. 在 ZK 中的 /region-in-transition 目录下，创建 regionX 对应的 znode，并标记该 region 的状态为 spliting；
2. 由于 master 一直在 watch ZK 中的 /region-in-transition 目录，所以可以立即感知到 regionX 将要进行切分。然后 master 会修改内存中 regionX 的状态；
3. 在 HDFS 上 regionX 的目录下，创建临时文件夹 .split ，用来保存切分后的子 region 信息；
4. 关闭 regionX：主要是停止 regionX 对外提供写服务，并触发 regionX 的 flush 操作，将 memstore 中的数据全部持久化到磁盘；
5. 在 .split 文件夹中生成两个子文件夹，分别生成引用文件，指向 regionX 的文件。引用文件的文件名格式为：`父 region 对应的 HFile 文件.父 region 名`，引用文件内容为切分点的 splitkey 和表示该引用文件引用的是父文件的上半部分还是下半部分的 boolean 变量；
6. 将两个子文件夹拷贝到和 regionX 同级的目录中，形成两个新的子 region；
7. regionX 进行下线，不再对外提供服务；
8. 两个新的子 region 上线，对外提供服务。


