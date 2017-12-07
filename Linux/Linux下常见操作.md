# Linux 下常用操作

标签：Linux

---

查看当前登陆用户的组内成员:
> groups

查看 test 用户所在的组以及组内的成员:
> groups test

查看软件包的依赖关系:
> yum deplist mysql-server

增加一个新用户到其他组:
> useradd -G groupName userName

将一个已有用户添加到一个已有组:
> usermod -g groupName userName

切换用户:
> 使用 `su username` 和 `su - username` 都可以实现切换用户的功能，但是需要注意的是，这两个命令之间的区别：
> - `su` 命令只是切换了用户的身份，但是 shell 环境仍然是前一个用户的 shell。也就是说，如果你在 `/opt/test` 目录使用了 `su` 命令切换了用户，那么切换之后的目录仍然是 `/opt/test` 。
- `su -` 命令将用户和 shell 环境一起进行了切换，也就是说，如果你在 `/opt/test` 目录使用 `su - username` 命令切换了用户，那么切换之后的目录将会变成 `/home/username` ，即 username 的 home 目录。

jmap 使用:
> - jmap 主要用于打印指定 java 进程的共享对象内存映射或堆内存细节。
> 
> - jmap 命令可以获得运行中的 jvm 堆的快照，从而可以离线分析堆，以检查内存泄漏，检查一些严重影响性能的大对象的创建，检查系统中什么对象最多，各种内存对象所占的内存大小等等。

> - 打印 heap 的概要信息，GC 使用的算法，heap 的配置以及 wise heap 的使用情况。具体用法如下：
```shell
[root@localhost ~]# jmap -heap 27900
Attaching to process ID 27900, please wait...
Debugger attached successfully.
Client compiler detected.
JVM version is 20.45-b01
using thread-local object allocation.
Mark Sweep Compact GC
Heap Configuration: #堆内存初始化配置
   MinHeapFreeRatio = 40     #-XX:MinHeapFreeRatio设置JVM堆最小空闲比率  
   MaxHeapFreeRatio = 70   #-XX:MaxHeapFreeRatio设置JVM堆最大空闲比率  
   MaxHeapSize = 100663296 (96.0MB)   #-XX:MaxHeapSize=设置JVM堆的最大大小
   NewSize = 1048576 (1.0MB)     #-XX:NewSize=设置JVM堆的‘新生代’的默认大小
   MaxNewSize = 4294901760 (4095.9375MB) #-XX:MaxNewSize=设置JVM堆的‘新生代’的最大大小
   OldSize = 4194304 (4.0MB)  #-XX:OldSize=设置JVM堆的‘老生代’的大小
   NewRatio = 2    #-XX:NewRatio=:‘新生代’和‘老生代’的大小比率
   SurvivorRatio = 8  #-XX:SurvivorRatio=设置年轻代中Eden区与Survivor区的大小比值
   PermSize = 12582912 (12.0MB) #-XX:PermSize=<value>:设置JVM堆的‘持久代’的初始大小  
   MaxPermSize = 67108864 (64.0MB) #-XX:MaxPermSize=<value>:设置JVM堆的‘持久代’的最大大小  
Heap Usage:
New Generation (Eden + 1 Survivor Space): #新生代区内存分布，包含伊甸园区+1个Survivor区
   capacity = 30212096 (28.8125MB)
   used = 27103784 (25.848182678222656MB)
   free = 3108312 (2.9643173217773438MB)
   89.71169693092462% used
Eden Space: #Eden区内存分布
   capacity = 26869760 (25.625MB)
   used = 26869760 (25.625MB)
   free = 0 (0.0MB)
   100.0% used
From Space: #其中一个Survivor区的内存分布
   capacity = 3342336 (3.1875MB)
   used = 234024 (0.22318267822265625MB)
   free = 3108312 (2.9643173217773438MB)
   7.001809512867647% used
To Space: #另一个Survivor区的内存分布
   capacity = 3342336 (3.1875MB)
   used = 0 (0.0MB)
   free = 3342336 (3.1875MB)
   0.0% used
tenured generation:   #当前的Old区内存分布  
   capacity = 67108864 (64.0MB)
   used = 67108816 (63.99995422363281MB)
   free = 48 (4.57763671875E-5MB)
   99.99992847442627% used
Perm Generation:     #当前的 “持久代” 内存分布
   capacity = 14417920 (13.75MB)
   used = 14339216 (13.674942016601562MB)
   free = 78704 (0.0750579833984375MB)
   99.45412375710227% used
```

查看指定进程:
> top -p 进程id

查看系统 CPU 总核数:
> grep 'model name' /proc/cpuinfo | wc -l

查看进程 CPU 和内存占用率排序：
> ps auxw --sort=rss //按照内存使用的实际大小排序，升序
> ps auxw --sort=%cpu //按照 CPU 使用的百分比排序，升序

awk 获取最后一列：
> cat file | awk -F ':' '{print $NF}'

查看指定进程的所有连接信息：
> lsof -p pid -nP | grep TCP

查看某个进程打开的文件数：
> lsof -p pid | wc -l

查看指定进程的线程数：
> pstree -p pid

查看所有的线程数：
> pstree -p | wc -l

获取网络 IO:
> 可通过读取 /proc/<pid>/net/dev 文件来获取指定进程的网络 IO。
> 也可以通过 nethogs 工具实时查看所有进程的网络 IO 情况。

获取磁盘 IO:
> 可通过读取 /proc/<pid>/io 文件来获取指定进程的磁盘 IO。
> 也可以同构 iotop 工具实时查看所有进程的网络 IO 情况。

批量 kill 进程：
> ps -ef | grep zabbix | grep -v grep | awk '{print $2}' | xargs kill -9

去除文件中的所有空格：
> `sed -i 's/[[:space:]]//g' tmp`

排序并去重：
> sort -u file

设置系统定时任务：
> 执行命令 `crontab -e`
> 然后进行编辑，如下：
> 
	*/5 * * * * /opt/test.sh		每五分钟执行一次脚本 /opt/test.sh
	30 5 * * * ls /opt				每天的 05:30 执行 ls /opt 的命令

在mysql外部执行mysql语句：
> mysql -uroot -ppasswd -h127.0.0.1 -e "use phoenix;show tables;"

使用多个查询条件（或）：
> cat test | grep -E "abc|ac"

删除file1之外的所有文件：
> ls | grep -v file1 | xargs rm

vi 显示行号:
> 在命令模式下，输入`:set nu`

查看系统版本：
> cat /etc/issue