package org.jenkinsci.plugins.mastertomasterapi;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import javax.annotation.CheckForNull;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class InterMasterService implements ExtensionPoint {
    /**
     * Creates a instance that receives inter-master calls.
     */
    @CheckForNull
    public abstract <T> T getInstance(Class<T> interfaceType, Master remote);

    public static ExtensionList<InterMasterService> all() {
        return Jenkins.getInstance().getExtensionList(InterMasterService.class);
    }
}
