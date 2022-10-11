package org.apache.sling.bundleresource.impl.reporting;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Simple Resolver Context used internally to collect resources provided by the bundles.
 */
public class SimpleResolveContext implements ResolveContext<Object> {

    public static final ResolveContext<Object> INSTANCE = new SimpleResolveContext();

    private SimpleResolveContext() {
        // No instances
    }

    @Override
    @NotNull
    public ResourceResolver getResourceResolver() {
        return new ResourceResolverStub();
    }

    @Override
    public Object getProviderState() {
        return null;
    }

    @Override
    public ResolveContext<?> getParentResolveContext() {
        return null;
    }

    @Override
    public ResourceProvider<?> getParentResourceProvider() {
        return null;
    }
}
