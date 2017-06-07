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
