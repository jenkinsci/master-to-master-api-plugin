package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.remoting.Channel;
import org.jenkinsci.plugins.mastertomasterapi.AbstractChannelBasedMasterImpl;

import java.net.URL;
import java.security.PublicKey;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConnectedMaster extends AbstractChannelBasedMasterImpl {
    private final PublicKey key;
    private final Channel channel;
    private final URL url;

    public ConnectedMaster(PublicKey key, Channel channel, URL url) {
        this.key = key;
        this.channel = channel;
        this.url = url;
    }

    @Override
    public PublicKey getPublicKey() {
        return key;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public URL getURL() {
        return url;
    }

    public String getDisplayName() {
        return url.toExternalForm();
    }
}
