# Apache Phoenix 常用配置

标签：Apache Phoenix 配置

---

Phoenix 的配置都是配置在 hbase-site.xml 配置文件中，其自身并没有维护自己的配置文件。

phoenix.mutate.maxSize：
> Phoenix 客户端一次批量提交的最大记录数。
> 默认值：500000