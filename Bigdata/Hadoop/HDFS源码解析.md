# HDFS 源码解析

标签：HDFS 源码

---

## FsShell

用户通过命令 `hdfs dfs -rm /path` 删除 HDFS 上的文件时，对命令中的 `dfs` 的参数的解析是在 `org.apache.hadoop.fs.shell.Delete.RM.processOptions` 方法中进行的。

## 修改源码

增加的功能为：

	当用户删除 HDFS 上的目录或文件时，校验要删除的路径是否为根目录，如果是，则不允许删除；
	如果要删除的目录或文件为一级目录或文件，则判断对应的标志位是否为 true：如果标志位为true，则继续执行，否则中断执行，提示用户要删除的目录或文件为一级目录或文件

涉及到的类主要有：`org.apache.hadoop.fs.shell.Delete.Rm`，需要在该类中增加相应的标志位属性，并修改 `processPath()` 方法的具体逻辑。 