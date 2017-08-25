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

