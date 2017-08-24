package org.xzd.javacode.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by xuzd on 17-7-27.
 */
public class Invoker implements InvocationHandler {
    public AbstractClass ac;

    public Invoker(AbstractClass ac) {
        this.ac = ac;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Start invoke()");
        method.invoke(ac, args);
        System.out.println("End invoke()");
        return null;
    }
}
