# Ansible 基本命令

标签：Ansible

---

## 批量执行命令

`ansible -i /home/hadoop/hosts all -m shell -a "ln -s /usr/local/hbase-current/conf/hbase-site.xml /usr/local/hadoop-current/etc/hadoop/hbase-site.xml" -f 23`

## 批量替换配置文件

`ansible-playbook -i /home/hadoop/hosts site.yaml  -f 23`