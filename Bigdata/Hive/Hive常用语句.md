# Hive 常用语句

标签：Hive

---

## 创建数据库

`create database d1;`

`create database d1 location '/path/to/d1';`

## 向表中插入数据

`insert into table t1 select * from t2`

`load data local inpath '/path/to/data' into table t1`

## 创建表

`create table t1 (name string, age int) row format delimited fields terminated by ',';`

## 增加列

`alter table t1 add columns (address string);`

## 删除表

`drop table t1;`

## 查看表的详细信息

`desc extended t1;`

## 授权

结合 Sentry 使用，Hive 可以对不同的角色进行授权，不同的资源（如数据库、表、服务器、HDFS URI 和字段）可以授权给不同的角色，然后每个角色可以绑定到多个用户组。

创建角色：
> CREATE ROLE [role_name];

给角色进行库授权：
> GRANT <PRIVILEGE> ON DATABASE <dbName> TO ROLE <roleName>

给角色进行表授权：
> GRANT <PRIVILEGE> ON TABLE <tableName> TO ROLE <roleName>

给角色进行字段授权(只支持授予 SELECT 权限)：
> GRANT SELECT(column_name) ON TABLE table_name TO ROLE role_name;

给角色进行 URI 授权(URI 只支持授予 ALL 权限)：
> GRANT ALL ON URI 'hdfs://namenode:XXX/path/to/table' TO ROLE role_name;