# HCatalog 介绍

标签：HCatalog

---

对于任何一个新的工具或者知识点，当我们需要对其进行研究时，都需要有一个明确的思路。简单来说，就是要在进行研究之前先问自己几个问题：这个东西的作用是什么？在什么场景下使用？具体应该如何使用？

下面，就沿着这一思路，对 HCatalog 做一个简单的研究。

## 目的

所有事物都有其存在的意义，那么对于 HCatalog，它的目的是什么呢？或者说，HCatalog 想要解决的问题是什么？

概括来说，HCatalog 提供了一个统一的元数据服务，允许不同的工具如 Pig、MapReduce 等通过 HCatalog 直接访问存储在 HDFS 上的底层文件。

HCatalog 使用了 Hive 的元数据存储，这样就使得像 MapReduce 这样的第三方应用可以直接从 Hive 的数据仓库中读写数据。同时，HCatalog 还支持用户在 MapReduce 程序中只读取需要的表分区和字段，而不需要读取整个表。也就是提供一种逻辑上的视图来读取数据，而不仅仅是从物理文件的维度。

HCatalog 还提供了一个消息通知服务，这样对于 Oozie 这样的工作流工具，在数据仓库提供新数据时，可以通知到这些工作流工具。

那么写到这里，就已经很清晰了，HCatalog 主要解决了这样一个问题：将以前各自为政的数据处理工具（如 Hive、Pig、MapReduce）有机的整合在一起，使其相互之间能够顺畅合作，进而提升效率。

## 场景

上面对 HCatalog 解决的问题描述的比较抽象，可能还是有点不好理解，下面通过一个具体的场景来展示 HCatalog 的作用（PS：场景来自 Hive 官网）：
- 张三将数据上传到 HDFS 上，并且将这些数据加载到相应的表中：
		hadoop distcp file:///file.dat hdfs://data/rawevents/20100819/data

		hcat "alter table rawevents add partition (ds='20100819') location 'hdfs://data/rawevents/20100819/data'"
- 李四需要在 Pig 中对张三加载的这些数据进行处理，如果不使用 HCatalog，那么他只能等到张三把数据加载成功后再手动在 Pig 中进行加载处理：
		A = load '/data/rawevents/20100819/data' as (alpha:int, beta:chararray, ...);
		B = filter A by bot_finder(zeta) = 0;
		...
		store Z into 'data/processedevents/20100819/data';
但如果使用 HCatalog 的话，当数据被张三加载成功后会自动发送消息，之后 Pig 会自动开始处理：
		A = load 'rawevents' using org.apache.hive.hcatalog.pig.HCatLoader();
		B = filter A by date = '20100819' and by bot_finder(zeta) = 0;
		...
		store Z into 'processedevents' using org.apache.hive.hcatalog.pig.HCatStorer("date=20100819");
- 王五需要在 Hive 中对这些数据进行分析，如果不使用 HCatalog 的话，需要手动将数据加载到 Hive 中的表中，然后进行一系列的分析操作：
		alter table processedevents add partition 20100819 hdfs://data/processedevents/20100819/data

		select advertiser_id, count(clicks)
		from processedevents
		where date = '20100819'
		group by advertiser_id;
但如果使用 HCatalog 的话，王五就可以直接对数据进行分析而不需要再手动加载，因为 Hive、Pig 共享的是同一份元数据：
		select advertiser_id, count(clicks)
		from processedevents
		where date = ‘20100819’
		group by advertiser_id;

由以上的场景可以看出，HCatalog 省去了许多需要人工干预的过程，使各个组件之间的协作自动化，大大提升了效率。

## 使用

HCatalog 是 Apache 的顶级项目，从 Hive0.11.0 开始，HCatalog 已经合并到 Hive 中。也就是说，如果是通过 binary 安装的 Hive0.11.0 之后的版本，HCatalog 已经自动安装了，不需要再单独部署。

因为 HCatalog 使用的就是 Hive 的元数据，因此对于 Hive 用户来说，不需要使用额外的工具来访问元数据，还是继续使用 Hive 的命令行工具。

对于非 Hive 用户，HCatalog 提供了一个称为 hcat 的命令行工具。这个工具和 Hive 的命令行工具类似，两者最大的不同就是 hcat 只接受不会产生 MapReduce 任务的命令。

如果用户需要在 MapReduce 程序中使用 HCatalog，HCatalog 提供了一个 HCatInputFormat 类来供 MapReduce 用户从 Hive 的数据仓库中读取数据。该类允许用户只读取需要的表分区和字段，同时其还以一种方便的列表格式来展示记录，这样就不需要用户来进行划分了。

同样的，HCatalog 提供了一个 HCatOutputFormat 类来供 MapReduce 用户向 Hive 中指定的表和分区中写入数据。

## 总结

以上就是对于 HCatalog 的简单介绍，因为没有在具体的生产环境中使用 HCatalog，因此只是介绍了一些比较基本的原理，更深入的原理解析留待以后补充。