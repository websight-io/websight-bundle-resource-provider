package org.apache.sling.bundleresource.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.bundleresource.impl.url.ResourceURLStreamHandler;
import org.apache.sling.bundleresource.impl.url.ResourceURLStreamHandlerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class BundleResourceTest {

    BundleResourceCache getBundleResourceCache() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getLastModified()).thenReturn(System.currentTimeMillis());

        BundleResourceCache cache = mock(BundleResourceCache.class);
        when(cache.getBundle()).thenReturn(bundle);

        return cache;
    }

    @Before
    public void setup() {
        ResourceURLStreamHandlerFactory.init();
    }

    @After
    public void finish() {
        ResourceURLStreamHandler.reset();
    }

    void addContent(BundleResourceCache cache, String path, Map<String, Object> content) throws IOException {
        final URL url = new URL("resource:" + path);

        ResourceURLStreamHandler.addJSON(path, content);
        when(cache.getEntry(path)).thenReturn(url);
    }

    void addContent(BundleResourceCache cache, String path, String content) throws IOException {
        final URL url = new URL("resource:" + path);

        ResourceURLStreamHandler.addContents(path, content);
        when(cache.getEntry(path)).thenReturn(url);
    }

    @Test
    public void testFileResource() throws MalformedURLException {
        final BundleResourceCache cache = getBundleResourceCache();
        when(cache.getEntry("/libs/foo/test.json")).thenReturn(new URL("file:/libs/foo/test.json"));
        final BundleResource rsrc = new BundleResource(null, cache,
                new PathMapping("/libs/foo", null, null), "/libs/foo/test.json", null, false);
        assertEquals(JcrConstants.NT_FILE, rsrc.getResourceType());
        assertNull(rsrc.getResourceSuperType());
        final ValueMap vm = rsrc.getValueMap();
        assertNull(vm.get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class));
    }

    @Test
    public void testJSONResource() throws IOException {
        final BundleResourceCache cache = getBundleResourceCache();
        addContent(cache, "/libs/foo/test.json", Collections.singletonMap("test", (Object) "foo"));
        final BundleResource rsrc = new BundleResource(null, cache,
                new PathMapping("/libs/foo", null, "json"), "/libs/foo/test", null, false);
        assertEquals(JcrConstants.NT_FILE, rsrc.getResourceType());
        assertNull(rsrc.getResourceSuperType());
        final ValueMap vm = rsrc.getValueMap();
        assertNull(vm.get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class));
        assertEquals("foo", vm.get("test", String.class));
    }

    /**
     * SLING-10140 - Verify that when the resourceRoot is a mapped file, that the sibling entry with the
     * JSONPropertiesExtension is loaded
     */
    @Test
    public void testJSONResourceForMappedFile() throws IOException {
        final BundleResourceCache cache = getBundleResourceCache();
        addContent(cache, "/SLING_INF/libs/foo/test.txt", "Hello Text");
        addContent(cache, "/SLING-INF/libs/foo/test.txt.json", Collections.singletonMap("test", (Object) "foo"));
        final BundleResource rsrc = new BundleResource(null, cache,
                new PathMapping("/libs/foo/test.txt", "/SLING-INF/libs/foo/test.txt", "json"), "/libs/foo/test.txt", null, false);
        assertEquals(JcrConstants.NT_FILE, rsrc.getResourceType());
        assertNull(rsrc.getResourceSuperType());
        final ValueMap vm = rsrc.getValueMap();
        assertNull(vm.get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class));
        assertEquals("foo", vm.get("test", String.class));
    }

    @Test
    public void testDefaultJSONResource() throws IOException {
        final BundleResourceCache cache = getBundleResourceCache();
        addContent(cache, "/libs/foo/test/.json", Collections.singletonMap("test", (Object) "foo"));
        final BundleResource rsrc = new BundleResource(null, cache,
                new PathMapping("/libs/foo", null, "json"), "/libs/foo/test", null, true);
        assertEquals(JcrConstants.NT_FOLDER, rsrc.getResourceType());
        assertNull(rsrc.getResourceSuperType());
        final ValueMap vm = rsrc.getValueMap();
        assertNull(vm.get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class));
        assertEquals("foo", vm.get("test", String.class));
    }

    @Test
    public void testSkipSettingResourceTypeForFile() {
        testSkipSettingResourceType(false, JcrConstants.NT_FILE, getBundleResourceCache());
    }

    @Test
    public void testSkipSettingResourceTypeForFolder() {
        testSkipSettingResourceType(true, JcrConstants.NT_FOLDER, getBundleResourceCache());
    }

    private static void testSkipSettingResourceType(final boolean isFolder, final String resourceType, final BundleResourceCache cache) {
        final BundleResource rsrc = new BundleResource(null, cache,
                PathMapping.create("/", "json"), "/libs/foo/test.txt", null, isFolder);
        final ValueMap vm = rsrc.getValueMap();

        assertEquals(resourceType, rsrc.getResourceType());
        assertNull(vm.get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class));
    }

}
