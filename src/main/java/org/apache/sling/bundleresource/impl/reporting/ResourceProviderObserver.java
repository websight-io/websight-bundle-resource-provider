package org.apache.sling.bundleresource.impl.reporting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.bundleresource.impl.BundleResourceProvider;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceProviderObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProviderObserver.class);
    private final ResourceChangeReporter reporter = new ResourceChangeReporter();
    // List of observers registered by this bundle, one per each providers
    private final List<FallbackReporterServiceListener> observers = new CopyOnWriteArrayList<>();
    // Listen for the `ResourceChangeListener` changes to refresh the fallback reporter in each FallbackReporterServiceListeners
    private FallbackRefreshServiceListener refreshServiceListener;

    public ResourceProviderObserver(BundleContext bundleContext) throws InvalidSyntaxException {
        // Listen for ResourceChangeListener services which may affect reporter configuration
        // Try to refresh ObservationReporter with the fresh one from resource provider
        refreshServiceListener = new FallbackRefreshServiceListener();
        bundleContext.addServiceListener(refreshServiceListener,
                "(" + Constants.OBJECTCLASS + "=" + ResourceChangeListener.class.getName() + ")");
    }

    public void serviceAdded(BundleContext bundleContext, BundleResourceProvider provider, long serviceId) throws InvalidSyntaxException {
        // Notify system that the resources are added
        reporter.reportResourceChanges(provider, ResourceChange.ChangeType.ADDED, null);
        LOGGER.debug("Reported changes for resources added by the provider");

        // Listen for the provider removal to notify the system about removed resources
        // Fallback reporter is stored and refreshed by the listener if the provider does not have valid reporter by itself
        // This is caused by the race condition caused by the fact, that we work on the bundle lifecycle events (STARTED/STOPPED)
        FallbackReporterServiceListener observer = new FallbackReporterServiceListener(observers, provider, reporter);
        bundleContext.addServiceListener(observer, "(" + Constants.SERVICE_ID + "=" + serviceId + ")");
    }

    public void close(BundleContext bundleContext) {
        for (FallbackReporterServiceListener observer : observers) {
            bundleContext.removeServiceListener(observer);
        }
        if (refreshServiceListener != null) {
            bundleContext.removeServiceListener(refreshServiceListener);
        }
    }

    private class FallbackRefreshServiceListener implements ServiceListener {

        @Override
        public void serviceChanged(ServiceEvent event) {
            for (FallbackReporterServiceListener observer : observers) {
                observer.refreshFallback();
            }
        }
    }

    private static class FallbackReporterServiceListener implements ServiceListener {

        private ObservationReporter fallbackReporter;
        private final BundleResourceProvider provider;
        private final ResourceChangeReporter reporter;
        private final List<FallbackReporterServiceListener> observers;


        FallbackReporterServiceListener(List<FallbackReporterServiceListener> observers, BundleResourceProvider provider, ResourceChangeReporter reporter) {
            this.observers = observers;
            this.provider = provider;
            this.reporter = reporter;

            this.fallbackReporter = provider.getObservationReporter();
            observers.add(this);
        }

        @Override
        public void serviceChanged(ServiceEvent event) {
            if (ServiceEvent.UNREGISTERING == event.getType()) {
                reporter.reportResourceChanges(provider, ResourceChange.ChangeType.REMOVED, fallbackReporter);
                // Unregister listener and remove from the observers lis
                FrameworkUtil.getBundle(this.getClass()).getBundleContext().removeServiceListener(this);
                observers.remove(this);
                LOGGER.debug("Reported changes for resources removed by the provider. Removed FallbackReporterServiceListener (self).");
            }
        }

        public void refreshFallback() {
            // Hopefully the provider is already updated
            LOGGER.debug("Refreshing fallback reporter for {}", provider.getMappedPath().getResourceRoot());
            this.fallbackReporter = provider.getObservationReporter();
        }
    }
}
