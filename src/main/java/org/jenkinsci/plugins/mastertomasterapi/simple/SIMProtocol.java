package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Extension;
import hudson.cli.Connection;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Listener;
import hudson.remoting.Channel.Mode;
import hudson.util.IOException2;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class SIMProtocol extends AgentProtocol {
    @Inject
    SIMManagement mgmt;

    @Inject
    SIMConnectionSet cons;

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void handle(Socket socket) throws IOException {
        connect(socket,false);
    }

    public ConnectedMaster connect(Socket socket, boolean isClient) throws IOException {
        try {
            Connection c = new Connection(socket);
            byte[] secret = c.diffieHellman(isClient).generateSecret();
            SecretKey sessionKey = new SecretKeySpec(Connection.fold(secret,128/8),"AES");
            c = c.encryptConnection(sessionKey,"AES/CFB8/NoPadding");

            InstanceIdentity id = InstanceIdentity.get();

            // ascertain the public key of that instance by challenge&response.
            c.proveIdentity(secret, new KeyPair(id.getPublic(),id.getPrivate()));
            PublicKey peer = c.verifyIdentity(secret);

            c.writeUTF(Jenkins.getInstance().getRootUrl());
            final URL rootUrl = new URL(c.readUTF());

            RegisteredMaster m = mgmt.findConnectedMaster(rootUrl,peer);
            if (m==null) {
                mgmt.add(m=new RegisteredMaster(rootUrl,peer));
            }

            c.writeUTF(m.isApproved()?"OK":"Unauthorized");

            String msg = c.readUTF();
            if (!msg.equals("OK")) {
                throw new IOException(socket+" refused SIM connection: "+msg);
            }

            Channel channel = new Channel("Simple master-to-master channel with " + socket.getInetAddress(),
                    Computer.threadPoolForRemoting, Mode.BINARY,
                    new BufferedInputStream(c.in), new BufferedOutputStream(c.out), null, false, Jenkins.getInstance().pluginManager.uberClassLoader);

            final ConnectedMaster cm = new ConnectedMaster(peer,channel,rootUrl);
            cons.masters.add(cm);
            LOGGER.log(INFO, "Connected to "+rootUrl);

            channel.addListener(new Listener() {
                @Override
                public void onClosed(Channel channel, IOException cause) {
                    LOGGER.log(INFO, "Disonncected to "+rootUrl);
                    cons.masters.remove(cm);
                }
            });

            return cm;
        } catch (GeneralSecurityException e) {
            throw new IOException2(e);
        }
    }

    public static SIMProtocol get() {
        return all().get(SIMProtocol.class);
    }


    private static final Logger LOGGER = Logger.getLogger(SIMProtocol.class.getName());
}
