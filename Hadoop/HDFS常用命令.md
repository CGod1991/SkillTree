# HDFS 常用命令

标签：HDFS

---

## 概述

HDFS 命令行管理命令是通过 bin/hdfs 脚本执行的，主要形式为：`hdfs [--config confdir] [command] [generic_options] [command_options]`.

其中，--config 指定配置文件目录，设置后会覆盖默认的配置文件目录 $HADOOP_HOME/conf。

command 为具体的管理指令，下面详细介绍一些常用的 command 的使用。

## dfs

在 HDFS 文件系统上执行命令。

使用方式：`hdfs dfs [generic_options] [command_options]`。

常见的 command_options 有以下几种：

### appendToFile

将本地的一个或多个文件内容追加到指定文件系统的指定文件中，如果指定的文件不存在，则自动创建。

使用方式：`hdfs dfs -appendToFile <localsrc> ··· <dst>`。

例如：
	
	hdfs dfs -appendToFile /tmp/file1 /tmp/file2 /test/file	//将 file1 和 file2 的内容追加到 HDFS 上的 /test/file 中，如果 file 文件不存在，则自动创建

### chgrp

改变文件系统上目录或文件所属的组。

使用方式：`hdfs dfs -chgrp [-R] group <dst>`。

Options：
- -R：递归执行。
- group：修改后的组。
- <dst>：目标目录或文件。

例如：

	hdfs dfs -chgrp -R hadoop /tmp	//将 /tmp 目录下的所有文件修改为属于 hadoop 组

### count

查看指定目录下的子目录数、文件数、文件大小、文件名或目录名。

使用方式：`hdfs dfs -count [-q] [-h] PATH`。

Options：
- -h：文件大小以易读的形式展示。

### du

显示指定目录下每个文件或目录的大小。

使用方式：`hdfs dfs -du [-s] [-h] PATH`。

### getfacl

获取指定目录或文件的 ACL 信息。

使用方式：`hdfs dfs -getfacl [-R] PATh`。

### getmerge

将指定目录下的文件合并后的内容下载到本地。

使用方式：`hdfs dfs -getmerge <src> <localdst>`。

例如：

	hdfs dfs -getmerge /tmp /test	//将 HDFS 上 /tmp 目录下（不包括子目录）的所有文件内容合并后写入 /test 文件中

### setrep

修改指定目录或文件的副本数。

使用方式：`hdfs dfs -setrep [-R] [-w] <numReplicas> <path>`。

Options：
- -w：等待修改副本的操作执行完成后再返回，可能需要等待很长时间。
- -R：递归执行。

### test

对指定的目录或文件做检验。

使用方式：`hdfs dfs -test -[edz] URI`。

Options：
- -e：检查指定的文件是否存在，如果存在返回 0 或 true。
- -d：检查 URI 是否为一个目录，如果存在返回 0 或 true。
- -z：检查指定的文件长度是否为 0，如果是返回 0 或 true。

### text

将文本文件或某些格式的非文本文件以文本格式输出。

使用方式：`hdfs dfs -text <src>`。

### touchz

创建一个空文件，功能类似 Linux 中的 touch 命令。

使用方式：`hdfs dfs -touchz <path>`。

## fsck

检查 HDFS 中指定目录的健康状况，同时返回文件的一些信息，包括块数量、块的副本、损坏的块信息等。

使用方式：`hdfs fsck <path> [-list-corruptfileblocks | [-move | -delete | -openforwrite] [-files [-blocks [-locations | -racks]]]] [-includeSnapshots]`。

Options：
- -list-corruptfileblocks：显示损坏的块以及它们属于哪个文件。
- -move：将损坏的文件移动到 /lost+found 目录中。
- -delete：删除损坏的文件。
- -openforwrite：显示已打开正在写入的文件。
- -files：显示所有被检查过的文件的基本信息，包括文件大小和块。
- -blocks：与 `-files` 一起使用，显示块的详细信息。
- -locations：与 `-files -blocks` 一起使用，显示块的位置信息。
- -racks：与 `-files -blocks`一起使用，显示块的机架信息。

例如：

	hdfs fsck /tmp -files -blocks -locations	//检查 /tmp 目录的健康状况，并且显示所有块的位置信息

## version

显示 Hadoop 的版本和编译信息。

## balancer

对集群中的数据进行重新分配，从而使数据在集群中均匀分配。

使用方式：`hdfs balancer [-threshold <threshold>] [-policy <policy>]`。

Options：
- -threshold <threshold>：判断集群是否平衡的目标参数，每一个 datanode 存储使用率与集群总的存储使用率的差值都应该小于该值。默认值为10.
- -policy <policy>：集群是否平衡的指标，可选值为 datanode 和 blockpool，其中 datanode 为默认值。datanode 表示每个 datanode 均衡了才认为集群是均衡的，blockpool 表示只有每个 datanode 上的 block pool 均衡了才认为集群是均衡的。

## dfsadmin

对 HDFS 集群进行管理，以下列出一些常用的操作。

### report

报告文件系统的基本信息和统计信息。

使用方式：`hdfs dfsadmin -report [-live] [-dead] [-decommisioning]`。

### safemode

安全模式维护命令。

使用方式：`hdfs dfsadmin -safemode enter | leave | get | wait`。

### fetchImage

从 NameNode 获取最新的 fsimage 文件，保存到指定目录。

使用方式：`hdfs dfsadmin -fetchImage <localdir>`。

### getDatanodeInfo

获取指定 DataNode 基本的信息。

使用方式：`hdfs dfsadmin -getDatanodeInfo <datanode_host:ipc_port>`。

例如：

	hdfs dfsadmin -getDatanodeInfo datanode1:50020	//获取 datanode1 的基本信息

### printTopology

以树形结构显示集群中机架和机架上的节点信息。

使用方式：`hdfs dfsadmin -printTopology`。

## namenode

NameNode 的管理命令，以下列出了一些常用的操作命令。

### format

格式化指定的 NameNode。具体流程为：启动 NameNode，进行格式化，关闭 NameNode。

使用方式：`hdfs namenode -format [-clusterid cid] [-force] [-nonInteractive]`。

Options：
- -clusterid id：指定集群 id。
- -force：强制格式化，无论 name 目录是否存在。
- -nonInteractive：如果 name 目录存在，则放弃格式化。

### rollback

将 NameNode 回滚到之前的版本。使用时需要停止整个集群。

使用方式：`hdfs namenode -rollback`。

### bootstrapStandby

将当前节点设置为 Standby NameNode，执行该操作后，会从 Active NameNode 上拷贝 namespace 的快照。

使用方式：`hdfs namenode -bootstrapStandby`。





