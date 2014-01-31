package org.jenkinsci.plugins.mastertomasterapi;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import java.security.PublicKey;

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
public abstract class InterMasterConnection<T extends Master> implements Iterable<T>, ExtensionPoint {
    /**
     * Obtains a connected master by its public key.
     *
     * @param publicKey
     *      Looks up {@link Master} by its {@link Master#getPublicKeyString()}
     * @return null if the master isn't connected.
     */
    public T get(String publicKey) {
        for (T m : this)
            if (m.getPublicKeyString().equals(publicKey))
                return m;
        return null;
    }

    public T get(PublicKey key) {
        for (T m : this)
            if (m.getPublicKey().equals(key))
                return m;
        return null;
    }

    /**
     * Returns all the {@link InterMasterConnection} implementations.
     */
    public static ExtensionList<InterMasterConnection> all() {
        return Jenkins.getInstance().getExtensionList(InterMasterConnection.class);
    }
}
