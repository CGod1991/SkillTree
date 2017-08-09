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

## 切换用户

使用 `su username` 和 `su - username` 都可以实现切换用户的功能，但是需要注意的是，这两个命令之间的区别：

- `su` 命令只是切换了用户的身份，但是 shell 环境仍然是前一个用户的 shell。也就是说，如果你在 `/opt/test` 目录使用了 `su` 命令切换了用户，那么切换之后的目录仍然是 `/opt/test` 。
- `su -` 命令将用户和 shell 环境一起进行了切换，也就是说，如果你在 `/opt/test` 目录使用 `su - username` 命令切换了用户，那么切换之后的目录将会变成 `/home/username` ，即 username 的 home 目录。