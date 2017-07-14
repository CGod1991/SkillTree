# Yarn 参数解释

标签：Yarn

---

- `yarn.resourcemanager.max-completed-applications`：ResourceManager 中保留的最大已完成的任务信息数量，默认为 10000.当访问 ResourceManager 的 8088 端口的时候，因为 Yarn Web 的页面是前端分页的，所以如果已完成的任务数太多，那么在显示该页面的时候就会非常慢，因为会将所有 10000 个任务的信息一起发送给前台。可以将该属性的值改小一点，建议改成每天任务数的 2-3 倍。 