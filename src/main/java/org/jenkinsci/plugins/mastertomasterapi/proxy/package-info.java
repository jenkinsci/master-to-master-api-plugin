/**
 * Defines a protocol independent RPC semantics.
 *
 * This abstraction allows us to potentially support transport mechanisms that are different from {@link Channel},
 * which allows Jenkins masters to talk to each other with limited trust between them.
 */
package org.jenkinsci.plugins.mastertomasterapi.proxy;
