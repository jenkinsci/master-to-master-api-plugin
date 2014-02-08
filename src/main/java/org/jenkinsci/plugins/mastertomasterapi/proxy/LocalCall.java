package org.jenkinsci.plugins.mastertomasterapi.proxy;

import org.kohsuke.stapler.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calls methods on a local object without any remoting.
 *
 * To cope with the versioning problem, the method selection is relaxed a little bit.
 * It prefers the exact match, but if no such method definition is available, it will
 * try to find a method that has a longer list of arguments and try to fill in the rest
 * of the arguments as JVM default values. Failing that, it'll still try to call a method
 * that has a shorter arguments by dropping the rest of the arguments.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalCall implements RPC {
    private final Object receiver;
    private final List<MethodDef> methods = new ArrayList<MethodDef>();

    public LocalCall(Object receiver) {
        this.receiver = receiver;
        for (Method m : receiver.getClass().getMethods())
            methods.add(new MethodDef(m));
    }

    /**
     * Public method definition on the receiver.
     */
    private class MethodDef {
        private final Method m;
        private Class<?>[] paramTypes;

        private MethodDef(Method m) {
            this.m = m;
            this.paramTypes = m.getParameterTypes();
            // TODO: capture argument names and use them
        }

        /**
         * Picks the best matching method to dispatch the call to. Higher the score, the better.
         */
        private int score(String methodName, List<Argument> args) {
            if (!m.getName().equals(methodName))
                return 0;

            int expected = paramTypes.length;
            int actual = args.size();

            // tolerate argument count mismatch, but not type
            int c = Math.min(expected, actual);
            for (int i=0; i<c; i++) {
                if (!args.get(i).isAssignableTo(paramTypes[i]))
                    return 0;
            }

            if (expected == actual)
                return 3000;

            // prefer calling a method with long list of parameters with default values appended at the end,
            // as opposed to calling a method with a short list of parameters and dropping arguments
            if (expected>actual)
                return 2000 - (expected-actual);
            else
                return 1000 - (actual-expected);
        }

        /**
         * Invokes the method.
         */
        public Object invoke(List<Argument> args) throws Throwable {
            Object[] a = new Object[paramTypes.length]; // actual arguments to call

            int expected = paramTypes.length;
            int actual = args.size();
            int c = Math.min(expected, actual);

            for (int i=0; i<c; i++) {
                a[i] = args.get(i).value;
            }

            // fill in the rest by the default value for types
            for (int i=c; i<expected; i++) {
                a[i] = ReflectionUtils.getVmDefaultValueFor(paramTypes[i]);
            }

            try {
                return m.invoke(receiver,a);
            } catch (IllegalAccessException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    public Object call(String methodName, List<Argument> argumentsAndNames, Set<Object> context) throws Throwable {
        MethodDef method = findBestMethod(methodName, argumentsAndNames);

        if (method!=null)
            return method.invoke(argumentsAndNames);
        else
            throw new NoSuchMethodException(methodName);
    }

    /**
     * Picks the method with the best score
     */
    private MethodDef findBestMethod(String methodName, List<Argument> argumentsAndNames) {
        int score=0;
        MethodDef method=null;
        for (MethodDef m : methods) {
            int s = m.score(methodName, argumentsAndNames);
            if (s>score) {
                score = s;
                method = m;
            }
        }
        return method;
    }

}
