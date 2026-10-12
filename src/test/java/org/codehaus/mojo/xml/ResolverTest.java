package org.codehaus.mojo.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResolverTest {
    @Test
    void testWindowsDriveLetterEscapementUpperCase() {
        String given = "file:D:/a/share/folder";
        String expected = "file:/D:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }

    @Test
    void testWindowsDriveLetterEscapementLowerCase() {
        String given = "file:z:/a/share/folder";
        String expected = "file:/z:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }

    @Test
    void testWindowsDriveLetterEscapementForNull() {
        String given = null;
        String expected = null;

        String resolved = Resolver.escapeWindowsDriveLetter(given);

        assertEquals(expected, resolved);
    }
}
