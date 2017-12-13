# HBase 基本操作

标签：HBase 

---

## Shell

## 创建 namespace

`create_namespace n1`

## 授权

### 给 user 用户授予 namespace hive 的所有权限

`grant 'user' , 'RWXCA', '@hive'`

## 创建表

`create 't1', 'f1'`

## 查看当前用户

`whoami`

## 权限控制

```shell
Command: grant
Grant users specific rights.
Syntax : grant <user>, <permissions> [, <@namespace> [, <table> [, <column family> [, <column qualifier>]]]

permissions is either zero or more letters from the set "RWXCA".
READ('R'), WRITE('W'), EXEC('X'), CREATE('C'), ADMIN('A')

Note: Groups and users are granted access in the same way, but groups are prefixed with an '@' 
      character. In the same way, tables and namespaces are specified, but namespaces are 
      prefixed with an '@' character.

For example:

    hbase> grant 'bobsmith', 'RWXCA'
    hbase> grant '@admins', 'RWXCA'
    hbase> grant 'bobsmith', 'RWXCA', '@ns1'
    hbase> grant 'bobsmith', 'RW', 't1', 'f1', 'col1'
    hbase> grant 'bobsmith', 'RW', 'ns1:t1', 'f1', 'col1'

Command: list_security_capabilities
List supported security capabilities

Example:
    hbase> list_security_capabilities

Command: revoke
Revoke a user's access rights.
Syntax : revoke <user> [, <@namespace> [, <table> [, <column family> [, <column qualifier>]]]]

Note: Groups and users access are revoked in the same way, but groups are prefixed with an '@' 
      character. In the same way, tables and namespaces are specified, but namespaces are 
      prefixed with an '@' character.

For example:

    hbase> revoke 'bobsmith'
    hbase> revoke '@admins'
    hbase> revoke 'bobsmith', '@ns1'
    hbase> revoke 'bobsmith', 't1', 'f1', 'col1'
    hbase> revoke 'bobsmith', 'ns1:t1', 'f1', 'col1'

Command: user_permission
Show all permissions for the particular user.
Syntax : user_permission <table>

Note: A namespace must always precede with '@' character.

For example:

    hbase> user_permission
    hbase> user_permission '@ns1'
    hbase> user_permission '@.*'
    hbase> user_permission '@^[a-c].*'
    hbase> user_permission 'table1'
    hbase> user_permission 'namespace1:table1'
    hbase> user_permission '.*'
    hbase> user_permission '^[A-C].*'

--------------------------------------------------------------------------------

NOTE: Above commands are only applicable if running with the AccessController coprocessor

```

## hbase 命令

可以通过 hbase 命令来查看具体的 HFile 的内容，用法如下：
> $HBASE_HOME/bin/hbase org.apache.hadoop.hbase.io.hfile.HFile -f hdfs://wedp-cluster/data/hbase/data/xuzd/test_split/0b4da2531010ee453b2ba91dfbb713c8/0/760756a48c3740fb8f27873f7c01c387 -p