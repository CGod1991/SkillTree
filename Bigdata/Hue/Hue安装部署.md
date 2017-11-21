# Hue 部署

标签：Hue

---

## 简介

## 依赖

安装 Hue 需要以下依赖：
> yum install ant asciidoc cyrus-sasl-devel cyrus-sasl-gssapi cyrus-sasl-plain gcc gcc-c++ krb5-devel libffi-devel libxml2-devel libxslt-devel make mysql mysql-devel openldap-devel python-devel sqlite-devel gmp-devel

## 安装

从 [这里](http://archive.cloudera.com/cdh5/cdh/5/hue-3.9.0-cdh5.8.2.tar.gz) 下载 Hue 安装包。

进入解压后的目录，执行以下命令进行安装：
> PREFIX=$HUE_HOME make install 

其中，$HUE_HOME 为 Hue 安装目录。

## 配置

Hue 的配置文件路径为 $HUE_HOME/desktop/conf/hue.ini。

Hue 对不同的 Hadoop 组件采用分段配置的方式。

### Hue

在 [desktop] 中配置如下属性：
> secret_key=1qaz2wsxdjflslsdjifidflsjflwieiflkfjslheiofwhfd
> http_host=hadoop1
> http_port=8888
> // Webserver runs as this user
   server_user=hue
   server_group=hadoop
> // This should be the Hue admin and proxy user
   default_user=hue

> // This should be the hadoop cluster admin
   default_hdfs_superuser=hdfs

在 $HADOOP_HOME/etc/hadoop/hdfs-site.xml 中增加以下内容：
```shell
<property>
  <name>dfs.webhdfs.enabled</name>
  <value>true</value>
</property>
```

在 $HADOOP_HOME/etc/hadoop/core-site.xml 中增加以下内容：
```shell
<property>
  <name>hadoop.proxyuser.hue.hosts</name>
  <value>*</value>
</property>
<property>
  <name>hadoop.proxyuser.hue.groups</name>
  <value>*</value>
</property>
```

### HDFS

在 [hadoop] 中的 [[hdfs_clusters]] 中配置如下属性：
> fs_defaultfs=hdfs://hadoop1:9000
> webhdfs_url=http://hadoop1:50070/webhdfs/v1
> hadoop_conf_dir=/opt/hadoop/etc/hadoop

在 $HADOOP_HOME/etc/hadoop/core-site.xml 中增加以下内容：
```shell
<property>
  <name>hadoop.proxyuser.hdfs.hosts</name>
  <value>*</value>
</property>
<property>
  <name>hadoop.proxyuser.hdfs.groups</name>
  <value>*</value>
</property>
```

### YARN

在 [hadoop] 中的 [[yarn_clusters]] 中配置如下属性：
> resourcemanager_host=hadoop1
> resourcemanager_port=8032
> resourcemanager_api_url=http://hadoop1:8088
> proxy_api_url=http://hadoop1:8088
> history_server_api_url=http://hadoop1:19888

### Hive

在 [beeswax] 中配置如下属性：
> hive_server_host=hadoop1
> hive_server_port=10000
> hive_conf_dir=/opt/hive/conf

### HBase

在 [hbase] 中配置如下属性：
> hbase_clusters=(Cluster|hadoop1:9090)
> hbase_conf_dir=/opt/hbase/conf

在 $HADOOP_HOME/etc/hadoop/core-site.xml 中增加以下内容：
```shell
<property>
  <name>hadoop.proxyuser.hbase.hosts</name>
  <value>*</value>
</property>
<property>
  <name>hadoop.proxyuser.hbase.groups</name>
  <value>*</value>
</property>
```

## 启动

1. 确认 Hadoop 集群和 HBase 集群正常运行。
2. 确认 HiveServer2 实例、HDFS HTTPFS 和 HBase thrift 已正常运行。
3. 启动 Hue：`$HUE_HOME/build/env/bin/supervisor &`

## 验证

通过 http://hadoop1:8888 访问。

需要注意的是，第一次访问时，需要创建用户和密码，因此可以使用自定义的用户和密码登陆。但需要记住该用户名和密码，后续会使用该用户作为管理员账户。

## 整合 LDAP

Hue 支持接入 LDAP 系统，这样用户登陆时的验证工作将交给 LDAP 服务去做，Hue 本身也就不会保存用户的信息，如登陆密码和组映射关系等。

但需要注意的是，在接入 LDAP 之前，需要先在 Hue 中创建一个 LDAP 中存在的用户，且使用相同的密码，然后给这个用户赋予管理员权限。否则，在配置好 LDAP 之后，将无法登陆 Hue。

### 配置

在 $HUE_HOME/desktop/conf/hue.ini 中的 [desktop] -> [[auth]] 中配置如下属性：
> backend=desktop.auth.backend.LdapBackend

在 [desktop] -> [[ldap]] 中配置如下属性：
> base_dn="ou=wecloud,dc=guahao-inc,dc=com"
> ldap_url=ldap://127.0.0.1:389
> bind_dn="cn=manager,dc=guahao-inc,dc=com"
> bind_password=secret
> search_bind_authentication=true

在 [desktop] -> [[ldap]] -> [[[users]]] 中配置如下属性：
> user_filter="objectclass=inetOrgPerson"
> user_name_attr=cn

在 [desktop] -> [[ldap]] -> [[[groups]]] 中配置如下属性：
> group_filter="objectclass=groupOfNames"
> group_name_attr=cn
> group_member_attr=member


