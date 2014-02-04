package org.jenkinsci.plugins.mastertomasterapi;

import hudson.Util;
import hudson.model.ModelObject;
import hudson.remoting.Base64;
import hudson.remoting.Channel;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;

/**
 * Represents a connection to another master.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Master implements ModelObject {
    /**
     * Base64 encoded value of the public key of Jenkins instance.
     * See {@link InstanceIdentity#getPublic()} as in
     * {@code Base64.encode(InstanceIdentity.get().getPublic().getEncoded())}
     */
    public String getPublicKeyString() {
        return Base64.encode(getPublicKey().getEncoded());
    }

    /**
     * RSA public key in the public key fingerprint format of "11:22:33:44:....:ee:ff"
     */
    public String getPublicKeyFingerprint() throws IOException {
        String s = Util.getDigestOf(new ByteArrayInputStream(getPublicKey().getEncoded()));
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<s.length(); i+=2) {
            if (buf.length()>0)        buf.append(':');
            buf.append(s.charAt(i)).append(s.charAt(i+1));
        }
        return buf.toString();
    }

    /**
     * Instance identity of this master as an RSA public key.
     */
    public abstract PublicKey getPublicKey();

    /**
     * If the communication to this master supports a {@link Channel} object, return it.
     * This method may create the channel on-demand.
     */
    @CheckForNull
    public abstract Channel getChannel();

    public abstract URL getURL();

    /**
     * Queries other services available on this master that can be used to communicate.
     *
     * The returned object is a proxy to the remote service.
     *
     * @param <T>
     *     Interface that represents the contract.
     */
    @CheckForNull
    public <T> T getService(Class<T> type) {
        if (!type.isInterface())
            throw new UnsupportedOperationException(type+" is not an interface");
        /*
            The default implementation is remoting based.
            for an implementation that doesn't support remoting,
            it'll need a different implementation
         */
        Channel ch = getChannel();
        if (ch==null)   return null;
        return type.cast(ch.getRemoteProperty(type.getName()));
    }
}
