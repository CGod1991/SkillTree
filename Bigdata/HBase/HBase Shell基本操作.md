# HBase Shell 基本操作

标签：HBase Shell

---

## 创建 namespace

`create_namespace n1`

## 授权

### 给 user 用户授予 namespace hive 的所有权限

`grant 'user' , 'RWXCA', '@hive'`

## 创建表

`create 't1', 'f1'`