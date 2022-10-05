package org.apache.sling.bundleresource.impl.reporting;

import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.bundleresource.impl.BundleResourceProvider;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceProviderObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProviderObserver.class);
    private final ResourceChangeReporter reporter = new ResourceChangeReporter();

    public void serviceAdded(BundleContext bundleContext, BundleResourceProvider provider, long serviceId) throws InvalidSyntaxException {
        reporter.reportResourceChanges(provider, ResourceChange.ChangeType.ADDED, null);
        LOGGER.debug("Reported resources added by the provider");

        bundleContext.addServiceListener(new ServiceListenerWithFallbackObserver(provider, reporter)
                , "(" + Constants.SERVICE_ID + "=" + serviceId + " )");
    }

    private static class ServiceListenerWithFallbackObserver implements ServiceListener {

        private final ObservationReporter fallbackReporter;
        private final BundleResourceProvider provider;
        private final ResourceChangeReporter reporter;

        ServiceListenerWithFallbackObserver(BundleResourceProvider provider, ResourceChangeReporter reporter) {
            this.fallbackReporter = provider.getObservationReporter();
            this.provider = provider;
            this.reporter = reporter;
        }

        @Override
        public void serviceChanged(ServiceEvent event) {
            if (ServiceEvent.UNREGISTERING == event.getType()) {
                reporter.reportResourceChanges(provider, ResourceChange.ChangeType.REMOVED, fallbackReporter);
                LOGGER.debug("Reported resources removed by the provider");
            }
        }
    }
}
