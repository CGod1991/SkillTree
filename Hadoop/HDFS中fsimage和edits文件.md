# HDFS 的 FSImage 和 EditLog 详解

标签：HDFS fsimage edits

---

fsimage 文件和 edits 文件是经过序列化的，在 HDFS NameNode 启动的时候，会先将 fsimage 的内容加载到内存中，然后执行 edits 文件中的各项操作，使得内存中的元数据和实际数据同步，可以对客户端提供读操作。

也就是说，fsimage 和 edits 文件合起来组成了 HDFS 的元数据，可想而知这两种文件的重要性。一旦 fsimage 文件损坏或丢失，将会直接影响 HDFS 上数据的操作。

下面就对 fsimage 和 edits 文件分别做详细的介绍。

## fsimage

fsimage 是 HDFS 元数据的一个永久性检查点，其中包含了 HDFS 中的所有目录和文件 idnode 的序列化信息。

对于目录来说，包含的信息主要有修改时间、访问控制权限等。

对于文件来说，包含的信息主要有修改时间、访问时间、块大小和组成一个文件块信息等。

fsimage 中并不会包含 DataNode 的信息，而是包含了 DataNode 上块的映射信息。DataNode 会定期向 NameNode 汇报自身的块信息。

## edits

edits 文件中保存的是 HDFS 所有更新操作的路径，客户端执行的所有写操作首先会写入 edits 文件中。

当 NameNode 刚启动的时候，除了将 fsimage 的内容加载到内存中之外，还会执行 edits 文件中的所有操作。当 NameNode 处于该阶段的时候，HDFS 系统处于安全模式，客户端无法对 HDFS 执行修改操作。

所以说，如果 edits 文件过大的话，会导致 NameNode 停留在安全模式的时间很长，因此需要对 fsimage 和 edits 进行合并来减小 edits 的大小。

## 合并

对于 Hadoop 2.x 版本来说，fsimage 和 edits 文件的合并是由 Standby NameNode 来负责的。

具体的过程如下：
- Active NameNode 和 Standby NameNode 都会实时的跟 Journal Node 上的 edits 进行同步。
- 在 Standby NameNode 上会一直运行一个线程 CheckpointerThread，主要职责就是定期检查合并的条件是否满足，如果满足条件则合并 fsimage 和 edits 文件，生成新的 fsimage 文件。
- Standby NameNode 合并完成后，会把最新的 fsimage 文件上传到 Active NameNode 的相应目录中。
- Active NameNode 接收到 Standby NameNode 发送的 fsimage 后，将旧的 fsimage 和 edits 文件清理掉。