# HCatalog 介绍

标签：HCatalog

---

- HCatalog 应用程序的数据模型以表的形式组织，表可以放入数据库中。
- HCatalog 的默认数据格式是 RCFile。但如果数据以不同的格式存储，那么用户可以实现 HCatInputStorageDriver 和 HCatOutputStorageDriver 来定义底层数据存储和应用程序记录格式之间的转换。