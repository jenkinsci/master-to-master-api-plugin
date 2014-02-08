package org.jenkinsci.plugins.mastertomasterapi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import org.jenkinsci.plugins.mastertomasterapi.proxy.AuthenticationForwardingRPC;
import org.jenkinsci.plugins.mastertomasterapi.proxy.LocalCall;
import org.jenkinsci.plugins.mastertomasterapi.proxy.RPC;
import org.jenkinsci.plugins.mastertomasterapi.proxy.TypeSafeProxy;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Partial implementation of {@link Master} that uses {@link Channel} exporting to
 * find inter-master services.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractChannelBsaedMasterImpl extends Master {

    private final LoadingCache<Class,Object> services = CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {
        @Override
        public Object load(Class key) throws Exception {
            return resolveService(key);
        }
    });

    @CheckForNull
    public <T> T getService(Class<T> type) {
        if (!type.isInterface())
            throw new UnsupportedOperationException(type+" is not an interface");
        try {
            return type.cast(services.get(type));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The underlying function that serves {@link #getService(Class)}.
     *
     * A {@link Master} implementation can override this method to resolve service objects differently.
     */
    protected <T> T resolveService(Class<T> type) throws InterruptedException, IOException {
        /*
            The default implementation is remoting based.
            for an implementation that doesn't support remoting,
            it'll need a different implementation
         */
        Channel ch = getChannel();
        if (ch==null)   return null;
        RPC rpc = ch.call(new RemoteServiceTask(type));
        rpc = AuthenticationForwardingRPC.sender(rpc);
        return type.cast(TypeSafeProxy.create(type,rpc));
    }

    private static class RemoteServiceTask implements Callable<RPC,IOException> {
        private final Class interfaceType;

        private RemoteServiceTask(Class interfaceType) {
            this.interfaceType = interfaceType;
        }

        public RPC call() throws IOException {
            Channel ch = Channel.current();
            Master m = Master.from(ch);
            for (InterMasterService ims : InterMasterService.all()) {
                Object o = ims.getInstance(interfaceType, m);
                if (o!=null) {
                    RPC rpc = toRPC(o);
                    rpc = AuthenticationForwardingRPC.receiver(rpc);
                    rpc = ch.export(RPC.class, rpc);
                    return rpc;
                }
            }
            return null;
        }

        private RPC toRPC(Object o) {
            if (o instanceof RPC)
                return (RPC) o;
            else
                return new LocalCall(o);
        }

        private static final long serialVersionUID = 1L;
    }

}
