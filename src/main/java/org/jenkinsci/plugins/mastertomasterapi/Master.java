package org.jenkinsci.plugins.mastertomasterapi;

import hudson.model.ModelObject;
import hudson.remoting.Channel;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;
import java.net.URL;

/**
 * Represents a connection to another master.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Master implements ModelObject {
    /**
     * The same value as {@link Jenkins#getLegacyInstanceId()}
     */
    public abstract String getInstanceId();

    /**
     * If the communication to this master supports a {@link Channel} object, return it.
     * This method may create the channel on-demand.
     *
     * @throws UnsupportedOperationException
     *      if the underlying communication mechanism or the trust level does not allow the use of {@link Channel}.
     */
    @CheckForNull
    public abstract Channel getChannel();

    public abstract URL getURL();

    /**
     * Queries other services available on this master that can be used to communicate.
     */
    public abstract <T> T getService(Class<T> type);
}
