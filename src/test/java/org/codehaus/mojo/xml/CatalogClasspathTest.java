package org.codehaus.mojo.xml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for catalog loading from classpath
 */
public class CatalogClasspathTest {

    @Test
    public void testClasspathCatalogLoading() throws Exception {
        // Create a mock AbstractXmlMojo to test setCatalogs
        TestableAbstractXmlMojo mojo = new TestableAbstractXmlMojo();

        // Set catalog to be loaded from classpath
        mojo.setCatalogArray(new String[] {"catalogs/test-catalog.xml"});

        List<File> catalogFiles = new ArrayList<File>();
        List<URL> catalogUrls = new ArrayList<URL>();

        // Execute setCatalogs which should find the catalog on classpath
        mojo.setCatalogs(catalogFiles, catalogUrls);

        // Verify that catalog was found on classpath and added to URLs
        assertEquals("Should have no file-based catalogs", 0, catalogFiles.size());
        assertEquals("Should have one URL-based catalog from classpath", 1, catalogUrls.size());

        // Verify the URL points to our test catalog
        URL catalogUrl = catalogUrls.get(0);
        assertNotNull("Catalog URL should not be null", catalogUrl);
        assertTrue(
                "Catalog URL should contain test-catalog.xml",
                catalogUrl.toString().contains("test-catalog.xml"));
    }

    @Test
    public void testFileCatalogStillWorksWhenNotOnClasspath() throws Exception {
        // Create a temporary catalog file
        File tempCatalog = File.createTempFile("catalog-", ".xml");
        tempCatalog.deleteOnExit();

        TestableAbstractXmlMojo mojo = new TestableAbstractXmlMojo();
        mojo.setBasedir(tempCatalog.getParentFile());
        mojo.setCatalogArray(new String[] {tempCatalog.getName()});

        List<File> catalogFiles = new ArrayList<File>();
        List<URL> catalogUrls = new ArrayList<URL>();

        mojo.setCatalogs(catalogFiles, catalogUrls);

        // File-based catalog should still work
        assertEquals("Should have one file-based catalog", 1, catalogFiles.size());
        assertEquals("Should have no URL-based catalogs", 0, catalogUrls.size());
    }

    @Test
    public void testUrlCatalogStillWorks() throws Exception {
        TestableAbstractXmlMojo mojo = new TestableAbstractXmlMojo();
        mojo.setCatalogArray(new String[] {"http://example.com/catalog.xml"});

        List<File> catalogFiles = new ArrayList<File>();
        List<URL> catalogUrls = new ArrayList<URL>();

        mojo.setCatalogs(catalogFiles, catalogUrls);

        // URL catalog should be recognized
        assertEquals("Should have no file-based catalogs", 0, catalogFiles.size());
        assertEquals("Should have one URL-based catalog", 1, catalogUrls.size());
        assertEquals(
                "URL should match",
                "http://example.com/catalog.xml",
                catalogUrls.get(0).toString());
    }

    @Test(expected = MojoExecutionException.class)
    public void testNonExistentCatalogThrowsException() throws Exception {
        TestableAbstractXmlMojo mojo = new TestableAbstractXmlMojo();
        mojo.setCatalogArray(new String[] {"nonexistent-catalog.xml"});

        List<File> catalogFiles = new ArrayList<File>();
        List<URL> catalogUrls = new ArrayList<URL>();

        // Should throw exception because catalog doesn't exist as URL, classpath, or file
        mojo.setCatalogs(catalogFiles, catalogUrls);
    }

    /**
     * Testable subclass of AbstractXmlMojo that exposes protected methods
     */
    private static class TestableAbstractXmlMojo extends AbstractXmlMojo {
        private String[] catalogArray;
        private File basedir = new File(".");

        public void setCatalogArray(String[] catalogs) {
            this.catalogArray = catalogs;
        }

        public void setBasedir(File basedir) {
            this.basedir = basedir;
        }

        @Override
        protected File getBasedir() {
            return basedir;
        }

        // Expose setCatalogs for testing
        @Override
        public void setCatalogs(List<File> pCatalogFiles, List<URL> pCatalogUrls) throws MojoExecutionException {
            // Temporarily set the catalogs field via reflection-like approach
            try {
                java.lang.reflect.Field field = AbstractXmlMojo.class.getDeclaredField("catalogs");
                field.setAccessible(true);
                field.set(this, catalogArray);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            super.setCatalogs(pCatalogFiles, pCatalogUrls);
        }

        @Override
        public void execute() throws MojoExecutionException {
            // Not needed for this test
        }
    }
}
