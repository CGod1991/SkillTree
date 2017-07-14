# Spark 学习笔记

标签：spark

---

- Spark 提供了两种针对 RDD 的操作：
	- transformation：根据已有的 RDD 数据集创建一个新的 RDD 数据集。
	- action：在 RDD 数据集上运行计算后，返回一个值或者将结果写入外部存储系统。

- 一个 Spark 应用程序主要包含一个驱动程序，该驱动程序执行用户的 main 函数并将各种算子分布到集群中并行运行。
- RDD 可被缓存到内存中，进而被多个并行执行的任务重用。
- Spark 支持两种共享变量：
	- 广播变量：可以缓存到各个节点的内存中的变量，通常为只读，类似于 Hadoop 中的 DistributedCache 的数据。
	- 累加器：只能用来做加法，例如计数和求和，类似于 Hadoop 中的 Counter。

- 目前主要有两种类型的 RDD：
	- 并行集合：接收一个已存在的 scala 集合，并在它上面运行各种并发计算。主要是通过调用 SparkContext 的 parallelize 方法，在一个已存在的 Scala 集合上创建而来。
	- Hadoop 数据集：类似于 MapReduce 模式，可在一个数据片的每条记录上运行各种函数。

- 创建并行集合的一个重要参数是 slice 数据，即将数据集切分为几份（类似 MapReduce 中的 InputSplit）。在集群模式中，Spark 将在每份 slice 启动一个 Task 进行处理。
- 通常，Spark 会根据集群的状况来自动设定 slice 的数目，也可以通过 parallelize 方法的第二个参数手动设置 slice 的数目。
- transformation 部分操作：
	- filter(func)：数据集中让 func 函数返回值为 true 的元素形成一个新的分布式数据集。
	- groupByKey([numTasks])：按照 key 进行分组，即在一个由 （K，V）对组成的数据集上调用，返回一个（K，Seq[V])对的数据集。其中 numTasks 为并行任务数目。
	- reduceByKey(func,[numTasks])：将数据按照 key 分组后，调用 func 函数处理。其中 numTasks 为并行任务数目。
	- join(otherDataset,[numTasks])：根据 key 连接两个数据集，即将类型为 (K,V) 和 (K,W) 类型的数据集合并成一个 (K,(V,W)) 类型的数据集。

- action 部分操作：
	- collect()：在驱动程序中以数组的形式返回数据集的所有元素。通常在 filter 或其他操作后调用，返回一个足够小的数据子集。

- 默认情况下，Spark 为每个 HDFS 数据块创建一个分片，但用户可以通过传入一个更大的值来指定更多的分片。
- 如果内存够用，尽量采用 MEMORY_ONLY 缓存级别，否则尝试使用 MEMORY_ONLY_SER。不要将数据集写到磁盘上，除非该数据集重算代价十分昂贵。
- 一个 Spark 运行时环境由四个阶段构成：
	- 构建应用程序运行时环境。除了启动任务相关的组件外，还需要在每个节点上启动一个 RDD 缓存管理服务 BlockManager。该服务采用了分布式的 master/slave 架构，其中，主节点上启动 master 服务 BlockManagerMaster，它掌握着所有 RDD 的缓存位置。从节点上启动供客户端存取 RDD 的 slave 服务。
	- 将应用程序转换成 DAG 图。主要涉及两个重要的概念：窄依赖和宽依赖。窄依赖指父 RDD 的一个分区最多被一个子 RDD 的分区所用，表现为一个父 RDD 的分区对已一个子 RDD 的分区或多个父 RDD 的分区对应一个子 RDD 的分区。宽依赖指子 RDD 的分区依赖于父 RDD 的所有分区。
	- 按照依赖关系调度执行 DAG 图。在该阶段中，DAGScheduler 按照依赖关系调度执行每个阶段：优先选择那些不依赖任何阶段的阶段，待这些阶段执行完成后，再选择那些依赖的阶段已经执行完成的阶段。依次进行，一直调度下去，直到所有的阶段都执行完成。
	- 销毁应用程序运行时环境。

- 不管是何种计算框架，均要包含两部分功能：资源管理和应用程序管理。
- 对于 Spark On YARN 来说，目前仅支持粗粒度的运行时环境构建方式，即在应用程序被提交到 YARN 集群上之后，在正式运行之前，将根据应用程序资源需求一次性将这些资源凑齐，之后使用这些资源运行，整个运行过程中不再申请新的资源。
- 应用程序运行过程中，Spark 按照 RDD 的依赖关系，将应用程序划分为若干个 Stage，每个 Stage 启动多个任务并行处理。
- Spark 才用了贪心算法划分阶段，即如果子 RDD 分区到父 RDD 分区是窄依赖，就可以实施经典的 fusion 优化，把对应操作划分到一个阶段。如果连续变换算则序列都是窄依赖，就可以把很多个操作合并到一个阶段，直到遇到一个宽依赖。
- 每个 Spark 应用程序拥有一套运行环境，开始运行时创建，运行结束时销毁。
- YARN-Spark ApplicationMaster 在自己内部启动 ClusterScheduler，生成 DAG 图，启动 Web UI 服务等。
- YARN-Spark ApplicationMaster 根据用户配置，向 ResourceManager 申请资源，在申请到的 Container 中启动 StandaloneExecutorBackend 服务，该服务通过 akka 向 ClusterScheduler 注册，等待领取任务。

## RDD

- Block-Manager 管理 RDD 的物理分区，每个 Block 就是节点上对应的一个物理数据块，可以存储在内存或磁盘上。
- RDD 中的 partition 是一个逻辑数据块，对应物理数据块 Block。
- 本质上，RDD 在代码中相当于是数据的一个元数据结构，存储着数据分区信息和其对应的逻辑结构映射关系，存储着 RDD 之前的依赖转换关系。RDD 中包含以下几个重要属性：
	- 分区列表。
	- 计算每个分片的函数。
	- 对父 RDD 的依赖列表。
	- 对 Key-Value 对数据类型 RDD 的分区器，控制分区策略和分区数。
	- 每个数据分区的地址列表（如 HDFS 上的数据块的地址）。


- RDD 是 Spark 的核心数据结构，RDD 的依赖关系形成了 Spark 的调度顺序，对 RDD 的操作构成了 Spark 程序。
- RDD 会被划分成许多分区分布在集群中的多个节点上，分区是个逻辑概念，变换前后的新旧分区在物理上可能是同一块内存存储。
- 有些 RDD 是计算的中间结果，其分区并不一定有物理上的内存或磁盘与之对应，如果要迭代使用数据，可以使用  cache() 函数缓存数据。
- 在物理上，RDD 实际上就是一个元数据结构，存储着诸如 Block、Node 的映射关系，以及其他的一些元数据信息。
- 一个 RDD 就是一组分区，在物理存储上，每个分区对应一个 Block，Block 可以存储在内存，当内存不够时可以存储到磁盘上。
- 如果是从诸如 HDFS 等外部存储作为数据源，则在 Spark 中的数据按照 HDFS 的数据分布策略进行数据分区，HDFS 中的一个数据块对应 Spark 的一个分区。同时，Spark 支持重分区，数据可以通过 Spark 默认的或者用户自定义的分区器决定数据块的分布在哪些节点。
- Spark 的核心数据模型是 RDD，但 RDD 是一个抽象类，具体由各个子类实现，如 MappedRDD、ShuffledRDD 等子类。Spark 将一些常用的大数据操作都转化成了对应的 RDD 的子类。
- RDD 常见的算子操作：
	- flatMap：处理的数据类型为 Value 型。将原来 RDD 的每个元素通过函数转换成新的元素，并将生成的 RDD 的每个集合中的所有元素合并为一个集合。
	- mapPartitions：处理的数据类型为 Value 型。获取到 RDD 中每个分区的迭代器，在函数中，通过该迭代器的对整个分区的元素进行操作。
	- union：处理的数据类型为 Value 型。合并两个 RDD 的所有元素，并且不进行去重操作。需要保证两个 RDD 的数据类型相同，并且和并后的 RDD 数据类型也要相同。
	- distinct：处理的数据类型为 Value 型。将 RDD 中的元素进行去重操作。
	- cache：处理的数据类型为 Value 型。将 RDD 元素从磁盘缓存到内存中，相当于 persist(MEMORY_ONLY) 的功能。
	- persist：处理的数据类型为 Value 型。对 RDD 进行缓存操作，具体缓存在哪由 StorageLevel 枚举类决定。
	- mapValues：处理的数据类型为 (Key, Value) 型。只对 Value 进行 map 操作，而不对 Key 进行处理。
	- partitionBy：处理的数据类型为 (Key, Value) 型。参数为分区器，对 RDD 的数据进行重新分区。如果原有的分区器和新的分区器一致，则不进行重新分区，否则，根据新的分区器进行重分区，生成一个新的 ShuffledRDD。
	- saveAsTextFile：将数据输出，存储到 HDFS 上的指定目录。RDD 的每个分区存储为 HDFS 上的一个数据块。
	- saveAsObjectFile：将分区中的每 10 个元素组成一个 Array，然后将该 Array 序列化，映射为 (Null, BytesWritable(Y)) 的元素，以 SequenceFile 的格式写入 HDFS。
	- collect：将分布式的 RDD 返回为一个单机 scala Array 数组，将结果返回到 Driver 所在的节点，以数组的形式存储。
	- reduceByKeyLocally：实现的是先 reduce 再 collectAsMap 的功能，先对 RDD 的整体进行 reduce 操作，然后再收集所有结果返回一个 HashMap。
	- lookup：处理的数据类型为 (Key, Value) 型。返回 RDD 中指定 Key 对应的元素形成的序列结果。如果该 RDD 中包含分区器，则只会处理 Key 所在的分区，返回由 (Key, Value) 形成的序列结果；如果该 RDD 中不包含分区器，则对整个 RDD 的元素进行扫描处理，搜索指定 Key 对应的元素。

- 广播变量（broadcast）主要用于广播 Map Side Join 中的小表，以及广播大变量。
- accumulator 变量主要做全局累加操作，广泛应用于在应用程序中记录当前运行指标的情景。

## 框架运行原理

- 对 RDD 的块管理通过 BlockManager 完成，BlockManager 将数据抽象为数据块，在内存或磁盘中存储。
- 根据 Spark Application 的 Driver 是否运行在集群中，可将 Spark Application 的运行方式分为 Client 和 Cluster 两种模式。
- Spark 中一些基本的概念：
	- Narrow Dependencies：父 RDD 的一个分区最多被一个子 RDD 的分区所用。
	- Shuffle Dependency：子 RDD 的分区依赖于父 RDD 的多个分区或所有分区，即一个父 RDD 的一个分区对应子 RDD 的多个分区。
	- Driver：负责运行 Application 的 main() 函数并创建 SparkContext。
	- Job（作业）：指一个 RDD Graph 触发的作业，通常由 Action 算子触发，在 SparkContext 中通过 runJob 方法向集群提交 Job。在一个 Application 里可以包含多个 Job。
	- Stage：每个 Job 会根据 RDD 的 Shuffle Dependency 被切分成多个 Stage，每个 Stage 中包含一组相同的 Task，这一组 Task 也叫做 TaskSet。
	- Task：一个 RDD 分区对应一个 Task，Task 执行对应的 Stage 中包含的算子。Task 被封装后放入 Executor 的线程池中执行。
	- TaskSetManager：一个 TaskSetManager 对应一个 Stage。
	- DAGScheduler：负责 Stage 的调度。
	- TaskScheduler：一个 TaskScheduler 对应一个 Application，也就是该 Application 中的所有 Action 触发的 Job 中的 TaskSetManager 都由对应的 TaskScheduler 进行调度。

- 从整体上看，Spark 集群的调度分为四个级别：Application、Job、Stage 和 Task。
- SparkContext 维持了整个 Application 的上下文信息，提供一些核心方法，如 runJob 提交 Job。然后根据主节点的分配获得一组独立的 Executor JVM 进程执行任务。Executor 空间内的不同 Application 之间是不共享的，同一个时间段内，一个 Executor 只能分配给一个 Application 使用。
- 默认情况下，用户向以 Standalone 模式运行的集群中提交的 Application 使用 FIFO 进行调度，每个 Application 会独占所有可用节点的资源。
- Action 算子触发的整个 RDD DAG 为一个 Job，在实现上，Action 算子是调用了 SparkContext 中的 runJob 方法提交了 Job。
- Task 的调度（本质上是 Task 在哪个分区执行）逻辑是由 TaskSetManager 完成。
- 序列化是将对象转换成字节流，本质上是将以链表形式存储的非连续空间的数据转化为在连续空间内存储的数组形式。
- 当大片连续区域进行数据存储并且存储的数据重复性高时，适合对数据进行压缩。
- 在分布式计算中，序列化和压缩是两个提升性能的重要手段。
- 物理上存储 RDD 是以 Block 为单位的，一个分区对应一个 Block，主要是通过分区 ID 来映射到具体的物理 Block。
- RDD 只支持粗粒度的转换，即在大量记录上执行单个操作。将创建 RDD 的一系列的 lineage 记录下来，以便恢复丢失的分区。
- 如果使用 Checkpoint 算子来做检查点，不仅要考虑 lineage 是否足够长，还要考虑是否有 Shuffle Dependency。Checkpoint 加 Shuffle Dependency 是性价比最高的。
- 以下两种情况需要使用 Checkpoint：
	- DAG 中的 lineage 过长，如果从头重算，开销比较大。
	- 在 Shuffle Dependency 上做 Checkpoint。

- Checkpoint 相当于是通过冗余数据来实现数据缓存，而 lineage 则相当于通过粗粒度的记录更新操作来实现容错。
- Shuffle 的本义是洗牌、混洗，即把一组有规则的数据重新打散重新组合成一组无规则的随机数据分区。而 Spark 中的 Shuffle 更像是洗牌的逆过程，把一组无规则的数据尽量转换成一组具有一定规则的数据。
- 在 Join 算法中有一个很经典的算法 Map Side Join，是确定数据该放到哪个分区的逻辑定义阶段。
- Shuffle 的两个阶段：
	- Shuffle Write：将数据根据下一个 Stage 分区数分成相应的 Bucket，并将 Bucket 最后写入磁盘。
	- Shuffle Fetch：去存储有 Shuffle 数据（即 Shuffle Write 生成的数据）的节点上的磁盘拉取需要的数据，将数据拉取到本地后执行用户定义的聚集函数操作。

- 在 Shuffle Write 时产生的 Shuffle 文件个数为 Map 任务的个数乘以 Reduce 任务的个数。