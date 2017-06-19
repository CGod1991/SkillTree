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

### 自定义数据

open-falcon 支持用户将自定义的监控数据进行上报。Agent 组件提供了 API，用户可以将自己收集的监控数据 push 到 Agent 的接口中，由 Agent 进行上报。

自定义的数据要以 json 的格式进行推送，同时，有些字段是要必须指定的。主要是以下七个字段：
- metric: 最核心的字段，代表这个采集项具体度量的是什么, 比如是 cpu_idle 呢，还是 memory_free, 还是 qps。 
- endpoint: 标明 metric 的主体(属主)，比如 metric 是 cpu_idle，那么 Endpoint 就表示这是哪台机器的 cpu_idle。
- timestamp: 表示汇报该数据时的 unix 时间戳，注意是整数，代表的是秒。
- value: 代表该 metric 在当前时间点的值，float64。
- step: 表示该数据采集项的汇报周期，这对于后续的配置监控策略很重要，必须明确指定。
- counterType: 只能是 COUNTER 或者 GAUGE 二选一，前者表示该数据采集项为计时器类型，后者表示其为原值 (注意大小写)。
	- GAUGE：即用户上传什么样的值，就原封不动的存储
	- COUNTER：指标在存储和展现的时候，会被计算为 speed，即（当前值 - 上次值）/ 时间间隔
- tags: 一组逗号分割的键值对, 对 metric 进一步描述和细化, 可以是空字符串. 比如 idc=lg，比如 service=xbox 等，多个 tag 之间用逗号分割。

下面是一个自定义数据 push 到 open-falcon 的例子，python 实现：
```python
#!-*- coding:utf8 -*-

import requests
import time
import json

ts = int(time.time())
payload = [
    {
        "endpoint": "test-endpoint",
        "metric": "test-metric",
        "timestamp": ts,
        "step": 60,
        "value": 1,
        "counterType": "GAUGE",
        "tags": "idc=lg,loc=beijing",
    },

    {
        "endpoint": "test-endpoint",
        "metric": "test-metric2",
        "timestamp": ts,
        "step": 60,
        "value": 2,
        "counterType": "GAUGE",
        "tags": "idc=lg,loc=beijing",
    },
]

r = requests.post("http://127.0.0.1:1988/v1/push", data=json.dumps(payload))

print r.text
```

## 自定义数据源

在 open-falcon 中，绘图组件和报警组件之间是没有依赖的，如果用户只想用报警功能，那么可以只安装报警组件，然后接入自己的数据源即可。

Transfer 组件有开通 rpc 端口，用户只要将自己的数据发送到该端口，Transfer 就可以接收并转发给 Judge 组件。数据格式为 json rpc
格式。

下面是一个简单的通过 python 客户端向 Transfer 发送数据的例子：
```python
import json
import socket
import itertools
import time
 
class RPCClient(object):
 
    def __init__(self, addr, codec=json):
        self._socket = socket.create_connection(addr)
        self._id_iter = itertools.count()
        self._codec = codec
 
    def _message(self, name, *params):
        return dict(id=self._id_iter.next(),
                    params=list(params),
                    method=name)
 
    def call(self, name, *params):
        req = self._message(name, *params)
        id = req.get('id')
 
        mesg = self._codec.dumps(req)
        self._socket.sendall(mesg)
 
        # This will actually have to loop if resp is bigger
        resp = self._socket.recv(4096)
        resp = self._codec.loads(resp)
 
        if resp.get('id') != id:
            raise Exception("expected id=%s, received id=%s: %s"
                            %(id, resp.get('id'), resp.get('error')))
 
        if resp.get('error') is not None:
            raise Exception(resp.get('error'))
 
        return resp.get('result')
 
    def close(self):
        self._socket.close()
 

if __name__ == '__main__':
    rpc = RPCClient(("127.0.0.1", 8433))
    for i in xrange(10000):
        mv1 = dict(endpoint='host.niean', metric='metric.niean.1', value=i, step=60, 
            counterType='GAUGE', tags='tag=t'+str(i), timestamp=int(time.time()))
        mv2 = dict(endpoint='host.niean', metric='metric.niean.2', value=i, step=60, 
            counterType='COUNTER', tags='tag=t'+str(i), timestamp=int(time.time()))
        print rpc.call("Transfer.Update", [mv1, mv2])
```

