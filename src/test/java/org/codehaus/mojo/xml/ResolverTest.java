package org.codehaus.mojo.xml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResolverTest {
    @Test
    public void testWindowsDriveLetterEscapementUpperCase()
    {
        String given = "file:D:/a/share/folder";
        String expected = "file:/D:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter( given );

        assertEquals( expected, resolved );
    }

    @Test
    public void testWindowsDriveLetterEscapementLowerCase()
    {
        String given = "file:z:/a/share/folder";
        String expected = "file:/z:/a/share/folder";

        String resolved = Resolver.escapeWindowsDriveLetter( given );

        assertEquals( expected, resolved );
    }

    @Test
    public void testWindowsDriveLetterEscapementForNull()
    {
        String given = null;
        String expected = null;

        String resolved = Resolver.escapeWindowsDriveLetter( given );

        assertEquals( expected, resolved );
    }
}