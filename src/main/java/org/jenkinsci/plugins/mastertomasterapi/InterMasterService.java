package org.jenkinsci.plugins.mastertomasterapi;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.remoting.Channel;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;

/**
 * An extension point for exposing inter-master service over {@link Master#getService(Class)}.
 *
 * <p>
 * An object returned from this class will be proxied (possibly by {@link Channel} or something else)
 * to another master.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InterMasterService implements ExtensionPoint {
    /**
     * Creates a instance that receives inter-master calls.
     *
     * @param remote
     *      {@link Master} that the returned instance will serve.
     */
    @CheckForNull
    public abstract <T> T getInstance(Class<T> interfaceType, Master remote);

    public static ExtensionList<InterMasterService> all() {
        return Jenkins.getInstance().getExtensionList(InterMasterService.class);
    }
}
