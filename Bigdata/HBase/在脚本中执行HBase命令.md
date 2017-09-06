# 在脚本中执行 HBase 命令

标签：HBase 脚本

---

可以在 Shell 脚本中执行 HBase 的命令，一个简单的例子如下：
```shell
#!/bin/bash

exec "/opt/hbase/bin/hbase" shell <<EOF
disable 'opentsdb:tsdb'
drop 'opentsdb:tsdb'
disable 'opentsdb:tsdb-meta'
drop 'opentsdb:tsdb-meta'
disable 'opentsdb:tsdb-tree'
drop 'opentsdb:tsdb-tree'
disable 'opentsdb:tsdb-uid'
drop 'opentsdb:tsdb-uid'
EOF
```