package org.jenkinsci.plugins.mastertomasterapi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a type-safe proxy object that calls into a {@link RPC} object.
 *
 * @author Kohsuke Kawaguchi
 */
public class TypeSafeProxy implements InvocationHandler {
    public static <T> T create(Class<T> type, RPC receiver) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new TypeSafeProxy(receiver)));
    }

    private final RPC receiver;

    public TypeSafeProxy(RPC receiver) {
        this.receiver = receiver;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Argument> a = new ArrayList<Argument>(args.length);
        Class<?>[] types = method.getParameterTypes();

        for (int i=0; i<args.length; i++)
            a.add(new Argument(types[i], null /*TODO*/, args[i]));

        return receiver.call(method.getName(), a, Collections.emptySet());
    }
}
