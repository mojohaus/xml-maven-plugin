package org.codehaus.mojo.xml;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolverTest {
    @Test
    void windowsDriveLetterEscapementUpperCase() {
        String given = "file:D:/a/share/folder";
        String expected = "file:/D:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }

    @Test
    void windowsDriveLetterEscapementLowerCase() {
        String given = "file:z:/a/share/folder";
        String expected = "file:/z:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }

    @Test
    void windowsDriveLetterEscapementForNull() {
        String given = null;
        String expected = null;

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }

    /**
     * Each execution loads its own catalogs. A catalog loaded by an earlier execution in the same JVM, for example by
     * another module of the reactor, must not answer for a later one.
     */
    @Test
    void catalogsDoNotLeakBetweenResolvers(@TempDir Path dir) throws Exception {
        File first = catalog(dir, "first", "first.xsd");
        File second = catalog(dir, "second", "second.xsd");

        Resolver resolver1 = new Resolver(
                dir.toFile(),
                Collections.singletonList(first),
                Collections.emptyList(),
                null,
                AbstractXmlMojo.CatalogHandling.passThrough,
                false);
        Resolver resolver2 = new Resolver(
                dir.toFile(),
                Collections.singletonList(second),
                Collections.emptyList(),
                null,
                AbstractXmlMojo.CatalogHandling.passThrough,
                false);

        assertResolvesTo("first/first.xsd", resolver1);
        assertResolvesTo("second/second.xsd", resolver2);
    }

    /**
     * Compares the tail of the system id only: on Windows the resolver may write a drive-letter path as
     * {@code file:C:/...} instead of {@code file:/C:/...}. Closes the stream the resolver opened, as a parser would,
     * so that Windows can delete the temporary directory.
     */
    private static void assertResolvesTo(String expectedTail, Resolver resolver) throws Exception {
        InputSource source = resolver.resolveEntity("-//TEST//SCHEMA", null);
        try (InputStream stream = source.getByteStream()) {
            String systemId = source.getSystemId();
            assertTrue(
                    systemId.endsWith("/" + expectedTail),
                    () -> "expected ..." + expectedTail + " but was " + systemId);
        }
    }

    private static File catalog(Path dir, String name, String schema) throws Exception {
        Path home = Files.createDirectories(dir.resolve(name));
        Files.write(home.resolve(schema), "<schema/>".getBytes(StandardCharsets.UTF_8));
        Path catalog = home.resolve("catalog.xml");
        Files.write(
                catalog,
                ("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">"
                                + "<public publicId=\"-//TEST//SCHEMA\" uri=\"" + schema + "\"/></catalog>")
                        .getBytes(StandardCharsets.UTF_8));
        return catalog.toFile();
    }
}
