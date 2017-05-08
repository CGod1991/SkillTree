# RabbitMQ 原理介绍及安装部署

标签：RabbitMQ 安装

---

## 简介

RabbitMQ 是一个用 Erlang 语言开发的 AMQP 开源实现。AMQP（Advanced Message Queue Protocol），高级消息队列协议，是异步消息处理领域的一个公开标准，主要由 Cisco、RedHat 等联合制定。而 RabbitMQ 就是 AMQP 的一个开源实现，由 RabbitMQ Technologies Ltd 公司开发并提供商业支持。

RabbitMQ 主要应用于大型系统中不同的应用或者子系统之间的通信，通过分隔数据的发送和接收来解耦应用。

## 一条消息的“一生”

在对 RabbitMQ 进行更进一步的介绍之前，先让我们来看一看在 RabbitMQ 中，一条消息，从生产者到消费者完整的轨迹。

当生产者发布一条消息时，首先跟 RabbitMQ 建立连接（channel），通过该连接将想要发布的消息发送到交换器（exchange）上。交换器通过特定的路由规则（routing_key），将消息发送到某个队列（queue）。RabbitMQ 会监控该队列，一旦发现有消费者订阅了该队列，就将消息发送给消费者进行处理，然后将该消息从队列中删除。

需要注意的是，这里提到的生产者和消费者只是消息发送和接收的概念体现，每个客户端都可以是消费者或生产者。

接下来，对上面涉及到的一些重要的概念进行进一步的介绍。

## 信道

channel，是消费者或生产者与 RabbitMQ 之间的一条连接，本质上是 TCP 连接中的一个虚拟连接。在 RabbitMQ 中，消息的发送和接收、队列的订阅等操作都是通过信道完成的。

之所以选择信道，而不是在 TCP 连接上进行命令的发送，主要是基于性能的考虑。在操作系统中，建立和销毁 TCP 连接的开销是很昂贵的。而且，同一时刻，操作系统对于 TCP 连接的数量也是有限制的，很容易成为性能的瓶颈。而采用信道就不会有这种问题，可以在一个 TCP 连接中，任意的创建多条信道。

## 路由键

routing_key，是一条特定的规则，决定了消息将要被发送到哪个队列。每条消息在发布的时候，都需要指定自己的 routing_key。

RabbitMQ 通过路由键实现了队列和交换器之间的绑定。

## 交换器

exchange，生产者将消息发送给交换器，然后由交换器根据路由规则，决定将消息发送到哪个队列。

交换器本质上只是一个名称和一个队列绑定列表，当消息被发布到交换器时，实际上是所连接的信道将消息上的路由键和交换器中的绑定列表做比较，然后路由消息

在 RabbitMQ 中，常用的交换器类型有三种：direct、fanout 和 topic。下面，对这三种类型的交换器做更进一步的介绍。

### direct

如果消息中的路由键和某个队列的路由键匹配的话，就将消息发送给该队列。

RabbitMQ 默认实现了一个名称为空的 direct 交换器，当声明一个队列时，如果没有指定交换器，那么 RabbitMQ 会把该队列自动绑定到这个默认的交换器，并以队列名称作为路由键。

在 RabbitMQ 中，支持在一个交换器上的多个队列配置相同的路由键。也就是说，对于绑定到交换器 Exchang_A 上的队列 Queue_1 和 Queue_2，可以设置同一个 routing_key（假设为 key_test）。当设置了 routing_key 为 key_test 的消息 Message 被发布到 Exchang_A 上时，Exchang_A 会将 Message 同时发送给 Queue_1 和 Queue_2 两个队列。

### fanout

设置为 fanout 的交换器，会将消息发送给所有绑定到它身上的队列，类似于广播。

通常应用于需要对一条消息做不同反应的场景中。比如，在社交网站上，如果用户上传了一张照片，在更新用户相册的同时，还需要给用户一些积分奖励。那么这种情况，就可以使用 fanout 类型的交换器来实现。只需要将更新用户相册的队列和增加用户积分的队列绑定到同一个 fanout 交换器上即可。

### topic

topic 类型的交换器，可以使来自不同源头的消息到达同一个队列，即支持在路由键中使用通配符。

在 RabbitMQ 中，`.` 把路由键分成多个部分，`*` 匹配特定位置的任意文本，`#` 则表示匹配所有规则。通过对这几种通配符的组合使用，就可以实现将不同来源的消息发送到同一个队列。比如，将 routing_key 设置为 `*.error` ，就可以将所有 routing_key 以 `.error` 结尾的消息发送到同一个队列。

## 队列

queue，生产者发布的消息最终到达的地方，同时消费者从队列中消费消息。

### 接收消息

消费者主要通过两种方式从队列中接收消息：使用 basic.consume 和 basic.get 命令。

当消费者使用 basic.consume 订阅了某个队列后，一旦有消息到达该队列，RabbitMQ 就将消息立即发送给消费者，然后等待下一条消息的到来。

如果消费者使用的是 basic.get 命令，只会从队列中获取单条消息，无法持续获取。假如队列中堆积了 5 条消息，使用 basic.get 命令只会获得最开始的那条消息，后面的 4 条消息无法获取。

如果一个队列有多个消费者进行订阅，RabbitMQ 采用轮询的方式将消息发送给某个消费者，每条消息只发送给一个消费者。

也就是说，如果消费者 A、B、C订阅了同一个队列，那么第一条消息会发送给 A，第二条发送给 B，第三条发送给 C，第四条发送给 A，···，以此类推。

当消息被消费者消费了之后，RabbitMQ 就将该消息从队列中删除。

那么 RabbitMQ 怎么知道消息被消费者成功消费了呢？这就涉及到了消息的确认机制。

### 消息确认

消费者接收到的每条消息都必须进行确认，如果消费者没有对消息进行确认，那么 RabbitMQ 不会将下一条消息发送给该消费者，直到其对消息进行了确认。如果在消费者向 RabbitMQ 发送确认之前，消费者与 RabbitMQ 之间的连接断开了，那么 RabbitMQ 会将该消息发送给其他的消费者。

主要有两种确认方式：使用 basic.ack 命令向 RabbitMQ 发送确认，或者在订阅队列时将 auto_ack 参数设置为 true。

需要注意的是，如果设置了 auto_ack 为 true，那么一旦消费者接收到了消息，RabbitMQ 就认为确认了消息，从而将消息从队列中删除。但是消费者接收到消息并不等同于成功处理了消息，如果在成功处理该条消息之前出现问题或者程序崩溃，由于此时 RabbitMQ 已经将消息从队列中删除了，那么就意味着这条消息丢失了。

## 虚拟主机

vhost，简化版的 RabbitMQ 服务器，每一个 vhost 拥有自己的交换器、队列和绑定。更重要的是，它拥有自己的权限，不同的 vhost 之间是隔离的。可以将 vhost 想象成物理服务器上的虚拟机。

RabbitMQ 中默认的虚拟主机为：“/”。

## 消息持久化

默认情况下，如果 RabbitMQ 进行了重启，那么队列、交换器和其中的消息都会丢失。如果想要你的数据在重启后不丢失，那么就需要对消息进行持久化设置。主要操作如下：

- 将消息的投递模式（delivery mode）设置为 2（持久）。

- 将消息发送到持久化的交换器。

- 消息必须到达持久化的队列。


RabbitMQ 是通过将消息写入磁盘中的持久化日志中的方式实现消息的持久化的。如果持久化队列中的某条消息被消费了，那么 RabbitMQ 会在持久化日志中将该消息标记为等待垃圾收集。

## 管理 RabbitMQ

前面的部分介绍了一些 RabbitMQ 中比较重要的概念和消息的相关知识，接下来介绍如何对 RabbitMQ 进行管理。

首先需要明确一个概念，通常提到的 RabbitMQ 节点，实际上指的是 RabbitMQ 应用和所在的 Erlang 节点。RabbitMQ 是 Erlang 应用程序的一种。

启动 RabbitMQ 通常使用 `rabbitmq-server` 工具，但需要注意的是，使用该命令启动的包括 Erlang 节点和 RabbitMQ 应用。同时，还把 RabbitMQ 应用设置成了独立运行模式。

对于 RabbitMQ 应用的管理，通常使用 `rabbitmqctl` 工具：

- stop 参数：将本地节点干净的关闭，包括 RabbitMQ 应用和 Erlang 节点。同时，可以使用 `-n rabbit@hostname` 参数，关闭指定的远程节点。

- stop_app 参数：只关闭 RabbitMQ 应用。

- start_app 参数：只启动 RabbitMQ 应用。


## 集群

对于 RabbitMQ 的内建集群，主要用于完成两个设计目标：

- 允许消费者和生产者在节点崩溃的情况下继续运行。
- 通过添加更多的节点来线性扩展消息通信吞吐量。

在默认情况下，如果集群中某个节点崩溃了，那么在该节点上队列上的消息也就丢失了，因为 RabbitMQ 不会将节点上的队列复制到整个集群中。

不论是在单节点系统中还是集群，对于 RabbitMQ 节点来说，要么是内存节点，要么是磁盘节点。两者间的主要区别：

- 内存节点：所有队列、交换器、绑定、用户、权限和 vhost 的元数据定义都只是存储在内存中。

- 磁盘节点：所有的元数据信息存储在磁盘中。对于单节点系统，只允许节点为磁盘节点。


当在集群中声明交换器、队列和绑定时，这些操作会等到集群中所有节点都成功提交元数据后才返回。

在 RabbitMQ 集群中，要求至少有一个磁盘节点，当有节点加入或离开时，需要将该变更通知到至少一个磁盘节点。

## 安装

分别介绍单节点和集群的安装部署。

### 单节点

---

#### 依赖

RabbitMQ 是使用 Erlang 编写的，因此需要安装 Erlang 库，以便运行 RabbitMQ。

#### 安装 Erlang

##### 下载 Erlang 源码

点击 [这里](http://erlang.org/download/otp_src_19.3.tar.gz) 下载 Erlang 源码包，并解压至指定目录。

##### 配置 ERL_TOP

```shell
export ERL_TOP=`pwd`
```

##### 编译

使用以下命令进行编译：
```shell
./configure --prefix=/path/to/install/erlang
make
make install
```	

##### 设置环境变量

编译完成后，设置 ERL_HOME。编辑 /etc/profile 文件，增加以下内容：
```shell
export ERL_HOME=/path/to/install/erlang
export PATH=$PATH:$ERL_HOME/bin
```

##### 验证

执行命令：`erl`，可以进入 Erlang 环境，证明安装成功。

#### 下载 RabbitMQ

点击 [这里](https://www.rabbitmq.com/releases/rabbitmq-server/v2.7.0/rabbitmq-server-generic-unix-2.7.0.tar.gz) 下载 RabbitMQ Server 安装包，并解压至指定目录（如：`/path/to/install/rabbitmq-server`）。

#### 设置文件夹结构

创建两个目录：RabbitMQ 的日志目录和 Mnesia 数据库目录。RabbitMQ 使用 Mnesia 数据库存储服务器信息，比如队列元数据、虚拟主机等。
```shell
mkdir -p /var/log/rabbitmq
mkdir -p /var/lib/rabbitmq/mnesia/rabbit
```

#### 启动

执行命令：`/path/to/install/rabbitmq-server/sbin/rabbitmq-server` 启动 RabbitMQ Server。
出现以下提示信息，说明启动成功：
```shell
Activating RabbitMQ plugins ...

********************************************************************************
*WARNING* Undefined function global:safe_whereis_name/1
********************************************************************************

0 plugins activated:


+---+   +---+
|   |   |   |
|   |   |   |
|   |   |   |
|   +---+   +-------+
|                   |
| RabbitMQ  +---+   |
|           |   |   |
|   v2.7.0  +---+   |
|                   |
+-------------------+
AMQP 0-9-1 / 0-9 / 0-8
Copyright (C) 2007-2011 VMware, Inc.
Licensed under the MPL.  See http://www.rabbitmq.com/

node           : rabbit@172-23-8-23
app descriptor : /home/xuzhidan/bigdata/RabbitMQ/rabbitmq_server-2.7.0/sbin/../ebin/rabbit.app
home dir       : /root
config file(s) : (none)
cookie hash    : TJy1BzDGsu3ovCCTHWG7rw==
log            : /var/log/rabbitmq/rabbit@172-23-8-23.log
sasl log       : /var/log/rabbitmq/rabbit@172-23-8-23-sasl.log
database dir   : /var/lib/rabbitmq/mnesia/rabbit@172-23-8-23
erlang version : 8.3

-- rabbit boot start
starting file handle cache server                                     ...done
starting worker pool                                                  ...done
starting database                                                     ...done
starting codec correctness check                                      ...done
-- external infrastructure ready
starting plugin registry                                              ...done
starting auth mechanism cr-demo                                       ...done
starting statistics event manager                                     ...done
starting logging server                                               ...done
starting auth mechanism amqplain                                      ...done
starting auth mechanism plain                                         ...done
starting exchange type direct                                         ...done
starting exchange type fanout                                         ...done
starting exchange type headers                                        ...done
starting exchange type topic                                          ...done
-- kernel ready
starting node monitor                                                 ...done
starting cluster delegate                                             ...done
starting guid generator                                               ...done
starting alarm handler                                                ...done
starting memory monitor                                               ...done
-- core initialized
starting empty DB check                                               ...done
starting exchange, queue and binding recovery                         ...done
starting mirror queue slave sup                                       ...done
starting adding mirrors to queues                                     ...done
-- message delivery logic ready
starting error log relay                                              ...done
starting networking                                                   ...done
starting direct_client                                                ...done
starting notify cluster nodes                                         ...done

broker running
```

### 集群

---

需要在集群中的所有节点上安装 RabbitMQ，具体安装步骤参照上面单节点的安装过程。

#### 同步 cookie 文件

RabbitMQ 集群是通过 Erlang 的集群实现的，当集群中的节点进行通信时，Erlang 节点会进行认证。如果节点之间的 Erlang cookie 不相同，则会认证失败。因此，需要在集群中的所有节点上同步 cookie 文件。

##### 拷贝 cookie

如果是通过上面介绍的单节点的方式安装的 RabbitMQ，则 Erlang 的 cookie 文件路径为：`/root/.erlang.cookie`。

将集群中任意一个节点上的 cookie 文件拷贝到其他的节点上，进行替换。

##### 修改权限

RabbitMQ 在启动每个节点时，会检查节点上的 cookie 文件的权限，必须为 400，否则会报错。因此需要将每个节点上的 cookie 文件的权限修改为 400 。

#### 配置主机名

##### 设置主机名

RabbitMQ 是通过主机名来对节点进行管理的，因此需要对集群中的每个节点都设置一个唯一的主机名。

可通过修改配置文件 `/etc/sysconfig/network` 永久修改主机名。

##### 添加 IP 主机名映射

修改节点上的 `/etc/hosts` 文件，添加集群中所有节点的IP和主机名映射，并在集群中同步。

#### 启动集群

假设集群中有三个节点：host1、host2和host3，通过以下步骤将整个集群启动。

##### 启动服务

在 host1 上执行以下命令，启动服务：
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmq-server -detached
```

在 host2 和 host3 上执行以下命令，启动服务，并重置 RabbitMQ 应用：
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmq-server -detached
sh sbin/rabbitmqctl stop_app
sh sbin/rabbitmqctl reset
```

##### 加入集群

在 host2 和 host3 上分别执行以下命令，加入到集群中
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmqctl cluster rabbit@host1 rabbit@host2
```

其中，host1 和 host2 作为集群中的磁盘节点，host3 作为集群中的内存节点。

##### 启动 RabbitMQ 应用

在 host2 和 host 3 上分别执行以下命令，启动 RabbitMQ 应用程序：
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmqctl start_app
```

##### 查看状态

执行以下命令，查看集群的状态：
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmqctl cluster_status
```
显示以下信息，说明集群部署成功：
```shell
[root@172-23-8-24 rabbitmq_server-2.7.0]# sbin/rabbitmqctl cluster_status
Cluster status of node 'rabbit@172-23-8-24' ...
[{nodes,[{disc,['rabbit@172-23-8-24','rabbit@172-23-8-23']}]},
 {running_nodes,['rabbit@172-23-8-23','rabbit@172-23-8-24']}]
...done.
```