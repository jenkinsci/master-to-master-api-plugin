package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Extension;
import org.jenkinsci.plugins.mastertomasterapi.InterMasterConnection;

import javax.inject.Singleton;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension @Singleton
public class SIMConnectionSet extends InterMasterConnection<ConnectedMaster> {
    /*package*/ final CopyOnWriteArrayList<ConnectedMaster> masters = new CopyOnWriteArrayList<ConnectedMaster>();

    public Iterator<ConnectedMaster> iterator() {
        return masters.iterator();
    }

    public static SIMConnectionSet get() {
        return all().get(SIMConnectionSet.class);
    }
}
