package org.jenkinsci.plugins.mastertomasterapi.proxy;

import javax.annotation.CheckForNull;
import java.io.Serializable;

/**
 * Argument to the call.
 *
 * Marked as serializable so that it can be used easily with RPC framework that supports Java serialization.
 *
 * @author Kohsuke Kawaguchi
 */
public class Argument implements Serializable {
    /**
     * Expected type of this argument in the method signature of the receiver.
     *
     * Can be null if ths caller cannot provide this information (such as calling from a dynamically typed scripting language.)
     */
    public final @CheckForNull Class type;

    /**
     * Name of the argument.
     *
     * Can be null if the caller cannot provide this information.
     */
    public final @CheckForNull String name;

    /**
     * Actual value of the argument.
     */
    public final Object value;

    public Argument(Class type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public boolean isAssignableTo(Class<?> expected) {
        if (type!=null)
            return expected.isAssignableFrom(type);
        if (value!=null)
            return expected.isAssignableFrom(value.getClass());

        // if both are null, then we expect the receiver to have a reference type
        return Object.class.isAssignableFrom(expected);
    }

    private static final long serialVersionUID = 1L;
}
