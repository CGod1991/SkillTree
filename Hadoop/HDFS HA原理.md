# HDFS HA 原理

标签：HDFS HA

---

- Active NameNode 向 JournalNode 集群中写入 EditLog，只有超过半数的 Journal Node 返回写入成功才认为本次写入成功。如果返回写入成功

## Qurom Journal Manager

