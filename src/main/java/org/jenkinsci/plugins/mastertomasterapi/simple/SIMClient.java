package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.remoting.Base64;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Kohsuke Kawaguchi
 */
public class SIMClient {
    public ConnectedMaster connect(URL jenkins) throws IOException {
        InetSocketAddress a = getCliTcpPort(jenkins);
        Socket s = new Socket(a.getAddress(), a.getPort());

        SIMProtocol p = SIMProtocol.get();
        new DataOutputStream(s.getOutputStream()).writeUTF("Protocol:"+p.getName());
        return p.connect(s,true);
    }
    /**
     * If the server advertises CLI endpoint, returns its location.
     */
    // TODO: copied from CLI client. needs to refactor
    private InetSocketAddress getCliTcpPort(URL url) throws IOException {
        URLConnection head = openConnection(url);

        String h = head.getHeaderField("X-Jenkins-CLI-Host");
        if (h==null)    h = head.getURL().getHost();
        String p = head.getHeaderField("X-Jenkins-CLI-Port");
        if (p==null)    p = head.getHeaderField("X-Hudson-CLI-Port");   // backward compatibility

        if (p==null) {
            throw new IOException("No X-Jenkins-CLI-Port among " + head.getHeaderFields().keySet());
        }

        return new InetSocketAddress(h,Integer.parseInt(p));
    }

    public PublicKey getInstanceIdentity(URL jenkins) throws IOException, GeneralSecurityException {
        // TODO: better error check
        String publicKey = openConnection(jenkins).getHeaderField("X-Instance-Identity");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(publicKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private URLConnection openConnection(URL url) throws IOException {
        if (url.getHost()==null || url.getHost().length()==0) {
            throw new IOException("Invalid URL: "+url);
        }
        URLConnection head = url.openConnection();
        try {
            head.connect();
        } catch (IOException e) {
            throw (IOException)new IOException("Failed to connect to "+url).initCause(e);
        }
        return head;
    }

}
