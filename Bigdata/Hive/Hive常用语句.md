# Hive 常用语句

标签：Hive

---

## 向表中插入数据

`insert into table t1 select * from t2`

`load data local inpath '/path/to/data' into table t1`

## 创建表

`create table t1 (name string, age int) row format delimited fields terminated by ',';`

## 增加列

`alter table t1 add columns (address string);`

## 删除表

`drop table t1;`