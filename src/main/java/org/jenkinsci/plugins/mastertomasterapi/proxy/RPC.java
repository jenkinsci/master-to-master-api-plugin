package org.jenkinsci.plugins.mastertomasterapi.proxy;

import org.acegisecurity.Authentication;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Remote procedure call interface.
 *
 * Each instance of {@link RPC} represents a specific receiver.
 *
 * @author Kohsuke Kawaguchi
 */
public interface RPC {
    /**
     * @param methodName
     *      Name of the method to invoke. Matches {@link Method#getName()}
     * @param arguments
     *      Ordered list of arguments and their related type information.
     * @param context
     *      Any other contextual information regarding this call, such as the {@link Authentication} object
     *      that establishes the identity of the caller.
     *
     * @return
     *      Return value from the RPC method.
     */
    Object call(String methodName, List<Argument> arguments, Set<Object> context) throws Throwable;
}
