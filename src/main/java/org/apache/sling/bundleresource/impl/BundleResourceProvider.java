package org.apache.sling.bundleresource.impl;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.apache.sling.spi.resource.provider.ProviderContext;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class BundleResourceProvider extends ResourceProvider<Object> {

    public static final String PROP_BUNDLE = BundleResourceProvider.class.getName();

    /**
     * The cache with the bundle providing the resources
     */
    private final BundleResourceCache cache;

    /**
     * The root path
     */
    private final PathMapping root;

    @SuppressWarnings("rawtypes")
    private volatile ServiceRegistration<ResourceProvider> serviceRegistration;

    /**
     * Creates Bundle resource provider accessing entries in the given Bundle an
     * supporting resources below root paths given by the rootList which is a
     * comma (and whitespace) separated list of absolute paths.
     */
    public BundleResourceProvider(final BundleResourceCache cache, final PathMapping root) {
        this.cache = cache;
        this.root = root;
    }

    //---------- Service Registration

    long registerService() {
        final Bundle bundle = this.cache.getBundle();
        final Dictionary<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_DESCRIPTION,
                "Provider of bundle based resources from bundle " + bundle.getBundleId());
        props.put(Constants.SERVICE_VENDOR, "The Apache Software Foundation");
        props.put(ResourceProvider.PROPERTY_ROOT, this.root.getResourceRoot());
        props.put(PROP_BUNDLE, bundle.getBundleId());

        serviceRegistration = bundle.getBundleContext().registerService(ResourceProvider.class, this, props);
        return (Long) serviceRegistration.getReference().getProperty(Constants.SERVICE_ID);
    }

    void unregisterService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
            } catch (final IllegalStateException ise) {
                // this might happen on shutdown, so ignore
            }
            serviceRegistration = null;
        }
    }

    // ---------- ResourceProvider interface

    /**
     * Returns a BundleResource for the path if such an entry exists in the
     * bundle of this provider.
     */
    @Override
    public Resource getResource(final ResolveContext<Object> ctx,
                                final String resourcePath,
                                final ResourceContext resourceContext,
                                final Resource parent) {
        final PathMapping mappedPath = getMappedPath(resourcePath);
        if (mappedPath != null) {
            final String entryPath = mappedPath.getEntryPath(resourcePath);

            // first try, whether the bundle has an entry with a trailing slash
            // which would be a folder. In this case we check whether the
            // repository contains an item with the same path. If so, we
            // don't create a BundleResource but instead return null to be
            // able to return an item-based resource
            URL entry = cache.getEntry(entryPath.concat("/"));
            final boolean isFolder = entry != null;

            // if there is no entry with a trailing slash, try plain name
            // which would then of course be a file
            if (entry == null) {
                entry = cache.getEntry(entryPath);
                if (entry == null) {
                    entry = cache.getEntry(entryPath + this.root.getJSONPropertiesExtension());
                }
            }

            // here we either have a folder for which no same-named item exists
            // or a bundle file
            if (entry != null) {
                // check if a JSON props file is directly requested
                // if so, we deny the access
                if (this.root.getJSONPropertiesExtension() == null
                        || !entryPath.endsWith(this.root.getJSONPropertiesExtension())) {

                    return new BundleResource(ctx.getResourceResolver(),
                            cache,
                            mappedPath,
                            resourcePath,
                            null,
                            isFolder);
                }
            }

            // the bundle does not contain the path
            // if JSON is enabled check for any parent
            String parentPath = ResourceUtil.getParent(resourcePath);
            while (parentPath != null) {
                final Resource rsrc = getResource(ctx, parentPath, resourceContext, null);
                if (rsrc != null) {
                    final Resource childResource = ((BundleResource) rsrc).getChildResource(resourcePath.substring(parentPath.length() + 1));
                    if (childResource != null) {
                        return childResource;
                    }
                }
                parentPath = ResourceUtil.getParent(parentPath);
                if (parentPath != null && this.getMappedPath(parentPath) == null) {
                    parentPath = null;
                }
            }
        }

        return null;
    }

    @Override
    public Iterator<Resource> listChildren(final ResolveContext<Object> ctx, final Resource parent) {
        if (parent instanceof BundleResource && ((BundleResource) parent).getBundle() == this.cache) {
            // bundle resources can handle this request directly when the parent
            // resource is in the same bundle as this provider.
            return new BundleResourceIterator((BundleResource) parent);
        }

        // ensure this provider may have children of the parent
        String parentPath = parent.getPath();
        PathMapping mappedPath = getMappedPath(parentPath);
        if (mappedPath != null) {
            return new BundleResourceIterator(parent.getResourceResolver(),
                    cache, mappedPath, parentPath, null);
        }

        // the parent resource cannot have children in this provider,
        // though this is basically not expected, we still have to
        // be prepared for such a situation
        return null;
    }

    @Nullable
    public ObservationReporter getObservationReporter() {
        final ProviderContext ctx = this.getProviderContext();
        if (ctx != null) {
            return ctx.getObservationReporter();
        }
        return null;
    }

    // ---------- Web Console plugin support

    BundleResourceCache getBundleResourceCache() {
        return cache;
    }

    public PathMapping getMappedPath() {
        return root;
    }

    // ---------- internal

    private PathMapping getMappedPath(final String resourcePath) {
        if (this.root.isChild(resourcePath)) {
            return root;
        }

        return null;
    }
}
