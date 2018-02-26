# 常用 Shell 脚本

标签：shell 脚本

---

## 远程拷贝

scp.sh:

```shell
#!/bin/bash

if [ $# != 3 ];then
    echo "Usage: scp.sh [dir|file] src dst"
    exit 1
fi

TYPE=$1
SRC=$2
DST=$3
for ip in `cat hosts`
do
    echo "=============$ip start============="
    if [ $TYPE == 'dir' ];then
        scp -r $SRC root@$ip:$DST
    elif [ $TYPE == 'file' ];then
        scp $SRC root@$ip:$DST
    else
        echo "Param invalidate:$TYPE."
        echo "Usage: scp.sh [dir|file] src dst"
        exit 1
    fi
    echo "=============$ip done============="
done

```