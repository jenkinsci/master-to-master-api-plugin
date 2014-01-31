package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.Util;
import hudson.remoting.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public URL getURL() {
        return url;
    }

    public String getPublicKeyString() {
        return Base64.encode(key.getEncoded());
    }

    public String getPublicKeyFingerprint() throws IOException {
        String s = Util.getDigestOf(new ByteArrayInputStream(Base64.decode(publicKey)));
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<s.length(); i+=2) {
            if (buf.length()>0)        buf.append(':');
            buf.append(s.charAt(i)).append(s.charAt(i+1));
        }
        return buf.toString();
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
