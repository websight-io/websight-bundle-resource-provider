package org.apache.sling.bundleresource.impl.url;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ResourceURLConnection extends URLConnection {

    private final String contents;

    protected ResourceURLConnection(final URL url, final String contents) {
        super(url);
        this.contents = contents;
    }

    @Override
    public void connect() throws IOException {
        if (contents == null) {
            throw new IOException("404");
        }
    }

    @Override
    public int getContentLength() {
        return contents.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.contents.getBytes("UTF-8"));
    }
}