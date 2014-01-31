package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Extension;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.ManagementLink;
import hudson.remoting.Callable;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.mastertomasterapi.InterMasterConnection;
import org.jenkinsci.plugins.mastertomasterapi.Master;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
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
    transient Jenkins jenkins;

    @Inject
    public transient SIMConnectionSet cons;

    public SIMManagement() throws IOException {
    }

    protected XmlFile getConfigFile() {
        return new XmlFile(new File(jenkins.getRootDir(),"sim/masters.xml"));
    }

    public void load() throws IOException {
        XmlFile f = getConfigFile();
        if (f.exists())
            f.unmarshal(this);
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
                if (n.getURL().equals(m.getURL())) {
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
            if (cm.getURL().equals(rootUrl) && cm.getKey().equals(peer)) {
                return cm;
            }
        }
        return null;
    }

    @RequirePOST
    public HttpResponse doRegister(@QueryParameter String url) throws IOException, GeneralSecurityException {
        jenkins.checkPermission(Jenkins.ADMINISTER);
        URL jenkins = new URL(url);
        add(new RegisteredMaster(jenkins,new SIMClient().getInstanceIdentity(jenkins)));
        return HttpResponses.ok();
    }

    @RequirePOST
    public HttpResponse doConnectAll() {
//        SIMConnectionSet cons = SIMConnectionSet.get();
        jenkins.checkPermission(Jenkins.ADMINISTER);
        for (RegisteredMaster master : masters) {
            ConnectedMaster c = cons.get(master.getKey());
            if (c==null) {// try to connect
                URL url = master.getURL();
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

    @RequirePOST
    public HttpResponse doSayHelloToAll() throws InterruptedException, IOException {
        final String me = Jenkins.getInstance().getRootUrl();
        for (InterMasterConnection<?> imc : InterMasterConnection.all()) {
            for (Master m : imc) {
                m.getChannel().call(new HelloWorld(me));
            }
        }
        return HttpResponses.ok();
    }

    public static SIMManagement get() {
        return all().get(SIMManagement.class);
    }

    private static final Logger LOGGER = Logger.getLogger(SIMManagement.class.getName());

    @Initializer(after=InitMilestone.JOB_LOADED, fatal=false)
    public static void init() throws IOException {
        get().load();
        get().doConnectAll();   // TODO: this is just to get us going
    }

    private static class HelloWorld implements Callable<Void, IOException> {
        private final String me;

        public HelloWorld(String me) {
            this.me = me;
        }

        public Void call() throws IOException {
            System.out.println("Hello world from "+ me);
            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}
