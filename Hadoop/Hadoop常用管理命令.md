# Hadoop 常用管理命令

标签：Hadoop

---

## distcp

用于大规模集群内部和集群之间的拷贝。

参数：
- -f <urilist>：使用 <urilist> 做为源文件列表，<urilist> 文件中的内容应该是完整合法的 URI。等价于把 <urilist> 文件中的所有文件名列在命令行中。
- -m <num_map>：同时拷贝的 map 最大数目。
- -i：忽略拷贝过程中的失败。

## 