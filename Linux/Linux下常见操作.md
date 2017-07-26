# Linux 下常用操作

标签：Linux

---

- `groups`：查看当前登陆用户的组内成员。
- `groups test`：查看 test 用户所在的组以及组内的成员。

## 查看软件包的依赖关系

`yum deplist mysql-server`

## 增加一个新用户到其他组

`useradd -G groupName userName`

## 将一个已有用户添加到一个已有组

`usermod -g groupName userName`