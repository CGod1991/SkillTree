# Shell 常用命令

标签：shell

---

- test和[ ]

	[]是test的别名，都是对某个表达式进行判断，返回真或假。
	如：test expression、[ expression ]都是当expression为真时返回真，expression为假时返回假。通常会和&&、||一起用。
	例如：
		[ expression ] && echo "expression is true"：只有当expression为真时才会打印"expression is true"。
		[ expression ] || echo "expression is false"：只有当expression为假时才会打印"expression is false"。

- 不换行输出：`echo -n`。

- 文件比较符

	- `-e filename`: 如果 filename 存在，则为真。使用方法：`[ -e /etc/profile ]` 
	- `-d filename`: 如果 filename 为目录，则为真。
	- `-a filename`： 如果 filename 存在，则为真。
	- `-z string`： 如果 string 长度为零，则为真。使用方法：`[ -z "$myvar" ]`

- 批量 kill 进程：`ps -ef | grep zabbix | grep -v grep | awk '{print $2}' | xargs kill -9`

- 去除文件中的所有空格：`sed -i 's/[[:space:]]//g' tmp`

- 排序并去重：`sort -u file`

- 设置系统定时任务：执行命令 `crontab -e`，然后进行编辑，如下：
		*/5 * * * * /opt/test.sh		每五分钟执行一次脚本 /opt/test.sh
		30 5 * * * ls /opt				每天的 05:30 执行 ls /opt 的命令

- 在mysql外部执行mysql语句：`mysql -uroot -ppasswd -h127.0.0.1 -e "use phoenix;show tables;"`

- 使用多个查询条件（或）：`cat test | grep -E "abc|ac"`

- 删除file1之外的所有文件：`ls | grep -v file1 | xargs rm`
- vi 显示行号:在命令模式下，输入`:set nu`

- 查看系统版本：`cat /etc/issue`

## 脚本参数

- $0：该脚本的文件名。
- $?：上一个指令的返回值。
- $*：所有位置参数的内容，也就是调用该脚本的参数。返回一个字符串，字符串中由空格分隔。
- $@：也是返回所有参数的内容，但返回的是多个字符串。

## shift

Shell 脚本中的 shift 命令用于将脚本的参数进行左移，通常用于在不知道传入参数个数的情况下，依次遍历每个参数然后进行相应的处理，常见于 Linux 中各种程序的启动脚本。

比如：
run.sh:
```shell
#!/bin/bash  
while [ $# != 0 ];do  
	echo "第一个参数为：$1,参数个数为：$#"  
	shift  
done 
```

当调用 `run.sh` 时，命令 `sh run.sh a b c d e f` 的结果显示如下：
```shell
第一个参数为：a,参数个数为：6
第一个参数为：b,参数个数为：5
第一个参数为：c,参数个数为：4
第一个参数为：d,参数个数为：3
第一个参数为：e,参数个数为：2
第一个参数为：f,参数个数为：1
```

也就是说，shift(shift 1) 命令每执行一次，变量的个数($#)减一（之前的 $1 变量被销毁,之后的 $2 就变成了 $1），而变量值提前一位。同理，shift n 后，前 n 位参数都会被销毁。