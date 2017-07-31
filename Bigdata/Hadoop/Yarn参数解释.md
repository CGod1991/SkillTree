# Yarn 参数解释

标签：Yarn

---

- `yarn.resourcemanager.max-completed-applications`：ResourceManager 中保留的最大已完成的任务信息数量，默认为 10000.当访问 ResourceManager 的 8088 端口的时候，因为 Yarn Web 的页面是前端分页的，所以如果已完成的任务数太多，那么在显示该页面的时候就会非常慢，因为会将所有 10000 个任务的信息一起发送给前台。可以将该属性的值改小一点，建议改成每天任务数的 2-3 倍。 

- `yarn.log-aggregation.retain-seconds`：控制 HDFS 上 YARN 聚合日志的保留时间，该值设置过小可能会影响对作业历史日志的查看。

- `yarn.log-aggregation-enable`：是否启用日志聚集功能，默认为 false。

- `yarn.log-aggregation.retain-seconds`：在 HDFS 上聚集的日志最多保存多长时间，默认为 -1，表示不会删除聚集日志。

- `yarn.log-aggregation.retain-check-interval-seconds`：多长时间检查一次 HDFS 上的聚集日志，并将满足条件的删除，默认为 -1。如果设置为 0 或负数，则表示该值为 `yarn.log-aggregation.retain-seconds` 属性值的十分之一。

- `yarn.nodemanager.remote-app-log-dir`：当应用程序运行结束后，日志将被转移到的 HDFS 上的目录，只有在启用了日志聚集后才有效。默认值为 `/tmp/logs`。

- `yarn.nodemanager.remote-app-log-dir-suffix`：远程日志目录的子目录名称，也就是 `yarn.nodemanager.remote-app-log-dir` 配置的目录的子目录。默认值为 `logs`，因此最终每个 application 在 HDFS 上的聚集日志的路径为：`${yarn.nodemanager.remote-app-log-dir}/${user}/${yarn.nodemanager.remote-app-log-dir-suffix}`