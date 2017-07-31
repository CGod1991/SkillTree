# YARN 日志

标签：YARN 日志

---

## YARN 中常见目录或文件名

### applicationId

在 YARN 中对作业的唯一标识。

格式：`application_${clusterStartTime}_${applicationId}`

例子：`application_1498552288473_2742`

### jobId

MapReduce 中对作业的唯一标识。

格式：`job_${clusterStartTime}_${jobId}`

例子：`job_1498552288473_2742`

### taskId

MapReduce 中某个作业中的某个任务的唯一标识。

格式：`task_${clusterStartTime}_${applicationId}_[m|r]_${taskId}`

例子：`task_1498552288473_2742_m_000000`、`task_1498552288473_2742_r_000000`

### attempId

任务尝试执行一次的 id。

格式：`attemp_${clusterStartTime}_${applicationId}_[m|r]_${taskId}_${attempId}`

例子：`attempt_1498552288473_2742_m_000000_0`

### appAttempId

ApplicationMaster 尝试执行一次的 id。

格式：`appattemp_${clusterStartTime}_${applicationId}_${appAttempId}`

例子：`appattempt_1498552288473_2742_000001`

### containerId

container 的 id

格式：`container_e*epoch*_${clusterStartTime}_${applicationId}_${appAttempId}_${containerId}`

例子：`container_e20_1498552288473_2742_01_000032` 、 `container_1498552288473_2742_01_000032`

## 服务日志

诸如 ResourceManager 和 NodeManager 这种系统自带的服务的输出日志，默认是存放在 `${HADOOP_HOME}/logs` 目录下的，此参数可以通过配置 `yarn-env.sh` 配置文件中的 `YARN_LOG_DIR` 来指定。

ResourceManager 的输出日志格式为：`yarn-${USER}-resourcemanager-${HOSTNAME}.log`，其中 ${USER} 为启动 ResourceManager 服务的用户名，${HOSTNAME} 为所在服务器的主机名。

NodeManager 的输出日志格式为：`yarn-${USER}-nodemanager-${HOSTNAME}.log` 。

需要注意 `*.log` 文件和 `*.out` 文件的区别：
- `*.log` 中存放的是 log4j 的输出日志。
- `*.out` 中存放的是 stdout 和 stderr 的输出日志。

## 作业统计日志

在每个作业的历史日志中都包含了一个作业用了多少个 Map、用了多少个 Reduce、作业的提交时间、作业的启动时间、作业的完成时间等信息。

JobHistoryServer 会读取作业的统计日志。

## 作业运行日志

主要指 ApplicationMaster 和 普通 Task 日志的信息，包括 container 的启动脚本和 container 的运行日志。

默认情况，该日志的存放路径为 ${HADOOP_HOME}/logs/userlogs。可以通过属性 `yarn.nodemanager.log-dirs` 进行配置。 

## 日志聚集

日志聚集是 YARN 提供的日志中央化管理功能，它能将运行完成的 Container 或任务日志上传到 HDFS 上，从而减轻 NodeManager 的负载，并且提供一个中央化的存储和分析机制。

默认情况下，Container 或任务日志保存在各个 NodeManager 上。如果启用了日志聚集功能，当 YARN 把日志上传到 HDFS 上之后，各个 NodeManager 上的对应日志就会被删除。



