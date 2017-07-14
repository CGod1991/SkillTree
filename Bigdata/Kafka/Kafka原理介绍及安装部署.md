# Kafka 原理介绍及安装部署

标签：kafka 安装

---

## 简介

Kafka 是 Linkedin 于 2010 年 12 月份开源的消息系统，它主要用于处理活跃的流式数据，包括网站的点击量、用户访问或搜索的内容等。

Kafka 是一个轻量级的/分布式的/具备 replication 能力的日志采集组件,通常被集成到应用系统中,收集“用户行为日志”等,并可以使用各种消费终端(consumer)将消息转存到 HDFS 等其他结构化数据存储系统中。

Kafka 的作用类似于缓存，即活跃的数据和离线处理系统之间的缓存。

## 特性

高吞吐率：即使在普通的节点上每秒钟也能处理成百上千的 message。

显式分布式：即所有的 producer、broker 和 consumer 都会有多个，均为分布式的。

易于扩展：可以由一个节点扩展至数千个节点，不需要停止集群。

## 使用场景

### Messaging

对于一些常规的消息系统,kafka 是个不错的选择。Kafka 的 partitons/replication 和容错,使其具有良好的扩展性和性能优势。

但是，kafka 并没有提供 JMS 中的“事务性”、“消息传输担保(消息确认机制)”、“消息分组”等企业级特性。kafka 只能使用作为"常规"的消息系统,在一定程度上,尚未确保消息的发送与接收绝对可靠(比如,消息重发,消息发送丢失等)。

### Websit activity tracking

Kafka 可以作为“网站活性跟踪”的最佳工具，可以将网页/用户操作等信息发送到 kafka 中，并进行实时监控,或者离线统计分析等。

### Log Aggregation

kafka 的特性决定它非常适合作为“日志收集中心”，应用程序可以将操作日志“批量”“异步”的发送到 kafka 集群中,而不是保存在本地或者数据库中。

Kafka 可以批量提交消息/压缩消息等,这对生产者而言,几乎感觉不到性能的开支。此时消费者可以使用 Hadoop 等其他系统化的存储和分析系统。

## 原理架构

### 原理

Kafka 的设计初衷是希望做为一个统一的信息收集平台，能够实时的收集反馈信息，并需要能够支撑较大的数据量,且具备良好的容错能力。

Kafka 使用文件存储消息(append only log)，这就直接决定了 kafka 在性能上严重依赖文件系统的本身特性。为了减少磁盘写入的次数，broker 会将消息暂时缓存起来，当消息的个数(或尺寸)达到一定阀值时，再一起刷新到磁盘，这样会减少磁盘 IO 调用的次数。

Producer 将会和 topic 下所有 partition leader 保持 socket 连接。消息由 producer 直接通过 socket 发送到 broker，中间不会经过任何“路由层”。

事实上，消息被路由到哪个 partition 上，由 producer 客户端决定，默认方式为“轮询”。

Consumer 端向 broker 发送 “fetch” 请求，并告知其获取消息的 offset，此后 consumer 将会获得一定条数的消息。Consumer 端也可以重置 offset 来重新消费消息。

Kafka 将每个 partition 数据复制到多个 server 上，任何一个 partition 都有一个 leader 和多个 follower (可以没有)。

备份的个数可以通过 broker 配置文件来设定，其中 leader 处理所有的读写请求，follower 需要和 leader 保持同步。

当 leader 失效时，需在 followers 中重新选取出新的 leader，可能此时 follower 落后于 leader，因此需要选择一个 “up-to-date” 的 follower。选择 follower 时需要兼顾一个问题，就是新的 leader server 上所已经承载的 partition leader 的个数，如果一个 server 上有过多的 partition leader，意味着此 server 将承受着更多的 IO 压力，因此在选举新 leader 时，需要考虑到“负载均衡”。

Kafka 中所有的 topic 内容都是以日志的形式存储在 broker 上。如果一个 topic 的名称为 “my_topic”，它有 2 个 partitions，那么日志将会保存在 my_topic_0 和 my_topic_1 两个目录中。

日志文件中保存了一序列 “log entries” (日志条目)，每个 log entry 格式为“4个字节的数字 N 表示消息的长度” + “N 个字节的消息内容”。每个日志都有一个 offset 来唯一的标记一条消息，offset 的值为8个字节的数字，表示此消息在此 partition 中所处的起始位置。

### 部署架构

Kafka 集群、producer 和 consumer 都依赖于 zookeeper 来保证系统的可用性，保存一些元数据信息。

kafka 集群几乎不需要维护任何 consumer 和 producer 状态信息，这些信息由 zookeeper 保存，因此 producer 和 consumer 的客户端实现非常轻量级，它们可以随意离开，而不会对集群造成额外的影响。

Producer 端使用 zookeeper 用来发现 broker 列表，以及和 Topic 下每个 partition leader 建立 socket 连接并发送消息。

Broker 端使用 zookeeper 用来注册 broker 信息，监测 partition leader 存活性。

Consumer 端使用 zookeeper 用来注册 consumer 信息，其中包括 consumer 消费的 partition 列表等，同时也用来发现 broker 列表，并和 partition leader 建立 socket 连接，获取消息。




## 安装部署

### 安装

Kafka 的安装比较简单，只需要保证 zookeeper 集群运行正常，并配置好 server.properties 文件即可。

修改配置文件中的以下几项，并保证在各节点上保持一致：
```shell
broker.id=0		//该属性的值要保证各个节点之间不能重复，该值可以为随意的整数
port=9092
log.dirs=/opt/kafka-0.8.2/data
zookeeper.connect=localhost:2181	//此处需要修改成使用的 zookeeper 集群的信息，逗号分隔
```
### 启动

保证 zookeeper 集群正常运行，然后在每个节点上执行以下命令，启动进程：
```shell
/opt/kafka-0.8.2/bin/kafka-server-start.sh /opt/kafka-0.8.2/config/server.properties &
```

### 验证

可以使用 kafka 自带的 producer 和 consumer 来验证集群是否能正常工作。

使用 bin 目录下的 kafka-console-consumer.sh 和 kafka-console-producer.sh 脚本可以启动 consumer 和 producer 客户端。

1. 进入 kafka 的安装目录，执行以下命令（假设 zookeeper 集群信息为：server1:2181,server2:2181,server3:2181），创建一个名为 “my_topic”的topic：
```shell
bin/kafka-topics.sh --create --zookeeper server1:2181,server2:2181,server3:2181 --replication-factor 1 --partitions 1 --topic my_topic
```

2. 启动一个 producer，将消息发送到 “my_topic”，执行以下命令（假设 kafka 集群信息为：server1:9092,server2:9092,server3:9092）：
```shell
bin/kafka-console-producer.sh --borker-list server1:9092,server2:9092,server3:9092 --topic my_topic
```

3. 输入以下消息：
```shell
This is a message.
This is another message.
```

4. 在集群中的另一个节点上，进入 kafka 的安装目录，然后启动一个 consumer，订阅 “my_topic” 的消息，执行以下命令：
```shell
bin/kafka-console-consumer.sh --zookeeper server1:2181,server2:2181,server3:2181 --topic my_topic --from-beginning
```

5. 然后可以看到终端上输出以下内容，证明集群可以正常使用：
```shell
This is a message.
This is another message.
```

## API

### Producer

0.8 以前版本的 Procuder API 有两种：kafka.producer.SyncProducer 和 kafka.producer.async.AsyncProducer.它们都实现了同一个接口。
 
0.8 以后的新版本 Producer API 提供了以下功能：

- 可以将多个消息缓存到本地队列里，然后异步的批量发送到 broker，可以通过参数 producer.type=async 做到。

- 自己编写 Encoder 来序列化消息，只需实现下面这个接口。默认的 Encoder 是 kafka.serializer.DefaultEncoder。

- 提供了基于 Zookeeper 的 broker 自动感知能力，可以通过参数 zk.connect 实现。如果不使用 Zookeeper，也可以使用 broker.list 参数指定一个静态的 brokers 列表，这样消息将被随机的发送到一个 broker 上，一旦选中的 broker 失败了，消息发送也就失败了。

- 通过分区函数 kafka.producer.Partitioner 类对消息分区，可以通过参数 partitioner.class 定制分区函数。

### Consumer

Consumer API 有两个级别：低级别和高级别。

低级别的和一个指定的 broker 保持连接，并在接收完消息后关闭连接，这个级别是无状态的，每次读取消息都带着 offset。

高级别的 API 隐藏了和 brokers 连接的细节，在不必关心服务端架构的情况下和服务端通信。还可以自己维护消费状态，并可以通过一些条件指定订阅特定的 topic，比如白名单黑名单或者正则表达式。

#### 低级别 API

低级别的 API 是高级别 API 实现的基础，也是为了一些对维持消费状态有特殊需求的场景，比如 Hadoop consumer 这样的离线 consumer。
 
#### 高级别 API

这个 API 围绕着由 KafkaStream 实现的迭代器展开，每个流代表一系列从一个或多个分区的 broker 上汇聚来的消息，每个流由一个线程处理，所以客户端可以在创建的时候通过参数指定想要几个流。

一个流是多个分区多个 broker 的合并，但是每个分区的消息只会流向一个流。
 
### 代码示例

以下是两个简单的 Producer 和 Consumer 的代码示例。

Producer（循环向topic中发送消息）：
```shell
import java.util.Properties;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class Producer extends Thread{
  private final kafka.javaapi.producer.Producer<Integer, String> producer;
  private final String topic;
  private final Properties props = new Properties();

  public Producer(String topic){
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props.put("metadata.broker.list", "10.106.1.234:9092");	//需要替换成自己的broker信息
    producer = new kafka.javaapi.producer.Producer<Integer, String>(new ProducerConfig(props));
    this.topic = topic;
  }
  
  public void run() {
    int messageNo = 1;
    while(true){
      String messageStr = new String("Message_" + messageNo);
      producer.send(new KeyedMessage<Integer, String>(topic, messageStr));
      messageNo++;
    }
  }
}
```

Consumer（订阅topic消息，并在控制台输出）：
```shell
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Consumer extends Thread{
  private final ConsumerConnector consumer;
  private final String topic;
  
  public Consumer(String topic){
    consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig());
    this.topic = topic;
  }

  private static ConsumerConfig createConsumerConfig(){
    Properties props = new Properties();
    props.put("zookeeper.connect", zkConnect);	//需要将zkConnect替换成自己的Zookeeper集群信息
    props.put("group.id", "group1");
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");

    return new ConsumerConfig(props);
  }
 
  public void run() {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, new Integer(1));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);
    ConsumerIterator<byte[], byte[]> it = stream.iterator();
    while(it.hasNext())
      System.out.println(new String(it.next().message()));
  }
} 
```

## 对比

### Kafka VS Flume

- Kafka 是一个非常通用的系统。多个生产者和消费者可以共享多个主题。相比之下，Flume 被设计为一个旨在往 HDFS 或 HBase 发送数据的专用工具，它对 HDFS 有特殊的优化，并且集成了 Hadoop 的安全特性。

- Flume 内置了很多的 source 和 sink 组件。而 Kafka 只有一个更小的生产消费者生态系统，并且 Kafka 的社区支持不好。使用 Kafka 通常需要自己编写生产者和消费者代码。

- Flume 可以使用拦截器实时处理数据，这对于数据屏蔽或者过量是很有用的。而 Kafka 需要外部的流处理系统才能做到。

- Kafka 和 Flume 都是可靠的系统，通过适当的配置都能保证零数据丢失。然而，Flume 不支持副本事件，如果 Flume 代理的一个节点奔溃了，即使使用了可靠的文件管道方式，也会丢失这些事件直到恢复这些磁盘。而 Kafka 则没有这个问题。

### Kafka VS RabbitMQ

- RabbitMQ 遵循 AMQP 协议，以 broker 为中心，有消息的确认机制。Kafka 遵从一般的 MQ 结构，以 consumer 为中心，无消息确认机制。

- Kafka 具有很高的吞吐量，内部采用消息的批量处理，消息处理的效率很高。RabbitMQ 在吞吐量方面稍逊于 Kafka，支持对消息的可靠的传递，支持事务，但不支持批量的操作，基于存储的可靠性的要求存储可以采用内存或者硬盘。

- Kafka 采用 Zookeeper 对集群中的 broker、consumer 进行管理，可以注册 topic 到 Zookeeper 上；通过 Zookeeper 的协调机制，producer 保存对应 topic 的 broker 信息，可以随机或者轮询发送到 broker 上；并且 producer 可以基于语义指定分片，消息发送到 broker 的某分片上。而 RabbitMQ 的负载均衡需要单独的 loadbalancer 进行支持。
