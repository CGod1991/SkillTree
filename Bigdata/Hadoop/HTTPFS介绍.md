# HTTPFS 介绍

标签：HTTPFS

---

## 简介

HTTPFS 是 Cloudera 公司提供的一个 Hadoop HDFS 的 HTTP 接口，通过 Web HDFS REST API 可以对 HDFS 进行读写访问。

HTTPFS 与 Web HDFS 的区别是不需要客户端能够访问 HDFS 集群中的每一个节点，通过 HTTPFS 可以访问放置在防火墙后面的 HDFS 集群。

HTTPFS 是一个 Web 应用，部署在内嵌的 tomcat 中。