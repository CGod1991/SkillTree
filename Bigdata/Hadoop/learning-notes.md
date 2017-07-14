# Hadoop 学习笔记

标签：Hadoop

## Hadoop 简介

### 概述

Hadoop 是一个开源框架，可编写和运行分布式应用来处理大规模数据。Hadoop 最适合一次写入、多次读取的数据存储需求，从这点来说，和 SQL 世界中的数据仓库比较类似。

Hadoop 的集群内部既包含数据又包含计算，客户端仅需发送待执行的 MapReduce 程序，而这些程序通常都比较小（通常为几千字节）。

不同的用户可以从独立的客户端提交计算“作业”到 Hadoop，这些客户端可以是远离 Hadoop 集群的个人电脑。

### 数据单元

Hadoop 中使用键/值对作为基本的数据单元，数据的来源可以是任何形式，但是最终都会转化为键/值对的形式来进行处理。

### MapReduce

MapReduce 程序的执行主要分为两个阶段：mapping 和 reducing。每个阶段都被定义为一个数据处理函数，分别为：mapper 和 reducer。在 mapping 阶段，MapReduce 框架获取输入数据，并将数据单元装入 mapper；在 reducing 阶段，reducer 主要处理来自 mapper 的所有输出，并给出最终结果。

MapReduce 使用列表和键/值对作为其主要的数据原语。键与值通常为整数或字符串，但也可以是可忽略的假值，或者是复杂的对象类型。

在 MapReduce 框架中编写应用程序，实质上就是定制化 mapper 和 reducer 的过程。

当使用 MapReduce 模型来写应用程序时，Hadoop 会管理所有与扩展性相关的底层问题。

MapReduce 框架中完整的数据流：
- 整个 MR 应用的输入必须组织为一个键/值对的列表 list(`<k1, v1>`).
- 含有键/值对的列表被拆分，然后通过调用 mapper 的 map 函数对每个单独的键/值对 `<k1, v1>` 进行处理。
- 所有 mapper 的输出在逻辑上被整合到一个包含 `<k2, v2>` 对的巨大列表中，然后所有具有相同 k2 的对被组织成一个新的键/值对 `<k2, list(v2)>`,作为 reducer 的输入，经过 reducer 的 reduce 函数的处理，输出键/值对 `<k3, v3>`。
- 最后，MapReduce 框架会自动搜集所有的 `<k3, v3>` 对，并将其写入文件中。

## Hadoop 1.0 中的守护进程及作用

### NameNode

HDFS 的 master，主要指导 DataNode 执行底层的 I/O 任务。NameNode 在 HDFS 中负责跟踪记录文件如何被分割成文件块，被分割后的文件块被哪些 DataNode 存储，以及 HDFS 的整体运行状态是否正常。

### DataNode

负责将 HDFS 上的数据块读取或写入到本地的文件系统中。

当客户端对 HDFS 上的文件进行读写时，文件被分割成了多块，由 NameNode 告知客户端每个数据块存放在具体哪个 DataNode 上，然后客户端直接和 DataNode 进程进行通信，来处理与数据库对应的本地文件。

同时，DataNode 还负责与其他 DataNode 进行通信，复制这些数据块以实现数据冗余。

初始化时，每个 DN 将当前存储的数据块告知 NN。在初始化映射完成后，DN 仍会不断的与 NN 进行通信，上报本地修改的相关信息，同时接收指令创建、移动或删除本地磁盘上的数据块。

### SecondaryNameNode

负责与 NameNode 通信，根据集群所配置的时间间隔获取 HDFS 元数据的快照。

### JobTracker

是应用程序与 Hadoop 之间的纽带，负责监测 MapReduce 作业的整个执行过程。

代码提交到 Hadoop 集群后，JobTracker 负责确定具体的执行计划，包括决定处理哪些文件、为不同的任务分配节点以及监控所有任务的运行。如果任务失败，则 JobTracker 会自动重启任务，但是新的所分配的节点可能会有所不同，同时受到预定义的重试次数的限制。

每个 Hadoop 集群中只有一个 JobTracker 进程，通常运行在主节点上。

### TaskTracker

负责管理各个任务在每个子节点上的执行情况。

每个 TaskTracker 负责执行有 JobTracker 分配的单项任务，每个子节点上只有一个 TaskTracker 进程，但是每个 TaskTracker 进程可以生成多个 JVM 来并行的处理多个 map 或 reduce 任务。

### JobTracker 和 TaskTracker 的通信

如果在指定的时间内 JobTracker 没有接收到来自 TaskTracker 的心跳，则它会认为 TaskTracker 已经崩溃，进而重新提交相应的任务到集群中的其他节点上执行。

### 分布

在大型集群中，NN 和 JobTracker 会分别在两台机器上，每个子节点上起一个 DN 和 TaskTracker，从而能够在存储数据的同一个节点上执行任务。

## Hadoop1.0 中的组件

### 工作流

一个典型的 Hadoop 工作流：数据文件复制到 -> HDFS -> MapReduce 框架解析成键/值对形式 -> 作为 MapReduce 程序的输入，进行处理。

需要注意的是，除非需要定制数据的导入或导出，否则几乎不需要编程来读写 HDFS 文件。

### 文件命令

可以在 Hadoop 的文件命令中使用 Unix 的管道，将其结果发送给其他的 Unix 命令做进一步处理。

hadoop fs -getmerge：把一组 HDFS 文件复制到本地之前进行合并。

### FileSystem 类

Hadoop 文件 API 的起点，是一个与文件系统交互的抽象类，存在不同的具体实现类用来处理 HDFS 文件系统和本地文件系统。

Hadoop 文件 API 使用 Path 对象来编制文件和目录名，使用 FileStatus 对象来存储文件和目录的元数据。

### 序列化

为了可以让键/值对可以在集群上进行移动，MapReduce 框架提供了一种序列化键/值对的方法，只有支持这种序列化的类才能够充当键或值。

实现 Writable 接口的类只能作为值，而实现 WritableComparable 接口的类既可以做键也可以做值。因为键在 Reduce 阶段需要被比较，而值则仅仅被传递，所以充当键的类需要实现 Comparable 接口来提供比较功能。

### MapReduceBase 类

一个类要想充当 mapper 或 reducer，需要继承基类 MapReduceBase 并分别实现 Mapper 或 Reducer 接口。

MapReduceBase 类包含了对类的构造和解构方法：
- void configure（JobConf job）：提取 XML 配置文件或者应用程序主类中的参数，在数据处理之前调用该函数。
- void close（）：作为任务结束前的最后一个操作，完成所有的结尾动作，如关闭数据库连接，打开文件等。

### MapReduce 框架的不同阶段

mapper 阶段：负责数据处理，将输入数据转换成键值对的列表，作为 reducer 阶段的输入。

shuffle 阶段：将 mapper 的输出分给不同的 reducer。该阶段的实现是通过 partitioner 将不同的键值对发送给不同的 reducer 来实现的。

对于普通的 MapReduce 数据流来说，当输入数据被分配到不同的节点之后，节点之间通信的唯一阶段是在 shuffle 阶段。

当有多个 reducer 时，Hadoop 默认采用对键进行散列的方式（当然，也支持用户自定义分区方式）来确定 mapper 应该把键值对发给哪个 reducer。通过 HashPartitioner 类来强制执行该策略。

如果用户想要实现自定义的分区方式（自制 partitioner），只需要实现 Partitioner 接口中的 configure（） 和 getPartition（）两个函数即可。

其中，configure 函数将 Hadoop 对作业的配置应用在 partitioner 上，getPartition 函数返回一个介于 0 和 reduce 任务数之间的整数，指向键值对将要发向的 reducer。

reducer 阶段：接受来自 mapper 的数据，按照键/值对中的键对输入数据进行排序，并将具有相同键的值进行归并（注意：并不是求和，求和的操作是在 reduce 函数中做的），然后调用 reduce 函数，迭代处理与指定建向管理的值，生成一个列表（k3， v3）。

reducer 的输出文件放在一个公共的目录中，通常该目录命名为 part-xxxx，xxxx 为 reducer 的分区 ID。

### InputFormat 接口

该接口的实现定义了Hadoop 分割与读取输入文件的方式。

包含两个方法：getSplits 和 getRecordReader。
- getSplits：确定所有用做输入数据的文件并将之分割为输入分片。每个 map 任务分配一个分片。
- getRecordReader：提供一个对象（RecordReader），用来循环提取给定分片中的记录，并将每个记录解析为已经预定义好类型的键和值。

MapReduce 输出数据到文件时，使用的是 OutputFormat 类，与 InputFormat 类似。由于 reducer 仅需要将它的输出写入自己的文件，因此输出不需要进行分片。

在定义自己的 InputFormat 类时，常见的做法为从负责文件分割的 FileInputFormat 类中继承一个子类。

### FileInputFormat 抽象类

实现的 getSplits 方法把输入数据粗略的分为一组分片，分片的数目有 numSplits 参数限定，同时每个分片的大小必须大于配置项 mapred.min.split.size 定义的字节大小，小于文件系统的块大小。在实际情况中，一个分片总是以一个文件系统块为大小，在 HDFS 中默认为 64MB。

isSplitable 方法，用于检查是否可以将给定的文件分片。默认返回 true，因此所有大于一个文件块的文件都要进行分片。如果想要一个文件不进行分片，则需要覆盖 isSplitable 方法，返回 false。

### FSDataInputStream 类

扩展了 DataInputStream 以支持随机读，MapReduce 需要这个特性，因为一台机器可能被指派从输入文件的中间开始处理一个分片。

### TextInputFormat 类

返回的键表示每行的字节偏移量，但通常并不会使用该键用于数据处理。

### KeyValueTextInputFormat 类

在具有一定结构化的输入文件中使用，有一个预定义的字符（通常为制表符 \t）将每行的键和值分开。

### SequenceFileInputFormat 类

用于读取序列文件（Hadoop 提供的一种高效的二进制压缩文件，为 Hadoop 处理做了优化，常用于链接多个 MapReduce 作业时）。

### 分片与数据块

通常情况下，一个文件块（数据块）就是一个分片。

但需要注意的是，输入分片是一种逻辑上的划分，而数据块则是一种物理上的分割，在实际的应用中，这两者从未达到完全的一致。

由于 Hadoop 会保证全部的记录被处理，通常的情形是这样的：某个计算节点会被指定处理某个特定的分片，该分片的部分数据存储在该计算节点本地，还有一部分数据是存储在其他的节点之上，因此就需要该计算节点和其他的节点做通信，获取分片数据。

这种情况下，需要从远端读取的数据是很少的，额外的开销几乎可以忽略。

## MapReduce 编写

- 一个流行的开发策略是为生产环境中的大数据集建立一个较小的、抽样的数据子集，称为开发数据集。
- Hadoop 要求 Mapper 和 Reducer 必须是它们自身的静态类。这些类比较小，通常放在同一个类中做为内部类。但这些内部类是独立的，并不与外部类进行交互。
- JobClient 类与 JobTracker 通信，让作业在集群上启动。
- 一旦将 JobConf 对象传递到 JobClient.runJob（），它就被视为作业的总体规划，称为决定该作业如何运行的蓝本。
- 对于每个 JVM 来说，一个分片中有多少个记录，map 方法就会被调用多少次。因此，减少在 map 方法中生成的对象个数，有助于提高性能，并减少垃圾回收。
- Hadoop Streaming 允许使用任何可执行脚本来处理按行组织的数据流，数据取自 Unix 标准输入 STDIN，并输出到 STDOUT。
- 在生成一个计算最大值的 Hadoop 程序时，可以利用最大值的分配律特征，即给定一个分割为多片的数据集，全局的最大值为每个分片最大值的最大值。
- 聚类函数：用于计算描述统计，通常分为以下三类：
	- 分配型：具有分配律特征，可以在逻辑上循环的将函数应用到跟小的数据集，如最大值、最小值、求和、计数等。
	- 代数型：不遵循分配律特征，推导需要在简单的函数上做一些“代数”运算，如平均值和方差等。
	- 全集型：求中值或 K 个最大值/最小值的函数。

- 默认情况下，Streaming 使用制表符分离记录中的键和值。当没有制表符时，整个记录被视为键，而值为空白文本。
- Hadoop 包括一个称为 Aggregate 的软件包，它让数据集的汇总统计更为简单。
- 在 Streaming 中，Aggregate 包作为 reducer 来做聚集统计，只需要提供一个 mapper 来处理数据并以特定格式输出即可。
- 可以将 combiner 视为 reducer 的助手，主要用于减少 mapper 的输出，以降低网络和 reducer 上的压力。
- 对于 combiner 的实现原则是，如果我们去掉 combiner ，reducer 的输出也应该保持不变。
- 如果 reducer 仅仅处理分配型函数，如最大值、最小值等，则可以使用 reducer 自身来作为 combiner。
- Combiner 未必会提高性能。应该通过监控作业的行为来判断由 combiner 输出的记录数是否明显的小于 combiner 的输入记录的数量，而且这种减少必须能被证明花费额外的时间来运行 combiner 是值得的。

## 管理 Hadoop 集群

- `dfs.name.dir`：指定在 NameNode 本地文件系统中存储 HDFS 元数据的目录。
- `dfs.data.dir`：指定在 DataNode 本地文件系统中存储 HDFS 文件块的目录。
- `mapred.local.dir`：指定在 TaskNode 的本地文件系统中存储临时数据的目录。
- `mapred.tasktracker.[map|reduce].tasks.maximum`：指定在一个 TaskTracker 上可以运行的 map 或 reduce 的最大任务数。
- `dfs.datanode.du.reserved`：DataNode 应具备的最小空闲空间。
- `mapred.child.java.opts`：分配给每个子任务的堆栈大小。
- `mapred.reduce.tasks`：一个作业的 reduce 任务个数。
- 对于 `dfs.name.dir` 属性，指定多个目录可以更好的实现备份，多个目录以逗号分隔。
- 如果在 DataNode 上有多个磁盘，应该在每个磁盘上创建一个数据目录，并全部列在 `dfs.data.dir` 中，DataNode 会通过并行访问来提高 IO 性能。
- 在实际的生产环境中，大多数的系统在可用空间太低时会出现稳定性问题，应该通过设置 `dfs.datanode.du.reserved` 属性来预留足够的空间（通常为 1G），当数据节点上空闲的空间低于预留值时，DataNode 就会停止接受数据块的写入。
- 可以设置一个四核机器的 map 和 reduce 任务数最多为 6 个（即每种 3 个），因为 TaskTracker 和 DataNode 进程分别占用了一个任务，加起来总计 8 个。
- 在考虑允许的任务数时，还应考虑分配给每个任务的堆栈大小。
- 在一个作业中 reduce 任务数量应该等于 0.95 或 1.75 乘以工作节点数，再乘以 `mapred.tasktracker.reduce.tasks.maximum`。
- fsck：参数为文件路径，会递归地检查该路径下所有文件的健康状态。
- 默认情况下，fsck 会忽略正在被客户端写入而打开的文件。如果想展示这些文件，可以使用 `-openforwrite` 参数。
- 损坏的块和失踪的副本意味着数据已永久丢失。默认情况下 fsck 对损坏的文件什么也不做，但可以使用 `-delete` 参数将其删除。更好的方式是使用 `-move` 参数将已损坏的文件移动到 /lost+found 目录中备用。
- 在 HDFS 中不能执行文件，因此不会设置文件的 x 权限，但是可以设置目录的 x 权限，表示允许访问子目录。
- 启用回收站功能（默认不启用）后，执行删除文件的命令行程序不会立即删除文件，而是把文件移动到用户工作目录下的 `.Trash` 文件夹，在用户设置的时间延迟到来之前，该文件都不会被永久删除。
- NameNode 会检测到节点的死亡，并开始复制那些低于约定副本数的数据块。
- 当所有 DataNode 的利用率处于平均利用率加减一个阈值的范围内时，集群就被认为是平衡的。
- 使用 `start-balancer.sh` 执行均衡操作会占用网络资源，因此建议在空闲时间执行。同时，还可以设置参数 `dfs.balance.bandwidthPerSec` 来限制用于做均衡的带宽。
- NameNode 保存文件系统的元数据，并在内存中缓存集群中的块映射来获得不错的性能。
- NameNode 应该部署在集群中性能最强大的机器上，给它尽可能多的内存，同时可以配置 RAID 来提高可靠性，防止单个驱动器出现故障。
- 减轻 NameNode 负担的一种方法是增加数据块的大小，以减少文件系统中元数据的数量。但会在一定程度上降低文件的访问并行度。
- Secondary NameNode 并不是 NameNode 的失效备份，它的作用仅仅是定期清理 NameNode 上文件系统的状态信息，使之紧凑，进而帮助 NameNode 变得更有效率。主要是合并 FsImage 和 EditLog 生成新的快照，让 NameNo 专注于活动的事务。因此将 Secondary NameNode 视为一个检查点服务器更准确。
- NameNode 使用 FsImage 和 EditLog 这两个文件来管理文件系统的状态信息。
	- FsImage：是文件系统在一些检查点上的快照文件。
	- EditLog：记录了文件系统在该检查点之后的每个增量修改。与最新的 FsImage 一起就可以完全确定文件系统当前的状态。

- 当初始化或重新启动 NameNode 时，NameNode 会合并 FsImage 和 EditLog 这两个文件来产生新的快照，当初始化结束后，FsImage 为一个全新的快照，而 EditLog 文件为空。之后，任何导致 HDFS 状态改变的操作都会被记录到 EditLog 中，而 FsImage 保持不变。
- Secondary NameNode 从 NameNode 中获取 FsImage 和 EditLog 是通过向以下的网址发送 HTTP Get 请求得到的，同样也是通过相同的地址和端口将合并后的元数据更新到 NameNode 上：
	- FsImage：http://namenode.hadoop-host.com:50070/getimage?getimage=1
	- EditLog：http://namenode.hadoop-host.com:50070/getimage?getedit=1
	
- Secondary NameNode 把系统的元数据下载到 `fs.checkpoint.dir` 目录下，并在该目下合并 FsImage 和 EditLog 文件。
- 如果想要将 Secondary NameNode 同时作为 NameNode 的备份节点（虽然 Secondary NameNode 的主要目的并不是做备份），则可以将 Secondary NameNode 上的 `dfs.name.dir` 目录通过网络文件系统（NFS）暴露给 NameNode。主要方式为：先将 Secondary NameNode 上的 `dfs.name.dir` 目录通过 NFS 挂载到 NameNode 上的某个目录（比如 `/mnt/snn`），然后将该目录配置到 NameNode 上的 `dfs.name.dir` 属性中。这样，当 HDFS 向 NameNode 上写入元数据时，同时也会在 Secondary NameNode 的 `dfs.name.dir` 目录中写入一份，从而达到备份的目的。
- 当 NameNode 宕机时，要想让备份节点（通常并不是 Secondary NameNode）作为 NameNode ，需要将备份节点的 IP 修改为原始 NameNode 的IP。然后通过在备份节点上执行 `bin/start-dfs.sh` 命令让备份节点运行起来，成为 NameNode。
- Secondary NameNode 会周期性的做检查点（即合并 FsImage 和 EditLog，默认一个小时一次），所以对于恢复故障来说，Secondary NameNode 上的元数据并不是最新的。
- HDFS 上数据块的副本放置默认策略为：
	- 对于第一个副本，如果执行写操作的客户端是 Hadoop 集群的一部分，则放在客户端所在的 DataNode 中。否则，随机放置在集群中。
	- 对于第二个副本，如果有多个机架，则随机放置在与第一个副本不同的机架中。否则，随机放置在与第一个副本不同的节点上。
	- 对于第三个副本，放置在与第二个副本相同机架的不同节点上。
	- 如果多与三个副本，后续的副本随机放置。

- 对于任务的机架感知，Hadoop 会尽量保证数据本地行（即任务和要处理的数据在同一个节点上），如果无法保证在节点层面的数据本地性，则会退而保证机架层面的数据本地性（即任务和要处理的数据在同一个机架）。
- Hadoop 无法自动的感知节点所处的机架，需要用户提供一个可执行的脚本，把节点的 IP 映射到机架名，来帮助 Hadoop 知道每个节点的位置。需要在 `core-site.xml` 中的 `topology.script.file.name` 中指定该脚本的位置，然后Hadoop 调用该脚本时会使用一组 IP 地址作为相互独立的参数。以下是一个脚本示例：

```shell
#!/bin/bash

ipaddr = $1
segments = 'echo $ipaddr | cut --delimiter=. --fields=4'
if [ "$segments" -lt 128];then
	echo /rack-1
else
	echo /rack-2
fi

```

- 在更复杂的集群拓扑结构中使用查找表来获取节点对应的机架信息会更合适。
- 如果没有网络拓扑的脚本，Hadoop 将默认采用平坦拓扑，所有的节点都会被分配到 /default-rack 中。

## Hadoop 源代码结构

- lib：Hadoop 运行时依赖的第三方库，包括编译好的 jar 包和其他语言生成的动态库。在 Hadoop 启动或者用户提交作业的时候，会自动加载这些库。

## MapReduce 基本架构

- MapReduce 是一个分布式计算框架，主要由两部分组成：编程模型和运行时环境。
	- 编程模型：为用户提供了非常易用的编程接口，用户只需要像编写串行程序一样实现几个简单的函数就可以实现一个分布式程序。主要提供了五个可编程的组件：InputFormat、Mapper、Partitioner、Reduce 和 OutputFormat。
	- 运行时环境：将用户的 MapReduce 程序部署到集群的各个节点上，负责节点间通信、节点失效处理、数据切分等工作。主要由两类服务组成：JobTracker 和 TaskTracker，其中 JobTracker 负责资源管理和所有作业的控制，TaskTracker 负责接收来自 JobTracker 的命令并执行它。
	
	
- HDFS 和 MapReduce 都缺乏相应的安全机制。例如，用户只要知道了某个 block 的 blockID，就可以绕过 NameNode 直接从 DataNode 上读取该数据块。用户也可以向任意的 DataNode 上写 block。

- YARN（Yet Another Resource Negotiator）：Hadoop 新的资源管理框架，将 JobTracker 中资源管理和作业控制功能分开，分别有两个不同的进程实现：ResourceManager 和 ApplicationMaster。
	- ResourceManager：负责所有应用程序的资源分配。
	- ApplicationMaster：负责管理某一个应用程序。
	

- MapReduce 主要是为了解决搜索引擎面临的海量数据处理扩展性差的问题。

- MapReduce 具体由以下几个组件组成：Client、JobTracker、TaskTracker 和 Task。
	- Client：用户编写的 MapReduce 程序通过 Client 提交到 JobTracker 端；同时，Client 还提供了接口方便用户查看作业的运行状态。
	- JobTracker：监控所有的 TaskTracker 和作业的健康状况，一旦发现某个任务执行失败，则会将相应的任务转移到其他的节点；同时，还会跟踪任务的执行进度、资源使用量等信息，并将这些信息告知任务调度器，调度器会在资源出现空闲时，选择合适的任务使用这些资源。
	- TaskTracker：使用 slot 等量划分本节点上的资源量，slot 代表计算资源（CPU、内存等）。一个 Task 只有在获取到一个 slot 后才有机会运行，而 Hadoop 任务调度器的作用就是将各个 TaskTracker 上的空闲 slot 分配给 Task 使用。TaskTracker 通过 slot 数目（可配置参数）限定 Task 的并发度。
	- Task：由 TaskTracker 启动。对 MapReduce 而言，其处理的数据单位为 split，需要注意的是，split 为逻辑概念，与数据块的对应关系通常情况下为一对一或一对多。split 只包含了一些数据的元数据信息，比如数据起始位置、数据长度、数据所在节点等。split 的划分完全由用户自己决定，但要注意的是，split 的数量决定的 Map Task 的数量，因为每个 split 会交由一个 Map Task 来处理。
	

- 在 Hadoop 中，任务调度器是一个可插拔的模块，用户可以根据自己的需要设计相应的调度器。

- Map Task 的执行过程：
	- 通过 InputFormat 读取对应的 split 并将其迭代处理成一个个的键值对。
	- 对每个键值对，依次调用用户自定的 map() 函数进行处理。
	- 通过分区函数将 map() 函数的输出结果分为不同的 partition，并保存到本地磁盘上。


- Reduce Task 执行过程：
	- shuffle 阶段：从远程节点上读取对应 partition 的数据，即 Map Task 的输出结果。
	- Sort 阶段：按照键对键值对进行排序。
	- Reduce 阶段：对每个键值对依次调用用户自定义的 reduce() 函数，并将最终结果保存到 HDFS 上。

## MapReduce 生命周期

### 作业提交与初始化

- 用户提交作业后，首先由 JobClient 实例将作业相关信息（如程序 jar 包、作业配置文件、分片元数据信息文件等）上传到 HDFS 上。其中，分片元数据信息文件记录了每个输入分片的逻辑位置信息。
- JobClient 通过 RPC 与 JobTracker 通信，提交作业。JobTracker 接收到新作业的提交请求后，由作业调度模块对作业进行初始化。

### 任务调度与监控

- 任务调度与监控均由 JobTracker 完成。其中，任务调度器先选择作业，然后从该作业中选择任务。选择任务时会先重点考虑数据本地性。
- JobTracker 跟踪作业的整个运行过程，并为作业的成功运行提供全方位的保障。
	- 当 TaskTracker 或者 Task 失败时，转移计算任务。
	- 当某个 Task 执行进度远落后于同一作业的其他 Task 时，为其启动一个相同的 Task，然后选取计算快的 Task 的结果做为最终的结果。

### 任务运行环境准备

- 运行环境准备包括 JVM 启动和资源隔离，均由 TaskTracker 来完成。
	- TaskTracker 为每个 Task 都会启动一个独立的 JVM，以避免不同的 Task 在运行的过程中相互影响。
	- TaskTracker 使用操作系统进程来实现资源隔离，以防止 Task 滥用资源。

### 任务执行

- TaskTracker 为 Task 准备好运行环境后，便会启动 Task。在运行过程中，每个 Task 的最新进度先由 Task 通过 RPC 发送给 TaskTracker，然后再由 TaskTracker 发送给 JobTracker。

### 作业完成

- 所有 Task 执行完成后，整个作业执行完成。

### 序列化

- 序列化是指将结构化对象转为字节流以方便通过网络进行传输或者写入持久化的过程。在 MapReduce 中，序列化的目的有两个：永久存储和进程间通信。

### 配置文件

- 配置属性中的 final 参数：如果管理员不想让用户在程序中修改某个属性的值，则可以把该属性的 final 参数设置为 true。

### 旧版 API 中文件切分算法

- 文件切分算法主要用于确定 InputSplit 的个数以及 InputSplit 的数据段，对于 FileInputFormat，是以文件为单位切分生成 InputSplit 的，因此，如果输入数据中有多个文件，则总的 InputSplit 数为每个文件对应的　InputSplit 数之和。对于每个文件，有三个属性值来确定对应的 InputSplit 的个数：
	- goalSize：根据用户期望的 InputSplit 数目计算出来的，即 totalSize / numSplits。其中 totalSize 为该文件的大小，numSplits 为用户设定的 Map Task 个数，可通过 mapred.map.tasks 属性配置，默认为1.
	- minSize：InputSplit 的最小值，有参数 mapred.min.split.size 确定，默认为1。
	- blockSize：HDFS 中数据块的大小，默认为 64MB。
	
- InputSplit 的大小计算公式为：splitSize = max{minSize, min{goalSize, blockSize}}，一旦输入分片的大小确定之后，FileInputFormat 就将每个文件依次切分为大小为 splitSize 的 InputSplit，最后剩下不足 splitSize 大小的数据块单独称为一个 InputSplit。
- 当使用 FileInputFormat 实现 InputFormat 时，为了提高 Map Task 的数据本地性，应尽量使 InputSplit 大小与 block 相同。
- 由于 FileInputFormat 仅仅按照数据量多少对文件进行切分，因此 InputSplit 的第一条记录或最后一条记录有可能被从中间切开。对于这种情况，RecordReader 规定每个 InputSplit 的第一条不完整记录交个前一个 InputSplit 处理。

### 新版 API 文件切分算法

- 相比于旧版 API 中的文件切分算法，新版做的改动为不再考虑用户设定的 Map Task 的个数（即参数 mapred.map.tasks），而用 mapred.max.split.size 代替。计算公式为：splitSize = max{minSize, min{maxSize, blockSize}}

- 在用户作业被提交到 JobTracker 之前，由 JobClient 自动调用 checkOutputSpecs 方法，检查输出目录是否合法。
- 作业运行成功之后，会在最终结果目录下生成 _SUCCESS 空文件，该文件主要为上层应用提供作业运行完成的标识。比如，Oozie 需要通过检测该文件是否存在来判断作业是否运行完成。
- Partitioner 的作用是对 Mapper 产生的中间结果进行分片，以便将同一分组的数据交给同一个 Reducer 处理。

### Hadoop Streaming

Hadoop Streaming 是 Hadoop 为方便非 Java 用户编写 MapReduce 程序而设计的工具包。

### TaskTracker

- TaskTracker 充当了 JobTracker 和 Task 之间的沟通桥梁的作用：一方面从 JobTracker 端接收并执行各种任务，如运行任务、提交任务、杀死任务等；另一方面，将本节点上的各个任务的状态通过周期性的心跳汇报给 JobTracker。
- 在同一个 TaskTracker 上，由作业的第一个任务完成该作业的本地化工作，后续任务只需要进行任务本地化。

### 性能调优

- 对于任何一个作业，可以从应用程序、参数和系统三个角度进行性能优化，其中，前两种需要根据应用程序自身特点进行，而系统优化需要从 Hadoop 平台设计缺陷出发进行系统级改进。


## YARN

- 当用户向 YARN 上提交一个应用程序后，YARN 将分两阶段运行该程序：
	- 启动 ApplicationMaster；
	- 由 ApplicationMaster 启动应用程序，申请资源，并监控整个应用程序的运行过程直到运行成功。

- 如果用户想要让一个新的计算框架运行在 YARN 上，需要将该框架重新封装成一个 ApplicationMaster，而 ApplicationMaster 将作为用户程序的一部分提交到 YARN 上。也就是说，YARN 上的所有计算框架实际上就是客户端的一个库，因此不必单独将这个框架以服务的形式部署到节点上。
- 对于一些小作业，MRAppMaster 无须再为每个任务分别申请资源，而是让其重用一个 Container（即 MRAppMaster 所在的 Container），并按照先 Map Task 后 Reduce Task 的顺序串行执行每个任务。
- 为了能够运行 MapReduce 程序，需要让各个 NodeManager 在启动时加载 shuffle server。shuffle server 实际上就是 Jetty/Netty Server，Reduce Task 通过该 Server 远程从各个 NodeManager 上复制 Map Task 产生的中间结果。
- 编译 Hadoop 源码命令（在 Hadoop 安装目录执行）：`mvn package -Pdist -DskipTests -Dtar`，该命令仅编译生成 jar 包而不编译 native code 、测试用例和生成文档。
- 通常，ResourceManager 中没有与某个具体的应用程序相关的信息。

### Protocol Buffers

- Protocol Buffers 是 Google 开源的序列化库，具有平台无关、高性能、兼容性好等优点。
- YARN 将 Protocol Buffers 应用到了 RPC 通信中，默认情况下，YARN RPC 中所有参数均采用 Protocol Buffers 进行序列化/反序列化。
- Protocol Buffers 是一种轻便高效的结构化数据存储格式，可以用于结构化数据序列化/反序列化。很适合做数据存储或 RPC 数据交换格式，常用做通信协议、数据存储等领域与语言无关、平台无关、可扩展的序列化结构数据格式。

### YARN 应用程序设计方法

- 通常，编写一个 YARN Application 会涉及到三个 RPC 协议：
	- ApplicationClientProtocol：用于 Client 和 ResourceManager 之间。Client 通过该协议可实现将应用程序提交到 ResourceManager 上、查询应用程序的运行状态或者杀死应用程序等功能。
	- ApplicationMasterProtocol：用于 ApplicationMaster 和 ResourceManager 之间。ApplicationMaster 通过该协议向 ResourceManager 注册、申请资源等。
	- ContainerManagementProtocol：用于 ApplicationMaster 和 NodeManager 之间。ApplicationMaster 通过该协议要求 NodeManager 启动或撤销 Container 或者查询 Container 的运行状态。

- YARN Application 客户端的主要作用是提供一系列的接口供用户与 YARN 进行交互，包括提交 Application、查询 Application 状态、修改 Application 属性等。


### ResourceManager 功能

- ResourceManager 主要完成以下几个功能：
	- 与客户端交互，处理来自客户端的请求。
	- 管理 NodeManager，接收来自 NodeManager 的资源汇报信息，向 NodeManager 下达管理命令（比如杀死 Container 等）。
	- 启动和管理 ApplicationMaster，在其运行失败时重新启动。
	- 资源管理和调度，接收 ApplicationMaster 的资源申请请求，并为其分配资源。

### ResourceManager 内部架构

主要由以下几个部分组成：

#### 用户交互模块

分别针对普通用户、管理员和 Web 提供了三种服务。
- ClientRMService：为普通用户提供的服务，处理来自客户端的各种请求，包括提交应用程序、杀死应用程序、获取应用程序的运行状态等。
- AdminService：为管理员提供的一套独立的接口，用来管理集群。比如：动态更新节点列表、更新 ACL 列表、更新队列信息等。
- WebApp：展示集群的资源使用情况和应用程序的运行状态等信息。

#### NodeManager 管理模块

主要涉及以下几个组件：
- NMLivelinessMonitor：监控 NodeManager 是否存活，如果在一定时间内 NodeManager 没有汇报心跳信息，则 ResourceManager 认为该 NodeManager 已经死掉了，然后将其从集群中删除。
- NodesListManager：维护正常节点和异常节点列表，管理配置文件中的 exclude 和 include 列表（可动态加载）。
- ResourceTrackerService：处理来自 NodeManager 的请求，主要包括注册和心跳两种请求。
	- 注册：NodeManager 启动时进行，请求中包含节点 ID、可用的资源上限等信息。
	- 心跳：周期性行为，请求中包含节点上各个 Container 状态、运行的 Application 列表、节点健康状态（可通过脚本设置）等。

#### ApplicationMaster 模块

主要涉及以下几个组件：

- AMLivelinessMonitor：监控 ApplicationMaster 是否存活，如果在一定时间内 ApplicationMaster 没有汇报心跳信息，则 ResourceManager 认为该 ApplicationMaster 已经死掉，将其所有运行的 Container 设置为失败状态，然后将 ApplicationMaster 重新分配到其他节点上执行。
- ApplicationMasterLauncher：与 NodeManager 进行通信，要求其为某个应用程序启动 ApplicationMaster。
- ApplicationMasterService：处理来自 ApplicationMaster 的请求，主要包括注册和心跳两种请求。
	- 注册：在 ApplicationMaster 启动时进行，请求中包含 ApplicationMaster 启动的节点、对外的 RPC 端口号、tracking URL 等信息。
	- 心跳：周期性进行汇报，汇报的信息包括所需的资源描述、待释放的 Container 列表、黑名单等。

#### 资源分配模块

主要涉及一个组件：ResourceScheduler 资源调度器。按照一定的约束条件，将集群中的资源分配给各个应用程序。

- YARN 中自带了三个资源调度器：FIFO、Fair Scheduler 和 Capacity Scheduler（默认）。

#### Application 管理模块

主要涉及以下几个组件：

- ApplicationACLsManager：管理应用程序访问权限，包括查看权限和修改权限。
	- 查看权限：查看应用程序的基本信息。
	- 修改权限：修改应用程序优先级、杀死应用程序等。
- RMAppManager：管理应用程序的启动和关闭。
- ContainerAllocationExpirer：判断并执行一个已经被分配的 Container 是否应该被回收。


#### 状态机管理模块

ResourceManager 使用有限状态机维护有状态对象的生命周期，总共维护了 4 类状态机：

- RMApp：维护了同一个 Application 启动的所有运行实例（Application Attempt）的生命周期。
- RMAppAttempt：维护了一次运行尝试（或运行实例）的生命周期。Application Attempt 的生命周期和 ApplicationMaster 的基本上是一致的，如果 ApplicationMaster 重新启动，则意味着一个新的Application Attempt 被启动。
- RMContainer：维护了一个 Container 的生命周期。
- RMNode：维护了一个 NodeManager 的生命周期。

#### 安全管理模块

由 ClientToAMSecretManager、ContainerTokenSecretManager、ApplicationTokenSecretManager等模块组成。

### Hadoop HA

- Hadoop 2.0 中的 HDFS 和 YARN 均采用了基于共享存储的 HA 解决方案：Active Master 不断将信息写入一个共享存储系统，而 Standby Master 不断读取这些信息，以与 Active Master 的内存信息保持同步。常用的共享存储系统有：Zookeeper、NFS、HDFS、BookKeeper、QJM（Qurom Journal Manager）

- 当需要进行主备切换时，被选中的 Standby Master 需要先保证信息完全同步后，再将自己的角色切换至 Active Master。

- YARN HA 采用了基于 Zookeeper 的方案，HDFS HA 则提供了基于 NFS、BookKeeper 和 QJM 的三套实现方案。

- 对于 ResourceManager 来说，目前实现的容错机制带来的好处仅仅是用户无须重新提交应用程序，而正在运行的 Container 将不得不重新运行。

#### ZKFailoverController

- 基于 Zookeeper 实现的切换控制器，主要由 ActiveStandbyElector 和 HealthMonitor 两个核心组件组成。
	- ActiveStandbyElector：负责与 Zookeeper 集群交互，通过尝试获取全局锁，以判断所管理的 Master 是进入 Active 还是进入 Standby 状态。
	- HealthMonitor：负责监控各个活动 Master 的状态，以根据它们的状态进行状态切换。

## 安全管理

- 由于所有的 Hadoop 集群都是部署在防火墙之内的局域网中并且只允许公司内部员工访问，因此为 Hadoop 设置安全机制的动机并不像传统的安全概念那样是为了防范外部黑客的攻击，而是为了更好的让多用户在共享 Hadoop 集群环境下安全高效的使用集群资源。

- 通常，系统安全机制由认证和授权两部分构成：
	- 认证：对一个实体的身份进行判断。
	- 授权：向实体授予对数据资源和信息访问权限的决策过程。

### Kerberos

- Kerberos 是一种基于第三方的服务认证协议，特点是用户只需要输入一次身份验证信息就可以凭借此验证信息获得的票据访问多个服务。
- 在 Hadoop 中，Client 与 NameNode 及 Client 与 ResourceManager 之间初次通信均采用了 Kerberos 进行身份验证，之后便换用 Delegation Token 以减小开销。
- DataNode 与 NameNode 和 NodeManager 与 ResourceManager 之间的认证始终采用 Kerberos 机制。

#### 基本概念

- KDC（Key Distribution Center）：密钥分发中心，存储了所有客户端密码和其他账户信息，接收来自客户端的票据请求，验证其身份并对其授予服务票据。包含认证服务和票据授权服务两个服务。
- TGT（Ticket-Granting Ticket）：票据授权票据，客户端访问票据授权服务时需要提供的票据，主要是为了申请服务票据。
- AS（Authentication Service）：认证服务，负责检验用户的身份。如果用户通过了验证，则向用户发送票据授权票据。
- TGS（Ticket-Granting Service）：票据授权服务，负责验证用户的 TGT。如果用户通过验证，则向用户发送服务票据。
- Ticket：服务票据，用于在认证服务器和用户请求的服务之间安全的传递用户的身份，同时也传递一些附加信息。

### Hadoop 授权机制

- Hadoop 的授权机制是通过访问控制列表（ACL）实现的，按照授权实体，分为队列访问控制列表、应用程序访问控制列表和服务访问控制列表。
- 服务访问控制是通过控制服务之间的通信协议实现的，通常发生在其他访问控制机制（如文件权限检查、队列权限检查等）之前。

## Hadoop RPC

-  同其他的 RPC 框架一样，Hadoop RPC 主要分为四部分：
	-  序列化层：将结构化数据序列化为字节流，以便于在网络上进行传输或者进行永久存储。在 RPC 框架中，主要用于将用户请求中的参数或者应答转换成字节流，方便进行跨机器传输。在 YARN 中，Hadoop RPC 在该层使用了 Protocol Buffers 实现。
	-  函数调用层：定位要调用的函数并执行该函数。Hadoop RPC 采用了 Java 反射机制与动态代理来实现。
	-  网络传输层：描述了 Client 和 Server 之间消息传输的方式。Hadoop RPC 采用了基于 TCP/IP 的 Socket 机制实现。
	-  服务器端处理框架：描述了客户端和服务器端之间的信息交互方式。Hadoop RPC 采用了基于 Reactor 设计模式的事件驱动 I/O 模型实现。

## YARN 中的资源调度器

- 资源调度器是 YARN 中最核心的组件之一，并且是插拔式的，它定义了一整套的接口规范，以便用户可以按照需要实现自己的资源调度器。
- 在 YARN 中，资源调度器是以层级队列方式组织资源的，这种组织方式符合公司的组织架构，有利于资源在不同组织间分配和共享，进而提高集群资源利用率。
- 目前主要有两种多用户的资源调度器的设计思路：
	- 在一个物理集群上虚拟多个 Hadoop 集群，每个集群拥有一套独立的 Hadoop 服务，如 HOD 调度器。
	- 扩展 Hadoop 调度器，使其支持多个队列多个用户。这种调度器允许管理员按照应用需求对应用程序或者用户分组，并为不同的分组分配不同的资源量，同时通过添加各种约束防止单个用户或应用程序独占资源。

- YARN 采用了双层资源调度模型：
	- 在第一层中，ResourceManager 中的资源调度器将资源分配给各个应用程序的 ApplicationMaster。
	- 在第二层中，ApplicationMaster 再进一步讲资源分配给其内部的任务。

- YARN 的资源分配过程是异步的，也就是说，当资源调度器将资源分配给某个应用程序后，它不会立刻将该资源发送给对应的 ApplicationMaster，而是先放到一个缓冲区内，然后等待 ApplicationMaster 通过周期性的心跳来主动获取。
- 在分布式计算中，当应用程序申请的资源暂时无法保证时，资源调度器需要选择合适的资源保证机制，主要有以下两种：
	- 增量资源分配：优先为应用程序预留一个节点上的资源直到累计释放的空闲资源满足应用程序需求。
	- 一次性资源分配：暂时放弃当前的资源，直到某个节点上的剩余资源能够一次性满足应用程序的需求。

- 在资源调度器中，每个队列可设置最小资源量和最大资源量：
	- 最小资源量：在资源紧缺情况下每个队列需保证的资源量。
	- 最大资源量：极端情况下队列也不能超过的资源使用量。

- 最小资源量并不是硬资源保证，当队列不需要任何资源时，并不会满足其最小资源量，而是暂时将空闲的资源分配给其他需要资源的队列。
- Capacity Scheduler 和 Fair Scheduler 都是以队列为单位分配资源，每个队列都可以设定一定比例的资源最低保证和使用上限，同时，对每个用户也可以设定一定的资源使用上限以防止资源滥用。

### Capacity Scheduler

- Capacity Scheduler 的特点：容量保证、灵活性、多重租赁、安全保证、动态更新配置文件。
- NodeManager 发送的心跳信息中有两类信息需要资源管理器处理：
	- 最新启动的 Container。
	- 运行完成的 Container。

### Fair Scheduler

- 在每个队列中，Fair Scheduler 可选择按照 FIFO、Fair 或 DRF 策略为应用程序分配资源。 

## NodeManager

- NodeManager 是 YARN 中单个节点上的代理，它管理 Hadoop 集群中单个计算节点，功能包括与 ResourceManager 保持通信、管理 Container 的生命周期、监控每个 Container 的资源使用情况、追踪节点健康状态、管理日志和不同应用程序用到的附属服务。
- 在 YARN 中，内存资源是通过 ContainerMonitor 监控的方式加以限制的，对于 CPU 资源采用了轻量级资源隔离方案 Cgroups。
- NodeManager 上有专门的服务来判断所在节点的健康状态，主要通过两种策略判断：
	- 通过管理员自定义的 Shell 脚本。
	- 判断磁盘的好坏，如果坏磁盘的比例达到一定比例，则任务节点处于不健康状态。

- 通过执行管理员自定义的 Shell 脚本来判断节点的健康状态的方式可以带来以下的好处：
	- 可作为节点负载的反馈：由于 YARN 当前只对 CPU 和内存进行了隔离，对于网络和磁盘 IO 没有任何的隔离措施，因此在不同的任务之间仍会有干扰。对于这种情况，则可通过健康监测脚本来检查网络或磁盘使用情况，一旦发现异常，则将节点健康状态置为异常，暂时不接受新的任务，当异常的资源恢复正常后再继续接收新的任务。
	- 人为暂时维护 NodeManager：当需要对某个节点上的 NodeManager 进行维护时，可通过人为控制健康监测脚本输出暂时让 NodeManager 停止接收新任务，待维护完成后再恢复。

- 在 YARN 中，分布式缓存是一种分布式文件分发与缓存机制，主要作用是将用户应用程序运行时所需的外部文件自动透明的下载并缓存到各个节点上，从而省去用户手动部署这些文件的麻烦。
- 资源缓存是用时触发的，有第一个用到该资源的任务触发，后续的同类任务无须再次缓存，直接使用已经缓存好的资源即可。
- 在 NodeManager 上的 Container 运行任务过程中，为了提高写数据的可靠性和并发写性能，YARN 允许 NodeManager 配置多个挂在不同磁盘的目录作为中间结果的存放目录。对于任意一个应用程序，YARN 会在每个磁盘中创建相同的目录结构，然后采用轮询策略使用这些目录。
- 作为 YARN 中的一个服务，NodeManager 管理的是 Container 而不是任务，一个 Container 中可能运行各种任务，但是对于 NodeManager 来说是透明的，它只负责 Container 相关的操作。

### Container 生命周期

Container 启动过程主要经历三个阶段：资源本地化、启动并运行 Container、资源清理。

- 资源本地化：主要指分布式缓存所完成的工作，包括两部分：
	- 应用程序初始化：初始化各类服务必需的服务组件以供后续 Container 使用，通常由第一个 Container 完成。
	- Container 本地化：创建工作目录，从 HDFS 下载各类文件资源。

- Container 启动并运行：启动由 ContainerLauncher 服务完成，然后该服务进一步调用可插拔组件 ContainerExecutor 来进行启动。YARN 中提供了 ContainerExecutor 的两种实现：DefaultContainerExecutor 和 LinuxContainerExecutor。
- 资源清理：资源本地化的逆过程，主要负责清理各种资源，有 ResourceLocalizationService 服务完成。

### 资源隔离

- 资源隔离是指为不同任务提供独立使用的计算资源以避免它们互相干扰。当前存在的资源隔离技术主要有：硬件虚拟化、虚拟机、Cgroups、Linux Container 等。
- 默认情况下，YARN 采用了进程监控的方式控制内存使用，即每个 NodeManager 启动一个单独的监控线程监控每个 Container 的内存使用量，一旦发现其超过约定的资源使用量，则将其杀死。
- Cgroups 是 Linux 内核提供的一种可以限制、记录、隔离进程组所使用的物理资源（如内存、CPU、IO等）的机制。

### 作业恢复

- MRAppMaster 在运行过程中会在 HDFS 上记录一些运行日志，以便重启时恢复之前的作业运行状态。
- MRAppMaster 采用了任务级别的恢复机制，以任务为基本单位进行恢复。这种机制是基于事务型日志完成作业恢复的，只关注两种任务：运行完成的任务和未运行完成的任务。
- 作业恢复采用了 Redo 日志的方式，即在作业运行过程中，记录作业或任务的运行日志，当 MRAppMaster 重启恢复作业时，重新执行这些日志，以重构作业或任务的内存信息，进而让作业沿着之前的断点继续执行。
- 在 MRv2 中将排序插件化，用户可以自定义一个排序算法，实现诸如基于 Hash 的聚集算法或者 Limit-N 算法。

## Capacity Scheduler 和 Fair Scheduler

### Capacity Scheduler

容量调度器的主要思想是，允许多个组织共享整个集群资源，通过为每个组织分配专门的队列，然后再为每个队列分配一定的资源，这也是容量调取器的名称由来。

但是容量调度器有个缺点，那就是有可能导致某个任务饿死。假如在某个队列中，当前正在运行的任务占用了该队列所有的资源，而且此时集群中其他的队列也没有空闲的资源可供调度，即使后面来的任务优先级更高，因为容量调度器不会强制释放 Container ，因此后面的任务必须等待该任务运行完成。如果当前运行的任务长时间的占用资源，那么后面的任务需要一直等待。

### Fair Scheduler

公平调度器的主要思想是保证每个应用都能够公平的获得集群的资源。

公平调度器中为每个队列都设置了一个权重属性，通过这个权重属性作为公平调度的依据。比如，对于队列 A 和队列 B，如果配置的权重分别为 2 和 3。当集群中只有队列 A 中有任务运行时，此时队列 A 中的任务获得整个集群中的所有资源。此时，队列 B 中有新的任务，那么队列 B 获得的资源和队列 A 的资源比例为 3:2，即队列 B 中的任务获得集群中 60% 的资源，队列 A 中的任务获得 40% 的资源。

如果没有给队列配置权重，则默认队列的权重为 1，即每个队列之间均分集群的资源。

公平调度器支持资源抢占，那么也就避免了某个应用一直处于饥饿状态。因为每个队列都会获得一定的资源，即使集群中的资源都已经被分配了，那么当新的应用被提交后，调度器也会杀死一部分的 Container，用来分配给新的应用。