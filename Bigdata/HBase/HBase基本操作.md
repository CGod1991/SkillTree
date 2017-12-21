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

### 查看 HFile 内容

可以通过 hbase 命令来查看具体的 HFile 的内容，用法如下：
> $HBASE_HOME/bin/hbase org.apache.hadoop.hbase.io.hfile.HFile -f hdfs://wedp-cluster/data/hbase/data/xuzd/test_split/0b4da2531010ee453b2ba91dfbb713c8/0/760756a48c3740fb8f27873f7c01c387 -p

### bulkload

importtsv 的参数如下：
```shell
[hbase@cdh3-151 hbase-current]$ bin/hbase org.apache.hadoop.hbase.mapreduce.ImportTsv
ERROR: Wrong number of arguments: 0
Usage: importtsv -Dimporttsv.columns=a,b,c <tablename> <inputdir>

Imports the given input directory of TSV data into the specified table.

The column names of the TSV data must be specified using the -Dimporttsv.columns
option. This option takes the form of comma-separated column names, where each
column name is either a simple column family, or a columnfamily:qualifier. The special
column name HBASE_ROW_KEY is used to designate that this column should be used
as the row key for each imported record. You must specify exactly one column
to be the row key, and you must specify a column name for every column that exists in the
input data. Another special columnHBASE_TS_KEY designates that this column should be
used as timestamp for each record. Unlike HBASE_ROW_KEY, HBASE_TS_KEY is optional.
You must specify at most one column as timestamp key for each imported record.
Record with invalid timestamps (blank, non-numeric) will be treated as bad record.
Note: if you use this option, then 'importtsv.timestamp' option will be ignored.

Other special columns that can be specified are HBASE_CELL_TTL and HBASE_CELL_VISIBILITY.
HBASE_CELL_TTL designates that this column will be used as a Cell's Time To Live (TTL) attribute.
HBASE_CELL_VISIBILITY designates that this column contains the visibility label expression.

HBASE_ATTRIBUTES_KEY can be used to specify Operation Attributes per record.
 Should be specified as key=>value where -1 is used 
 as the seperator.  Note that more than one OperationAttributes can be specified.
By default importtsv will load data directly into HBase. To instead generate
HFiles of data to prepare for a bulk data load, pass the option:
  -Dimporttsv.bulk.output=/path/for/output
  Note: if you do not use this option, then the target table must already exist in HBase

Other options that may be specified with -D include:
  -Dimporttsv.skip.bad.lines=false - fail if encountering an invalid line
  '-Dimporttsv.separator=|' - eg separate on pipes instead of tabs
  -Dimporttsv.timestamp=currentTimeAsLong - use the specified timestamp for the import
  -Dimporttsv.mapper.class=my.Mapper - A user-defined Mapper to use instead of org.apache.hadoop.hbase.mapreduce.TsvImporterMapper
  -Dmapreduce.job.name=jobName - use the specified mapreduce job name for the import
  -Dcreate.table=no - can be used to avoid creation of table by this tool
  Note: if you set this to 'no', then the target table must already exist in HBase
  -Dno.strict=true - ignore column family check in hbase table. Default is false

For performance consider the following options:
  -Dmapreduce.map.speculative=false
  -Dmapreduce.reduce.speculative=false

```

使用 importtsv 生成 hfile 文件：
> $HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.bulk.output=hdfs://wedp-cluster/user/xuzd/out -Dimporttsv.separator=, -Dimporttsv.columns=HBASE_ROW_KEY,f:count xuzd:wordcount hdfs://wedp-cluster/user/xuzd/word_count.csv

completebulkload 的参数如下：
```shell
[hbase@cdh3-151 hbase-current]$ bin/hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles
usage: completebulkload /path/to/hfileoutputformat-output tablename
 -Dcreate.table=no - can be used to avoid creation of table by this tool
  Note: if you set this to 'no', then the target table must already exist in HBase
```

使用 completebulkload 将生成的 hfile 文件导入 HBase 中的表：
> $HBASE_HOME/bin/hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles hdfs://wedp-cluster/user/xuzd/out xuzd:wordcount