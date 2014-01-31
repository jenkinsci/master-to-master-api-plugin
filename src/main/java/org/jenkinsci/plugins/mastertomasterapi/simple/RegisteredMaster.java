package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.remoting.Base64;

import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Masters that are registered as a peer.
 *
 * @author Kohsuke Kawaguchi
 */
public class RegisteredMaster {
    public final URL url;

    /**
     * base64-encoded persisted form of the public key.
     */
    private final String publicKey;

    private boolean approved;

    private transient PublicKey key;

    public RegisteredMaster(URL url, PublicKey key) {
        this.url = url;
        this.publicKey = Base64.encode(key.getEncoded());
    }

    public URL getUrl() {
        return url;
    }

    public PublicKey getKey() {
        if (key == null) {
            try {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(publicKey));
                KeyFactory kf = KeyFactory.getInstance("RSA");
                key = kf.generatePublic(spec);
            } catch (GeneralSecurityException e) {
                throw new Error(e); // shouldn't happen
            }
        }
        return key;
    }

    public boolean isApproved() {
        // TODO: implement a proper approval check
//        return approved;
        return true;
    }
}
