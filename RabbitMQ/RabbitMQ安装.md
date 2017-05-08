# RabbitMQ 安装

标签：RabbitMQ

---

## 单节点

### 依赖

---

RabbitMQ 是使用 Erlang 编写的，因此需要安装 Erlang 库，以便运行 RabbitMQ。

### 安装 Erlang

---

#### 下载 Erlang 源码

点击 [这里](http://erlang.org/download/otp_src_19.3.tar.gz) 下载 Erlang 源码包，并解压至指定目录。

#### 配置 ERL_TOP

```shell
export ERL_TOP=`pwd`
```

#### 编译

使用以下命令进行编译：
```shell
./configure --prefix=/path/to/install/erlang
make
make install
```	

#### 设置环境变量

编译完成后，设置 ERL_HOME。编辑 /etc/profile 文件，增加以下内容：
```shell
export ERL_HOME=/path/to/install/erlang
export PATH=$PATH:$ERL_HOME/bin
```

#### 验证

执行命令：`erl`，可以进入 Erlang 环境，证明安装成功。

	

### 下载 RabbitMQ

---

点击 [这里](https://www.rabbitmq.com/releases/rabbitmq-server/v2.7.0/rabbitmq-server-generic-unix-2.7.0.tar.gz) 下载 RabbitMQ Server 安装包，并解压至指定目录（如：`/path/to/install/rabbitmq-server`）。

### 设置文件夹结构

---

创建两个目录：RabbitMQ 的日志目录和 Mnesia 数据库目录。RabbitMQ 使用 Mnesia 数据库存储服务器信息，比如队列元数据、虚拟主机等。
```shell
mkdir -p /var/log/rabbitmq
mkdir -p /var/lib/rabbitmq/mnesia/rabbit
```

### 启动

---

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

## 集群

需要在集群中的所有节点上安装 RabbitMQ，具体安装步骤参照上面单节点的安装过程。

### 同步 cookie 文件

---

RabbitMQ 集群是通过 Erlang 的集群实现的，当集群中的节点进行通信时，Erlang 节点会进行认证。如果节点之间的 Erlang cookie 不相同，则会认证失败。因此，需要在集群中的所有节点上同步 cookie 文件。

#### 拷贝 cookie

如果是通过上面介绍的单节点的方式安装的 RabbitMQ，则 Erlang 的 cookie 文件路径为：`/root/.erlang.cookie`。

将集群中任意一个节点上的 cookie 文件拷贝到其他的节点上，进行替换。

#### 修改权限

RabbitMQ 在启动每个节点时，会检查节点上的 cookie 文件的权限，必须为 400，否则会报错。因此需要将每个节点上的 cookie 文件的权限修改为 400 。

### 配置主机名

---

#### 设置主机名

RabbitMQ 是通过主机名来对节点进行管理的，因此需要对集群中的每个节点都设置一个唯一的主机名。

可通过修改配置文件 `/etc/sysconfig/network` 永久修改主机名。

#### 添加 IP 主机名映射

修改节点上的 `/etc/hosts` 文件，添加集群中所有节点的IP和主机名映射，并在集群中同步。

### 启动集群

---

假设集群中有三个节点：host1、host2和host3，通过以下步骤将整个集群启动。

#### 启动服务

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

#### 加入集群

在 host2 和 host3 上分别执行以下命令，加入到集群中
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmqctl cluster rabbit@host1 rabbit@host2
```

其中，host1 和 host2 作为集群中的磁盘节点，host3 作为集群中的内存节点。

#### 启动 RabbitMQ 应用

在 host2 和 host 3 上分别执行以下命令，启动 RabbitMQ 应用程序：
```shell
cd /path/to/install/rabbitmq-server 
sh sbin/rabbitmqctl start_app
```

#### 查看状态

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