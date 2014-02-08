package org.jenkinsci.plugins.mastertomasterapi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hudson.remoting.Callable;
import hudson.remoting.Channel;

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
        return type.cast(ch.call(new RemoteServiceTask(type)));
    }

    private static class RemoteServiceTask implements Callable<Object,IOException> {
        private final Class interfaceType;

        private RemoteServiceTask(Class interfaceType) {
            this.interfaceType = interfaceType;
        }

        public Object call() throws IOException {
            Master m = Master.from(Channel.current());
            for (InterMasterService ims : InterMasterService.all()) {
                Object o = ims.getInstance(interfaceType, m);
                if (o!=null)
                    // TODO: wrap this in the security transparent object
                    return o;
            }
            return null;
        }

        private static final long serialVersionUID = 1L;
    }

}
