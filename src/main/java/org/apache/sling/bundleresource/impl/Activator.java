/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.bundleresource.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.bundleresource.impl.reporting.ResourceProviderObserver;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator, BundleListener {


    /**
     * The name of the bundle manifest header listing the resource provider root
     * paths provided by the bundle (value is "Sling-Bundle-Resources").
     */
    public static final String BUNDLE_RESOURCE_ROOTS = "Sling-Bundle-Resources";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<Long, BundleResourceProvider[]> bundleResourceProviderMap = new HashMap<>();
    private ResourceProviderObserver resourceProviderObserver;

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(final BundleContext context) throws InvalidSyntaxException {
        context.addBundleListener(this);
        this.resourceProviderObserver = new ResourceProviderObserver(context);
        final Bundle[] bundles = context.getBundles();
        for (final Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                // add bundle resource provider for active bundles
                addBundleResourceProvider(bundle);
            }
        }
        BundleResourceWebConsolePlugin.initPlugin(context);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) {
        BundleResourceWebConsolePlugin.destroyPlugin();

        context.removeBundleListener(this);
        for (final BundleResourceProvider[] providers : this.bundleResourceProviderMap.values()) {
            for (final BundleResourceProvider provider : providers) {
                try {
                    provider.unregisterService();
                } catch (final IllegalStateException ise) {
                    // might happen on shutdown
                }
            }
        }
        this.resourceProviderObserver.close(context);
        this.bundleResourceProviderMap.clear();
    }

    /**
     * Loads and unloads any components provided by the bundle whose state
     * changed. If the bundle has been started, the components are loaded. If
     * the bundle is about to stop, the components are unloaded.
     *
     * @param event The <code>BundleEvent</code> representing the bundle state
     *              change.
     */
    @Override
    public void bundleChanged(final BundleEvent event) {
        int type = event.getType();
        if (type == BundleEvent.STARTED) {
            // register resource provider for the started bundle
            addBundleResourceProvider(event.getBundle());
        } else if (type == BundleEvent.STOPPED) {
            // remove resource provider after the bundle has stopped
            removeBundleResourceProvider(event.getBundle());
        }
    }

    // ---------- Bundle provided resources -----------------------------------

    private void addBundleResourceProvider(final Bundle bundle) {
        BundleResourceProvider[] providers = null;
        try {
            synchronized (this) {
                // on startup we might get here twice for a bundle (listener and activator)
                if (bundleResourceProviderMap.get(bundle.getBundleId()) != null) {
                    return;
                }
                final String prefixes = bundle.getHeaders().get(BUNDLE_RESOURCE_ROOTS);
                if (prefixes != null) {
                    log.debug("addBundleResourceProvider: Registering resources '{}' for bundle {}:{} ({}) as service ",
                            prefixes, bundle.getSymbolicName(), bundle.getVersion(), bundle.getBundleId());

                    final PathMapping[] roots = PathMapping.getRoots(prefixes);
                    providers = new BundleResourceProvider[roots.length];

                    int index = 0;
                    final BundleResourceCache cache = new BundleResourceCache(bundle);
                    for (final PathMapping path : roots) {
                        final BundleResourceProvider provider = new BundleResourceProvider(cache, path);
                        providers[index] = provider;
                        index++;
                    }
                    bundleResourceProviderMap.put(bundle.getBundleId(), providers);
                }
            }
            if (providers != null) {
                for (final BundleResourceProvider provider : providers) {
                    final long id = provider.registerService();
                    log.debug("addBundleResourceProvider: Service ID = {}", id);
                    resourceProviderObserver.serviceAdded(bundle.getBundleContext(), provider, id);
                }
            }
        } catch (final Exception ex) {
            log.error("activate: Problem while registering bundle resources for bundle {} : {} ({})",
                    bundle.getSymbolicName(), bundle.getVersion(), bundle.getBundleId(), ex);
        }
    }

    private void removeBundleResourceProvider(final Bundle bundle) {
        final BundleResourceProvider[] providers;
        synchronized (this) {
            providers = bundleResourceProviderMap.remove(bundle.getBundleId());
        }
        if (providers != null) {
            log.debug("removeBundleResourceProvider: Unregistering resources for bundle {}:{} ({})",
                    bundle.getSymbolicName(), bundle.getVersion(), bundle.getBundleId());
            for (final BundleResourceProvider provider : providers) {
                try {
                    provider.unregisterService();
                } catch (final IllegalStateException ise) {
                    // might happen on shutdown
                }
            }
        }
    }
}
