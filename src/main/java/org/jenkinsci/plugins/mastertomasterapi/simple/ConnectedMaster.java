package org.jenkinsci.plugins.mastertomasterapi.simple;

import hudson.remoting.Channel;
import org.jenkinsci.plugins.mastertomasterapi.Master;

import java.net.URL;
import java.security.PublicKey;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConnectedMaster extends Master {
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

    @Override
    public <T> T getService(Class<T> type) {
        return null;    // TODO
    }

    public String getDisplayName() {
        return url.toExternalForm();
    }
}