package org.jenkinsci.plugins.mastertomasterapi.proxy;

import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A pair of {@link RPC} filter objects that allows the caller and the receiver to pass the thread-local
 * {@link Authentication} object.
 *
 * To use this, wrap the caller side of the {@link RPC} chain with {@link #sender(RPC)}, and the receiver
 * side of the {@link RPC} chain with {@link #receiver(RPC)}
 *
 * @author Kohsuke Kawaguchi
 * @see Jenkins#getAuthentication()
 */
public abstract class AuthenticationForwardingRPC {
    private AuthenticationForwardingRPC() {} // no instantiation please

    public static RPC sender(final RPC receiver) {
        return new RPC() {
            public Object call(String methodName, List<Argument> arguments, Set<Object> context) throws Throwable {
                Authentication a = Jenkins.getAuthentication();

                context = new HashSet<Object>(context);
                context.add(new ForwardedAuthentication(a));

                return receiver.call(methodName,arguments,context);
            }
        };
    }

    public static RPC receiver(final RPC receiver) {
       return new RPC() {
           public Object call(String methodName, List<Argument> arguments, Set<Object> context) throws Throwable {
               Authentication a = find(context);

               SecurityContext old = ACL.impersonate(a);
               try {
                   return receiver.call(methodName,arguments,context);
               } finally {
                   SecurityContextHolder.setContext(old);
               }
           }

           /**
            * Finds authentication object from the context.
            */
           private Authentication find(Set<Object> context) {
               for (Object c : context) {
                   if (c instanceof ForwardedAuthentication) {
                       return (Authentication) c;
                   }
               }

               // fall back
               return Jenkins.ANONYMOUS;
           }
       };
    }

    private static final class ForwardedAuthentication implements Authentication, Serializable {
        private final String name;
        private final Object principal;
        private final GrantedAuthority[] authorities;

        private ForwardedAuthentication(Authentication a) {
            this.name = a.getName();
            this.principal = a.getPrincipal();
            GrantedAuthority[] au = a.getAuthorities();
            this.authorities = new GrantedAuthority[au.length];
            for (int i = 0; i < au.length; i++)
                authorities[i] = new GrantedAuthorityImpl(au[i].getAuthority());
        }

        public String getName() {
            return name;
        }

        public Object getPrincipal() {
            return principal;
        }

        public GrantedAuthority[] getAuthorities() {
            return authorities;
        }

        public boolean isAuthenticated() {
            return true;    // assumed to be authenticated by the caller
        }

        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // noop
        }

        public Object getCredentials() {
            return null;
        }

        public Object getDetails() {
            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}
