# ZAB 协议原理介绍

标签：ZAB Leader选举

---

## 概述

在分布式系统中，对于数据一致性的问题，Paxos 算法是比较主流的解决思路，像谷歌的 Chubby 就是基于 Paxos 算法的思想实现的。但在目前使用比较广泛的开源分布式锁服务 ZooKeeper 中，并没有使用采用 Paxos 算法，而是基于 ZAB 协议实现的。

ZAB（ZooKeeper Atomic Broadcast）协议是专门为 ZooKeeper 设计的一种支持崩溃恢复的原子广播协议，与 Paxos 算法最大的区别就是增加了崩溃恢复的功能。

从整体上来看，ZAB 协议包括两种模式：崩溃恢复模式和消息广播模式，更进一步，可以划分为三个阶段：发现、同步和广播。

下面，依次对这三个阶段进行详细的介绍。

## 发现

该阶段对应于 ZooKeeper 中就是 Leader 的选举过程。

当整个服务框架在启动时，或者集群中的 Leader 出现网络中断、崩溃退出或异常重启时，就会进入该阶段，也就是进行 Leader 选举。

进行 Leader 选举时分两种情况：集群启动和 Leader 重新选举。

在介绍具体的选举过程之前，先对在选举过程中涉及到的一些基本术语进行简单的说明。

### 基本术语

#### zxid

ZooKeeper 的事务 id，全局唯一，每次 ZooKeeper 的状态进行更新的时候，zxid 都会增加。也就是说，zxid 的值越大，说明对应的 ZooKeeper 集群的数据是越新的。

zxid 的值有64位，其中高 32 位是 epoch 的值，低 32 位代表的是在同一个 Leader 周期内的事务 id。

#### epoch

zxid 的高 32 位的值，主要用来标识不同的 Leader 周期。

因为在 ZooKeeper 集群的运行过程中，每个服务器都有可能由于各种原因崩溃或下线，其中当然也会包括 Leader 服务器。因此肯定为经历多次的 Leader 选举，而 epoch 的值就是为了区分不同的 Leader 周期，以便于选举之后的数据同步。

有关同步的过程会在后面进行介绍。

#### server id

ZooKeeper 集群中每个服务器都有一个全局唯一的 server id，用来标识不同的服务器。

### 选举过程

在整个 ZooKeeper 集群启动或 Leader 崩溃需要重新选举时，需要选举出一个 Leader 服务器来对外提供服务。选举 Leader 的大体过程如下：

- 每个 Follower 都向其他所有的节点发送投票信息，选举自己为 Leader。投票中主要包含的信息为 zxid 和 server id。
- 同时，每个 Follower 都会在本地保存一个已收到的投票集合，保存了收到的投票信息。
- Follower 收到其他节点的投票信息后，会跟自身进行相应的比较：首先比较 zxid 的大小，如果相等，再比较 server id 的大小。如果收到的投票的 zxid 或 server id 比自己的大，那么更新本地已收到的投票信息。
- 如果本地收到的投票信息中已经有某个节点获得了半数以上的投票，发送投票，将该节点选为 Leader，然后停止本节点的选举过程。

## 同步

当集群中的 Leader 被选举出来之后，集群中其他的 Follower 节点需要跟 Leader 做数据同步，保证数据的一致性。

大体流程为：
- 新选举出来的 Leader 向所有的 Follower 发送最新的 epoch。
- 每个 Follower 接收到 Leader 发送的 epoch 值后，如果发现 epoch 和自己的值相同，则进行数据同步操作，实际上就是执行一系列的事务操作。
- 当 Leader 收到超过半数的 Follower 的确认反馈后，向所有 Follower 发送 commit 消息。
- 到 Follower 接收到 commit 消息后，会依次处理并提交所有未处理的事务。

## 广播

在 ZooKeeper 集群完成同步后，整个集群就可以对外提供服务，也就是可以接收客户端的事务请求了。此时就进入了广播阶段。

ZooKeeper 进行消息广播的流程如下：
- 当 Leader 接收到来自客户端的事务请求时，会生成对应的事务，然后根据 zxid 的顺序将每个事务发送给所有的 Follower。
- Follower 根据接收到的消息顺序，依次处理事务。事务处理完成后，将结果反馈给 Leader。
- 当 Leader 收到超过半数的 Follower 的确认信息后，就会向所有的 Follower 发送 commit 消息。
- Follower 接收到 Leader 的 commit 消息后，就会进行事务的提交。