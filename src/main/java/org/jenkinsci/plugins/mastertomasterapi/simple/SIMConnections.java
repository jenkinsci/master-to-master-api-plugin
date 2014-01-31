package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Extension;
import org.jenkinsci.plugins.mastertomasterapi.InterMasterConnection;
import org.jenkinsci.plugins.mastertomasterapi.Master;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SIMConnections extends InterMasterConnection {

    @Override
    public Master get(String publicKey) {
        return null;
    }

    public Iterator<Master> iterator() {
        return null;
    }
}
