package org.jenkinsci.plugins.mastertomasterapi;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

/**
 * Represents a set of existing connections into {@link Master}.
 *
 * <p>
 * Several plugins define their own topology and transport to connect multiple masters.
 * This abstraction allows those mechanisms to expose the connected masters, so as to
 * let other plugins talk to other masters.
 *
 * <p>
 * This abstraction by itself does not expose how to connect to other masters nor
 * how the topology looks like.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InterMasterConnection implements Iterable<Master>, ExtensionPoint {
    /**
     * Obtains other connected masters.
     *
     * @param instanceId
     *      Jenkins instance ID. See {@link Master#getInstanceId()}
     * @return null if the master isn't connected.
     */
    public abstract Master get(String instanceId);

    /**
     * Returns all the {@link InterMasterConnection} implementations.
     */
    public static ExtensionList<InterMasterConnection> all() {
        return Jenkins.getInstance().getExtensionList(InterMasterConnection.class);
    }
}
