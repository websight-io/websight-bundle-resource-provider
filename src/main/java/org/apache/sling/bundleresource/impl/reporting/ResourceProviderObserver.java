package org.apache.sling.bundleresource.impl.reporting;

import org.apache.sling.bundleresource.impl.BundleResourceProvider;

public class ResourceProviderObserver {

    private final ResourceChangeReporter reporter = new ResourceChangeReporter();

    public void serviceAdded(BundleResourceProvider provider) {
        reporter.reportResourceAddedChanges(provider);
    }

    private void serviceRemoved(BundleResourceProvider provider) {
        reporter.reportResourceRemovedChanges(provider);
    }
}
