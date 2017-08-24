package org.xzd.javacode.proxy;

import java.lang.reflect.Proxy;

/**
 * Created by xuzd on 17-7-27.
 */
public class DynamicProxyTest {
    public static void main(String[] args) {
        show(new ClassA());
        show(new ClassB());
    }

    private static void show(AbstractClass ac) {
        Invoker invoker = new Invoker(ac);
        AbstractClass abstractClass = (AbstractClass) Proxy.newProxyInstance(AbstractClass.class.getClassLoader(),
                new Class[]{AbstractClass.class}, invoker);
        abstractClass.show();
    }
}
