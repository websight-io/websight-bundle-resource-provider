package org.apache.sling.bundleresource.impl;

import static org.apache.sling.api.resource.observation.ResourceChange.ChangeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.bundleresource.impl.event.SimpleResolveContext;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.apache.sling.spi.resource.provider.ObserverConfiguration;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceChangeEventsEmitter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void emitResourceRemovedEvents(BundleResourceProvider provider) {
        emitResourceEvents(provider, ChangeType.REMOVED);
    }

    public void emitResourceAddedEvents(BundleResourceProvider provider) {
        emitResourceEvents(provider, ChangeType.ADDED);
    }

    private void emitResourceEvents(BundleResourceProvider provider, ChangeType changeType) {
        final String resourceRoot = provider.getMappedPath().getResourceRoot();
        final Resource root = provider.getResource(SimpleResolveContext.INSTANCE, resourceRoot, ResourceContext.EMPTY_CONTEXT, null);
        final ObservationReporter reporter = provider.getObservationReporter();
        if (reporter != null) {
            sendEvents(root, changeType, provider, reporter);
        } else {
            log.debug("getObservationReporter is null");
        }
    }

    private void sendEvents(final Resource root, final ChangeType changeType,
                            final BundleResourceProvider provider, final ObservationReporter reporter) {
        if (log.isDebugEnabled()) {
            log.debug("Detected change for resource {} : {}", root.getPath(), changeType);
        }

        final List<ResourceChange> allChanges = new ArrayList<>();
        for (final ObserverConfiguration config : reporter.getObserverConfigurations()) {
            collectResourceChanges(root, changeType, provider, allChanges);
            final List<ResourceChange> perConfigChanges = allChanges.stream()
                    .filter(change -> config.matches(change.getPath()))
                    .filter(change -> config.getChangeTypes().contains(change.getType()))
                    .collect(Collectors.toList());
            reporter.reportChanges(config, perConfigChanges, false);
            if (log.isDebugEnabled()) {
                for (ResourceChange change : perConfigChanges)
                    log.debug("Send change for resource {}: {} to {}", change.getPath(), change.getType(), config);
            }
        }
    }

    private void collectResourceChanges(final Resource resource, final ResourceChange.ChangeType changeType,
                                        BundleResourceProvider provider, List<ResourceChange> changes) {
        changes.add(new ResourceChange(changeType, resource.getPath(), false));
        Iterator<Resource> children = provider.listChildren(SimpleResolveContext.INSTANCE, resource);
        children.forEachRemaining(child -> {
            collectResourceChanges(child, changeType, provider, changes);
        });
    }
}
