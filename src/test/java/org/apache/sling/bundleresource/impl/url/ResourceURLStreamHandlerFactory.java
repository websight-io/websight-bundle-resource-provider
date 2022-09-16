package org.apache.sling.bundleresource.impl.url;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ResourceURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private static volatile boolean init = false;

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("resource".equals(protocol)) {
            return new ResourceURLStreamHandler();
        }
        return null;
    }

    public static void init() {
        if (!init) {
            URL.setURLStreamHandlerFactory(new ResourceURLStreamHandlerFactory());
            init = true;
        }
    }

}

