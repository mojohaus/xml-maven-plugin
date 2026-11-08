package org.codehaus.mojo.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;
import org.codehaus.plexus.resource.PlexusResource;
import org.codehaus.plexus.resource.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The resources read from remote URLs are downloaded once per resolver if, and only if, asked for (#74).
 */
class ResolverCacheTest {
    private static final String SCHEMA = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>";

    private final AtomicInteger requests = new AtomicInteger();

    private HttpServer server;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/schema.xsd", exchange -> {
            requests.incrementAndGet();
            byte[] body = SCHEMA.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void shouldDownloadOnEveryResolutionByDefault(@TempDir Path dir) throws Exception {
        Resolver resolver = newResolver(dir, false);

        resolveTwice(resolver);

        assertTrue(requests.get() >= 4, "every resolution downloads, but only " + requests.get());
    }

    @Test
    void shouldDownloadOnceWhenCaching(@TempDir Path dir) throws Exception {
        Resolver resolver = newResolver(dir, true);

        resolveTwice(resolver);
        int afterFirst = requests.get();
        resolveTwice(resolver);

        assertEquals(afterFirst, requests.get(), "the second round must not reach the server");
        assertTrue(afterFirst <= 2, "the first round reads the schema at most twice, to probe and to read");
    }

    @Test
    void shouldDownloadOnceForResolveResource(@TempDir Path dir) throws Exception {
        Resolver resolver = newResolver(dir, true);

        String schema = url();
        for (int i = 0; i < 5; i++) {
            LSInput input = resolver.resolveResource(null, null, null, schema, null);
            assertNotNull(input);
            try (InputStream stream = input.getByteStream()) {
                assertEquals(SCHEMA, new String(readAll(stream), StandardCharsets.UTF_8));
            }
        }
        int afterFive = requests.get();
        resolver.resolveResource(null, null, null, schema, null);

        assertEquals(afterFive, requests.get());
        assertTrue(afterFive <= 2, "five resolutions read the schema at most twice");
    }

    private void resolveTwice(Resolver resolver) throws Exception {
        for (int i = 0; i < 2; i++) {
            InputSource source = resolver.resolveEntity(null, url());
            try (InputStream stream = source.getByteStream()) {
                assertEquals(SCHEMA, new String(readAll(stream), StandardCharsets.UTF_8));
            }
        }
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/schema.xsd";
    }

    private static byte[] readAll(InputStream stream) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    /** A resolver whose resource manager hands every name back as a URL, as the URL loader of the plugin does. */
    private static Resolver newResolver(Path dir, boolean cache) throws Exception {
        ResourceManager locator = mock(ResourceManager.class);
        when(locator.getResource(org.mockito.ArgumentMatchers.anyString())).thenAnswer(invocation -> {
            PlexusResource resource = mock(PlexusResource.class);
            when(resource.getURL()).thenReturn(new URL((String) invocation.getArgument(0)));
            return resource;
        });
        return new Resolver(
                dir.toFile(),
                Collections.emptyList(),
                Collections.emptyList(),
                locator,
                AbstractXmlMojo.CatalogHandling.passThrough,
                false,
                cache);
    }
}
