package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.ManagementLink;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * UI-bound object that keeps track of all the {@link RegisteredMaster}.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SIMManagement extends ManagementLink {
    private final Vector<RegisteredMaster> masters = new Vector<RegisteredMaster>();

    @Inject
    Jenkins jenkins;

    @Inject
    SIMConnectionSet cons;

    public SIMManagement() throws IOException {
        load();
    }

    protected XmlFile getConfigFile() {
        return new XmlFile(new File(jenkins.getRootDir(),"sim/masters.xml"));
    }

    public void load() throws IOException {
        getConfigFile().unmarshal(this);
    }

    public synchronized void save() throws IOException {
        getConfigFile().write(this);
    }


    public List<RegisteredMaster> getMasters() {
        return masters;
    }

    public void add(RegisteredMaster m) throws IOException {
        synchronized (masters) {
            for (RegisteredMaster n : masters.toArray(new RegisteredMaster[masters.size()])) {
                // both of them are supposed to be unique more or less, so remove duplicates.
                if (n.getUrl().equals(m.getUrl())) {
                    masters.remove(n);
                }
                if (n.getKey().equals(m.getKey())) {
                    masters.remove(n);
                }
            }
        }

        masters.add(m);
        save();
    }

    @Override
    public String getIconFileName() {
        return "computer.png";
    }

    @Override
    public String getUrlName() {
        return "simple-inter-masters";
    }

    public String getDisplayName() {
        return "Simple Inter-Master Communications";
    }

    /**
     * Finds a {@link RegisteredMaster} that matches the description.
     */
    public RegisteredMaster findConnectedMaster(URL rootUrl, PublicKey peer) {
        for (RegisteredMaster cm : getMasters()) {
            if (cm.getUrl().equals(rootUrl) && cm.getKey().equals(peer)) {
                return cm;
            }
        }
        return null;
    }

    public HttpResponse doConnectAll() {
        for (RegisteredMaster master : masters) {
            ConnectedMaster c = cons.get(master.getKey());
            if (c==null) {// try to connect
                URL url = master.getUrl();
                LOGGER.fine("Trying to connect to " + url);
                try {
                    new SIMClient().connect(url);
                } catch (IOException e) {
                    LOGGER.log(WARNING, "Failed to connect to " + url, e);
                }
            }
        }
        return HttpResponses.ok();
    }

    public static SIMManagement get() {
        return all().get(SIMManagement.class);
    }

    private static final Logger LOGGER = Logger.getLogger(SIMManagement.class.getName());

}
