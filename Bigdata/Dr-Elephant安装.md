# Dr.Elephant 安装

标签：Dr.Elephant

---

## 简介

Dr.Elephant 是一个对 Hadoop 和 Spark 任务进行性能监控和调优的工具，它能够自动收集 Hadoop 平台所有的度量标准，并对收集的数据进行分析，将分析结果以一种简单且易于理解的形式展示出来。

Dr.Elephant 的设计目的是通过它对任务的分析结果知道 Hadoop 或 Spark 开发者对其任务进行便捷的优化，从而提高开发者的效率以及 Hadoop 集群的使用效率。

## 编译

Dr.Elephant 的编译需要依赖 JDK 1.8、Play 和 SBT，因此需要先确保编译环境中已经安装了上述组件。

JDK 1.8 的安装比较简单，就不做介绍了，下面简单介绍下 Play 和 SBT 的安装过程。

### 安装 Play

需要注意的是，Dr.Elephant 使用了 playframework 中的 play 命令，而在 Play 2.3.0 版本之后已经取消了 play 命令，因此需要下载 2.3.0 之前版本的 Play。

从 [这里](https://downloads.typesafe.com/play/2.2.6/play-2.2.6.zip) 下载 Play 2.2.6 。

Paly 的安装比较简单，下载完成后，将安装包解压，然后将解压后的路径加入到 $PATH 中即可。

### 安装 SBT

可以通过 yum 来安装 SBT。

首先需要增加 SBT yum 源。在 /etc/yum.repos.d 目录下新增文件 bintray-sbt-rpm.repo，文件内容如下：
```shell
#bintray--sbt-rpm - packages by  from Bintray
[bintray--sbt-rpm]
name=bintray--sbt-rpm
baseurl=https://sbt.bintray.com/rpm
gpgcheck=0
repo_gpgcheck=0
enabled=1
```

然后即可通过 yum 命令安装 SBT。

### 下载

从 [这里](https://github.com/linkedin/dr-elephant) 下载 GitHub 上最新的源码。

### 修改配置

源码下载完成后，进入 $dr-elephant 目录，修改 compile.sh 文件，找到以下内容：
```shell
play_command $OPTS clean test compile dist
```
然后将其修改为：
```shell
play_command $OPTS clean compile dist
```

修改 compile.conf 文件，修改后的内容如下：
```shell
hadoop_version=2.6.0
spark_version=1.6.0
```

### 编译

进入 $dr-elephant 目录，执行以下命令进行编译：
```shell
./compile.sh compile.conf
```

出现类似以下的信息表示编译成功：
```shell
+ mv dr-elephant-2.0.6.zip /root/dr-elephant/dist/
```
编译生成的安装包路径为 /root/dr-elephant/dist/dr-elephant-2.0.6.zip 。

## 安装

### 环境要求

由于 Dr.Elephant 是通过 Hadoop 的 Resource Manager 和 Job History Server 来收集任务运行的信息，因此需要集群中 Hadoop 的版本为 2.x，而且需要启动 Resource Manager 和 Job History
Server。

同时，Dr.Elephant 将元数据和分析的结果保存在关系型数据库中，因此需要提供一个数据库服务。通常使用 mysql，但有一点需要注意，mysql 的版本必须是 5.5+。

### 部署过程

1. 将安装包在需要部署 Dr.Elephant 的机器上解压，修改 $dr-elephant/app-conf/elephant.conf,主要修改有关数据库的配置：
```shell
db_url=127.0.0.1
db_name=drelephant
db_user=drelephant
db_password=drelephant
```
2. 在 mysql 中创建上面配置的数据库，并给对应的用户授权。授权命令如下：
```shell
grant all privileges on drelephant.* to drelephant@"127.0.0.1" identified by "drelephant";
flush privileges;
```
3. 在数据库 drelephant 中创建表 play_evolutions，建表语句如下：
```shell
create table play_evolutions (
                      id int not null primary key, hash varchar(255) not null, 
                      applied_at timestamp, 
                      apply_script text, 
                      revert_script text, 
                      state varchar(255), 
                      last_problem text
                  );
```
4. 由于 Dr.Elephant 使用 `hadoop classpath` 命令来加载类，所以需要将以下变量添加到系统变量中：
```shell
export HADOOP_HOME=/path/to/hadoop/home
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$HADOOP_HOME/bin:$PATH
```
5. 修改sql文件：修改 $dr-elephant/conf/evolutions/default/1.sql,找到以下内容：
```shell
create index yarn_app_result_i4 on yarn_app_result (flow_exec_id);
create index yarn_app_result_i5 on yarn_app_result (job_def_id);
create index yarn_app_result_i6 on yarn_app_result (flow_def_id);
```
修改成以下内容：
```shell
create index yarn_app_result_i4 on yarn_app_result (flow_exec_id(100));
create index yarn_app_result_i5 on yarn_app_result (job_def_id(100));
create index yarn_app_result_i6 on yarn_app_result (flow_def_id(100));
```
6. 执行以下命令启动 Dr.Elephant：
```shell
$dr-elephant/bin/start.sh $dr-elephant/app-conf
```

启动成功后，可通过 http://ip:8080 访问页面。

## 问题

1. 现象：
启动失败，查看日志 dr.log，显示如下异常信息：
```shell
c.j.b.h.AbstractConnectionHook - Failed to obtain initial connection Sleeping for 0ms and trying again. Attempts l
eft: 0. Exception: null.Message:Unknown system variable 'language'
Oops, cannot start the server.
Configuration error: Configuration error[Cannot connect to database [default]]
```
问题原因：
主要是由于 mysql 驱动的版本不一致导致的。
解决方法：
将集群中使用的 mysql 驱动拷贝到 Dr.Elephant 的 lib 目录下。
需要注意的是，驱动名称需要和原先的驱动名称保持一致，否则会报`Driver not found: [com.mysql.jdbc.Driver]` 异常。如原先的驱动名称为 `mysql.mysql-connector-java-5.1.36.jar`,则需要将新的驱动 jar 包也重命名为该名称。
