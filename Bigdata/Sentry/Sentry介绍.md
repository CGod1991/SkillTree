# Apache Sentry 介绍

标签：Apache Sentry

---

- Apache Sentry 的目标是实现授权管理，它是一个策略引擎，被数据处理工具用来验证访问权限。
- Apache Sentry 提供了定义和持久化访问资源的策略的方法，这些策略可以存储在文件里或能使用 RPC 访问的数据库后端存储中。
- Apache Sentry 授权包括以下几个角色：
	- 资源：可能是服务器、数据库、表或者 URL（例如 HDFS 或本地路径）。
	- 权限：授权访问某一个资源的规则。
	- 角色：一系列权限的集合。
	- 用户和组：一个组是一系列用户的集合。Sentry 不能直接给一个用户或组授权，需要将权限授权给角色，角色可以授权给一个组而不是一个用户。

- 在 Sentry 中，所有的权限都只能授予角色，当角色挂载到用户组时，该组内的用户才具有相应的权限。

在 Sentry 的体系结构中有三个重要的组件：Binding、Policy Engine 和 Policy Provider。

## Binding

Binding 实现了对不同的查询引擎授权，Sentry 将自己的 Hook 函数插入到各 SQL 引擎的编译、执行的不同阶段。

这些 Hook 主要有两个作用：
- 起到过滤器的作用，只放行具有相应数据对象访问权限的 SQL 查询。
- 起到授权接管的作用，使用了 Sentry 之后，grant/revoke 的执行也完全在 Sentry 中实现。

对于所有引擎的授权信息存储在 Sentry 指定的统一的数据库中，这样所有引擎的权限就实现了集中管理。

## Policy Engine

Policy Engine 主要负责判定输入的权限要求与已保存的权限描述是否匹配。

## Policy Provider

Policy Provider 主要负责从文件或数据库中读取已设定好的访问权限。

## 权限控制

在 Sentry 中，所有对象的权限都是授予了角色，然后将角色与组进行关联，从而实现了对组内用户的权限控制。

也就是说，如果某个用户想要具有对某个对象（如数据库、表、路径）的某种权限（如读、写、所有），那么需要先将该用户加入到某个组中，然后授予某个角色所申请的权限，最后将该角色和用户所在组进行关联即可。

对于 Hive 来说，组与用户的关系主要基于 Hive 所采用的用户身份认证系统（如 Kerberos、LDAP），如果没有配置，则默认使用服务器上 Linux 系统的用户系统。

## 安装

Sentry 服务的比较简单，分为两部分：修改配置文件和初始化数据库。

### 配置

下载 Sentry 安装包之后，解压到指定目录，然后修改 $SENTRY_HOME/conf/sentry-site.xml 文件，修改后的内容如下：
```shell
<configuration>
  <property>
    <name>sentry.service.security.mode</name>
    <value>none</value>
  </property>
  <property>
    <name>sentry.store.jdbc.url</name>
    <value>jdbc:mysql://localhost:3306/sentry?createDatabaseIfNotExist=true&amp;characterEncoding=UTF-8</value>
  </property>
  <property>
    <name>sentry.service.admin.group</name>
    <value>root,xzd</value>
  </property>
  <property>
    <name>sentry.service.allow.connect</name>
    <value>hive,hdfs</value>
  </property>
  <property>
    <name>sentry.store.jdbc.driver</name>
    <value>com.mysql.jdbc.Driver</value>
  </property>
  <property>
    <name>sentry.store.jdbc.user</name>
    <value>root</value>
  </property>
  <property>
    <name>sentry.store.jdbc.password</name>
    <value>1qaz2wsx</value>
  </property>
  <property>
    <name>sentry.service.server.rpcport</name>
    <value>8038</value>
  </property>
  <property>
    <name>sentry.service.server.rpcaddress</name>
    <value>0.0.0.0</value>
  </property>
  <property>
    <name>sentry.store.group.mapping</name>
    <value>org.apache.sentry.provider.common.HadoopGroupMappingService</value>
  </property>
  <property>
    <name>sentry.verify.schema.version</name>
    <value>false</value>
  </property>
</configuration>
```

### 初始化数据库

Sentry 将数据保存在第三方的数据库中，因此需要对 Sentry 使用到的数据库进行初始化。执行如下命令：
```shell
$SENTRY_HOME/bin/sentry --command schema-tool --conffile $SENTRY_HOME/conf/sentry-site.xml --dbType mysql --initSchema
```

## 启停服务

通过下面的命令对 Sentry 服务进行启停：
```shell
# start
nohup $SENTRY_HOME/bin/sentry --command service --conffile $SENTRY_HOME/conf/sentry-site.xml

# stop
ps -ef | grep SentryMain | grep -v grep | awk '{print $2}' | xargs kill -9
```

## Hive

### UDF

Sentry 支持对创建 UDF 进行授权。使用如下命令进行授权：
```shell
grant ALL on URI "file:///usr/local/hive-1.1.0-cdh5.8.2/auxlib/hive-udf-samples-1.0.jar" to role xuzd_test_role
```

其中，URI 的内容为 UDF 所在 jar 包的路径。