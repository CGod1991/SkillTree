# Spark SQL 整合 Sentry 总结

标签：Spark SQL Sentry

---

对于 Spark SQL 和 Sentry 的整合，大体的流程可以参考 Kafka 整合 Sentry 的实现。

Sentry 官网中给出了整合 Kafka 的大体步骤，具体参见：[这里](https://cwiki.apache.org/confluence/display/SENTRY/Integrating+with+Sentry+New+Universal+Authorization+Model) 。
	
类似的，对于 Spark SQL 的整合需要考虑如下几点：
- 定义授权模型：梳理 Spark SQL 中涉及到的所有需要进行权限控制的资源，比如数据库、表等。
- 