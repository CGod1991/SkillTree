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