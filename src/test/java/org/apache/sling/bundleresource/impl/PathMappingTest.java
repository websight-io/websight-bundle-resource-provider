package org.apache.sling.bundleresource.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PathMappingTest {

    @Test
    public void testSimpleRoot() {
        final PathMapping[] paths = PathMapping.getRoots("/libs/foo");
        assertEquals(1, paths.length);
        assertNull(paths[0].getEntryRoot());
        assertNull(paths[0].getEntryRootPrefix());
        assertEquals("/libs/foo", paths[0].getResourceRoot());
        assertEquals("/libs/foo/", paths[0].getResourceRootPrefix());
        assertNotNull(paths[0].getJSONPropertiesExtension());
    }

    @Test
    public void testDefaultPropJSON() {
        final PathMapping[] paths = PathMapping.getRoots("/libs/foo");
        assertEquals(PathMapping.DEFAULT_JSON_DIR, paths[0].getJSONPropertiesExtension());
    }

    @Test
    public void testSimpleRootWithJSON() {
        final PathMapping[] paths = PathMapping.getRoots("/libs/foo;" + PathMapping.DIR_JSON + ":=json");
        assertEquals(1, paths.length);
        assertNull(paths[0].getEntryRoot());
        assertNull(paths[0].getEntryRootPrefix());
        assertEquals("/libs/foo", paths[0].getResourceRoot());
        assertEquals("/libs/foo/", paths[0].getResourceRootPrefix());
        assertEquals(".json", paths[0].getJSONPropertiesExtension());
    }
}
