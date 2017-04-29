# 使用 IntelliJ IDEA 调试 HDFS

标签：Hadoop IntelliJ IDEA

## 前言

最近开始慢慢的将开发环境切换至 IntelliJ IDEA，所以花了点时间整理了下在 IDEA 上设置 Hadoop HDFS 调试环境。

## 环境信息

Ubuntu 14
Hadoop 2.6.0
IntelliJ IDEA 15
JDK 1.7
Maven 3.0.5

## 编译 Hadoop

### 安装依赖的组件

使用以下命令安装编译所需要的组件：

> sudo apt-get -y install maven build-essential autoconf automake libtool cmake zlib1g-dev pkg-config libssl-dev libfuse-dev protobuf-compiler

### 下载源码

点击 [这里](https://archive.apache.org/dist/hadoop/common/hadoop-2.6.0/hadoop-2.6.0-src.tar.gz) 下载 Hadoop 2.6.0 源码。

下载完成后，将源码解压至指定目录（假设为：/home/bigdata），则 Hadoop 源码路径为：/home/bigdata/hadoop-2.6.0-src 。

### 编译

进入 Hadoop 源码目录（/home/bigdata/hadoop-2.6.0-src ），执行以下命令进行编译：

> mvn clean install -Pdist -DskipTests

编译的时间取决于网络的情况，这个时候可以走开休息一会。

## 配置环境变量

编译完成之后，需要配置环境变量才能保证 HDFS 的正常启动。调试 HDFS 只需要两个目录中的内容：/home/bigdata/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/target/hadoop-common-2.6.0 和 /home/bigdata/hadoop-2.6.0-src/hadoop-hdfs-project/hadoop-hdfs/target/hadoop-hdfs-2.6.0 ,修改你的用户目录下的 .bashrc 文件，增加以下内容：
```shell
export JAVA_HOME=/your/jdk/path
export HADOOP_COMMON_HOME=/home/bigdata/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/target/hadoop-common-2.6.0
export HADOOP_HDFS_HOME=/home/bigdata/hadoop-2.6.0-src/hadoop-hdfs-project/hadoop-hdfs/target/hadoop-hdfs-2.6.0
export HADOOP_YARN_HOME=/home/bigdata/hadoop-2.6.0-src
export HADOOP_MAPRED_HOME=/home/bigdata/hadoop-2.6.0-src
export HADOOP_CONF_DIR=$HADOOP_COMMON_HOME/etc/hadoop
export PATH=$PATH:$HADOOP_COMMON_HOME/bin:$HADOOP_HDFS_HOME/bin:$HADOOP_COMMON_HOME/sbin:$HADOOP_HDFS_HOME/sbin
export PATH=$JAVA_HOME/bin:$PATH
```

修改 /home/bigdata/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/target/hadoop-common-2.6.0/etc/hadoop/core-site.xml 配置文件，增加以下内容：
```shell
<configuration>
<property>
  <name>fs.defaultFS</name>
  <value>hdfs://localhost/</value>
  <description>The name of the default file system. </description>
</property>
</configuration>
```

## 运行 HDFS

先执行命令 `hdfs namenode -format` 格式化 HDFS 系统，然后执行 `hdfs namenode` 和 `hdfs datanode` 分别启动 NameNode 和 DataNode 进程。
