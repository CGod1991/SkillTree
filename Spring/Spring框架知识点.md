# Spring 框架知识点

标签：Spring 框架

---

- @Service：当需要定义某个类为一个 bean 时，则在这个类的类名前一行使用 @Service 注解。

- @Resource：当需要在某个类中定义一个属性，并且该属性是一个已存在的 bean，要为该属性赋值或注入时在该属性上一行使用 @Resource 注解。

- @Component 注解是配合 Spring 中的 classpath-scanning 功能使用的，在 Spring 的配置文件中配置了 `<context:component-scan base-package="com.greenline.dataplatform"/>` 后，Spring 会到指定的包下面扫描标注有 @Component 的类，如果找到，则将它们添加到容器进行管理，并根据它们所标注的 @Autowired 为这些类注入符合条件的依赖对象。