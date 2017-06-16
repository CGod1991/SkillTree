# open-falcon 介绍

标签：监控 告警

---

## 基本组件

### Agent

部署在目标服务器上，负责采集监控项指标数据，然后将采集到的数据上报给 Transfer。

### Transfer

数据接收端，只做数据的转发，将数据转发给后端的 Graph 和 Judge。

### Graph

负责数据的实际存储。当接收到 Transfer 转发的数据后，操作 rrd 文件来存储监控数据。

### Query

负责查询各个 Graph 的数据，对外提供了统一的 HTTP 查询接口。

### Dashboard

一个 Web 客户端，主要用于查询被监控项的历史趋势图。

### Task

负责一些定时任务，比如索引全量更新、垃圾索引清理、自身组件监控等。

### Sender

报警发送模块，负责从 redis 中读取报警短信或邮件，然后调用短信或邮件发送接口，进行发送。

### UIC（FE）

负责用户组管理。UIC 是用 Java 实现的，FE 是用 GO 重新实现了 UIC。

### Portal

使用 Python 实现的 Web 前端，主要用于配置报警策略，管理机器分组。

### HeartBeat Server

心跳服务器，负责将 Agent 上报的心跳信息保存到数据库中，同时会定时从数据库中下载监控项信息、插件信息和报警策略数据，以供 Agent 和 Judge 的查询请求。

### Judge

用于判断是否出发报警条件。

Judge 先从 HeartBeat Server 获取所有的报警策略列表，然后等待 Transfer 转发的数据。每收到一条 Transfer 转发过来的数据，就进行阈值判断。如果触发了报警条件，Judge 会产生相应的报警 event，然后将 event 写入 Alarm 的 redis 队列中。

### Alarm

报警事件处理组件。

Judge 把报警 event 写入 redis 中，Alarm 从 redis 中读取这些 event 做相应的处理，比如发送短信、邮件，或者回调某个 HTTP 地址。

如果需要发送短信或邮件，Alarm 将生成的短信或邮件写入 redis 中的队列，由 Sender 组件负责从这些队列中读取短信或邮件内容并进行发送。

### Links

报警合并依赖的 Web 端，存放报警详情信息。

## 架构

open-falcon 中的所有组件大体上可以分为两类：绘图组件和报警组件。这两类组件都可以独立安装运行，相互之间没有依赖。

### 绘图组件

主要包括：Transfer、Graph、Query、Dashboard。

数据流如下：
- Agent 组件采集到监控项的数据后，将数据上报给 Transfer。
- Transfer 收到 Agent 上报的数据后，将数据转发一份给 Graph。
- Graph 接收到 Transfer 转发的数据后，将数据以 rrd 的格式进行存储。
- Query 对外提供了统一的 HTTP 接口，当用户调用该接口查询数据时，Query 会去 Graph 中查询数据。

### 报警组件

主要包括：HeartBeat Server、Portal、Judge、Alarm、Sender。

报警流程大体如下：
- 用户首先在 Portal 中配置报警策略。
- Judge 通过 HeartBeat Server 获取所有的报警策略。
- Transfer 会将 Agent 上报的数据也转发一份给 Judge，Judge 每收到一条 Transfer 转发的数据，就会进行阈值判断，是否触发报警条件。
- 如果触发了报警条件，Judge 会生成相应的报警 event，然后将这些 event 发送到 Alarm 的 redis 队列中。
- Alarm 从 redis 的队列中读取报警 event，进行相应的处理。比如：发送短信、发送邮件或者调用 callback。如果要发送短信或邮件，Alarm 并不负责实际的发送操作，而是把短信或邮件的内容发送到 redis 中的对应队列中，由 Sender 执行具体的发送操作。
- Sender 会从 redis 的队列中读取短信或邮件的内容，然后调用用户实现的短信或邮件发送接口，执行具体的发送操作。

## 问题

1、 使用命令`./env/bin/pip install -r pip_requirements.txt`安装 portal 组件时报错：
	
```shell
  Downloading/unpacking Flask==0.10.1 (from -r pip_requirements.txt (line 1))
  Could not fetch URL https://pypi.python.org/simple/Flask/: There was a problem confirming the ssl certificate: <urlopen error [Errno 1] _ssl.c:492: error:14090086:SSL routines:SSL3_GET_SERVER_CERTIFICATE:certificate verify failed>
  Will skip URL https://pypi.python.org/simple/Flask/ when looking for download links for Flask==0.10.1 (from -r pip_requirements.txt (line 1))
  Could not fetch URL https://pypi.python.org/simple/: There was a problem confirming the ssl certificate: <urlopen error [Errno 1] _ssl.c:492: error:14090086:SSL routines:SSL3_GET_SERVER_CERTIFICATE:certificate verify failed>
  Will skip URL https://pypi.python.org/simple/ when looking for download links for Flask==0.10.1 (from -r pip_requirements.txt (line 1))
  Cannot fetch index base URL https://pypi.python.org/simple/
  Could not fetch URL https://pypi.python.org/simple/Flask/: There was a problem confirming the ssl certificate: <urlopen error [Errno 1] _ssl.c:492: error:14090086:SSL routines:SSL3_GET_SERVER_CERTIFICATE:certificate verify failed>
  Will skip URL https://pypi.python.org/simple/Flask/ when looking for download links for Flask==0.10.1 (from -r pip_requirements.txt (line 1))
  Could not fetch URL https://pypi.python.org/simple/Flask/0.10.1: There was a problem confirming the ssl certificate: <urlopen error [Errno 1] _ssl.c:492: error:14090086:SSL routines:SSL3_GET_SERVER_CERTIFICATE:certificate verify failed>
  Will skip URL https://pypi.python.org/simple/Flask/0.10.1 when looking for download links for Flask==0.10.1 (from -r pip_requirements.txt (line 1))
  Could not find any downloads that satisfy the requirement Flask==0.10.1 (from -r pip_requirements.txt (line 1))
Cleaning up...
No distributions at all found for Flask==0.10.1 (from -r pip_requirements.txt (line 1))
Storing complete log in /root/.pip/pip.log
```
解决方法：修改安装命令，使用`./env/bin/pip install -i http://pypi.python.org/simple -r pip_requirements.txt`进行安装。