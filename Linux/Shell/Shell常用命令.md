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
