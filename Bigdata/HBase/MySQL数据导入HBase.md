# MySQL 数据导入 HBase

标签：MySQL HBase 数据导入

---

使用 sqoop 将 MySQL 中的数据导入 HBase 中：
```shell
sqoop import --connect jdbc:mysql://127.0.0.1/test --username hive --password xxx \
--query "select * from SENTRY_GROUP where 1=1 and \$CONDITIONS" \
--hbase-table xuzd:mysql_import --hbase-create-table \
--hbase-row-key GROUP_ID \
--split-by CREATE_TIME \
--column-family f
```