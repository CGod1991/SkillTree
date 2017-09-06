# OpenTSDB 介绍

标签：opentsdb 时序数据库

## 简介

OpenTSDB 是基于 HBase 存储时间序列数据的一个开源数据库，本质上，只是一个 HBase 的应用，它对于时间序列数据的处理可以供其他系统参考借鉴。

## tcollector

tcollector 是一个客户端程序，用来收集本机的数据，并将数据发送到 OpenTSDB。

tcollector 主要做以下几件事：
- 运行所有的采集者并收集数据。
- 完成所有发送数据到 TSD 的连接管理任务。
- 不必在你写的每个采集者中嵌入这些代码。
- 是否删除重复数据。

## 表

OpenTSDB 中的数据是存储在 HBase 中的，涉及到的表主要有四个：tsdb-uid、tsdb、tsdb-meta（可选） 和 tsdb-tree（可选）。

### tsdb-uid

tsdb-uid 维护了一个全局唯一值 UID 索引，其中 UID 对应监控指标标签。

tsdb-uid 中新的 UID 是从 0 开始增加的，每增加一条新的记录，UID 值加 1 。

当注册一个新的 UID 时，会在 tsdb-uid 表中增加两行：一行从标签名映射到 UID，另一行从 UID 映射到标签名。

name-UID 行使用标签名作为行健，并在 id 列族存储 UID，用标签类型作为列限定符。

UID-name 行使用 UID 作为行健，在 name 列族中存储标签名，用标签类型作为列限定符。

### tsdb

tsdb 表提供时间序列数据的存储和查询支持，也就是说，tsdb 表中保存了一个个实际的数据点。

tsdb 表支持按照日期范围和标签进行过滤的数据查询。

tsdb 表的行健的组成规则如下：
```shell
UID(metric) + timestamp + UID(tagk1) + UID(tagv1) + ... + UID(tagkN) + UID(tagvN)
```
其中，所有属性对应的 UID 都保存在 tsdb-uid 表中。timestamp 是以小时为单位的，也就是说，同一个小时内的所有数据保存在同一行中。

tsdb 表只有一个列族 t，列限定符为同一小时内每隔一秒的时间戳，这样，一个小时内的所有数据都保存在同一行之中。

### tsdb-meta

主要用来存储时间序列索引和元数据。是一个可选特性，默认是不开启的，可以通过配置文件来启用该特性。

tsdb-meta 中的列族只有一个：name，列限定符为 ts_meta 和 ts_ctr。

ts_ctr 为一个列计数器，记录了一个时间序列中存储的数据个数。

### tsdb-tree

用来以树状层次关系来表示metric的结构，只有在配置文件开启该特性后，才会使用此表。
