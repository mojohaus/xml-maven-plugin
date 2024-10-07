package org.codehaus.mojo.xml;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Abstract base class for the plugins Mojo's.
 */
public abstract class AbstractXmlMojo extends AbstractMojo {

    /**
     * The Maven Project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The Maven Settings.
     */
    @Parameter(defaultValue = "${settings}", required = true, readonly = true)
    private Settings settings;

    /**
     * The base directory, relative to which directory names are interpreted.
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File basedir;

    /**
     * Whether to skip execution.
     *
     * @since 1.0.1
     */
    @Parameter(property = "xml.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(readonly = true, defaultValue = "${plugin.artifacts}")
    private List<Artifact> pluginDependencies;

    /**
     * An XML catalog file, or URL, which is being used to resolve entities.  See
     * <a href="examples/catalog.html">Catalog files</a>.
     *
     * @since 1.0
     */
    @Parameter
    private String[] catalogs;

    /**
     * Class loader which wraps resources available to the plugin.
     */
    private ClassLoader classLoader;

    public enum CatalogHandling {
        /**
         * Unmatched entities are resolved through URI resolution
         */
        passThrough,
        /**
         * Unmatched entities are resolved through file only URI resolution
         */
        local,
        /**
         * Unmatched entities generate an error
         */
        strict
    }

    /**
     * How to handle entities which cannot be found in any catalog. See
     * <a href="common-properties.html">Common Goal Properties</a>.
     *
     * @since 1.0.2
     */
    @Parameter(property = "xml.catalogHandling", defaultValue = "passThrough")
    private CatalogHandling catalogHandling;

    /**
     * Plexus resource manager used to obtain XSL.
     */
    @Component
    private ResourceManager locator;

    private boolean locatorInitialized;

    public AbstractXmlMojo() {}

    /**
     * Returns the maven project.
     */
    protected MavenProject getProject() {
        return project;
    }

    /**
     * Returns the projects base directory.
     */
    protected File getBasedir() {
        return basedir;
    }

    /**
     * Converts the given file into an file with an absolute path.
     */
    protected File asAbsoluteFile(File f) {
        if (f.isAbsolute()) {
            return f;
        }
        return new File(getBasedir(), f.getPath());
    }

    /**
     * Attempt to convert given uri into a classpath resource.
     *
     * Resources are looked up using plugin classpath.
     *
     * @param catalog Catalog location.
     * @return URL of resource or null if not found.
     */
    private URL asClasspath(String catalog) {
        return getClassLoader().getResource(catalog);
    }

    /**
     * Returns the plugins catalog files.
     */
    protected void setCatalogs(List<File> pCatalogFiles, List<URL> pCatalogUrls) throws MojoExecutionException {
        if (catalogs == null || catalogs.length == 0) {
            return;
        }

        for (int i = 0; i < catalogs.length; i++) {
            try {
                URL url = new URL(catalogs[i]);
                pCatalogUrls.add(url);
            } catch (MalformedURLException e) {
                URL classpath = asClasspath(catalogs[i]);
                if (classpath != null) {
                    pCatalogUrls.add(classpath);
                    continue;
                }
                File absoluteCatalog = asAbsoluteFile(new File(catalogs[i]));
                if (!absoluteCatalog.exists() || !absoluteCatalog.isFile()) {
                    throw new MojoExecutionException("That catalog does not exist:" + absoluteCatalog.getPath(), e);
                }
                pCatalogFiles.add(absoluteCatalog);
            }
        }
    }

    /**
     * Creates a new resolver.
     */
    protected Resolver getResolver() throws MojoExecutionException {
        List<File> catalogFiles = new ArrayList<File>();
        List<URL> catalogUrls = new ArrayList<URL>();
        setCatalogs(catalogFiles, catalogUrls);

        return new Resolver(
                getBasedir(), catalogFiles, catalogUrls, getLocator(), catalogHandling, getLog().isDebugEnabled());
    }

    /**
     * Scans a directory for files and returns a set of path names.
     */
    protected String[] getFileNames(File pDir, String[] pIncludes, String[] pExcludes)
            throws MojoFailureException, MojoExecutionException {
        if (pDir == null) {
            throw new MojoFailureException(
                    "A ValidationSet or TransformationSet" + " requires a nonempty 'dir' child element.");
        }
        final File dir = asAbsoluteFile(pDir);
        if (!dir.isDirectory()) {
            throw new MojoExecutionException("The directory " + dir.getPath()
                    + ", which is a base directory of a ValidationSet or TransformationSet, does not exist.");
        }
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(dir);
        if (pIncludes != null && pIncludes.length > 0) {
            ds.setIncludes(pIncludes);
        }
        if (pExcludes != null && pExcludes.length > 0) {
            ds.setExcludes(pExcludes);
        }
        ds.scan();
        return ds.getIncludedFiles();
    }

    /**
     * Converts the given set of file names into a set of {@link File} instances. The file names may be relative to the
     * given base directory.
     */
    protected File[] asFiles(File pDir, String[] pFileNames) {
        if (pFileNames == null) {
            return new File[0];
        }

        File dir = asAbsoluteFile(pDir);
        File[] result = new File[pFileNames.length];
        for (int i = 0; i < pFileNames.length; i++) {
            result[i] = new File(dir, pFileNames[i]);
        }
        return result;
    }

    /**
     * Scans a directory for files and returns a set of {@link File} instances.
     */
    protected File[] getFiles(File pDir, String[] pIncludes, String[] pExcludes)
            throws MojoFailureException, MojoExecutionException {
        return asFiles(pDir, getFileNames(pDir, pIncludes, pExcludes));
    }

    /**
     * Calculates the exclusions to use when searching files.
     */
    protected String[] getExcludes(String[] origExcludes, boolean skipDefaultExcludes) {
        if (skipDefaultExcludes) {
            return origExcludes;
        }

        String[] defaultExcludes = FileUtils.getDefaultExcludes();
        if (origExcludes == null || origExcludes.length == 0) {
            return defaultExcludes;
        }
        String[] result = new String[origExcludes.length + defaultExcludes.length];
        System.arraycopy(origExcludes, 0, result, 0, origExcludes.length);
        System.arraycopy(defaultExcludes, 0, result, origExcludes.length, defaultExcludes.length);
        return result;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private void setProperty(List<String> pProperties, String pKey, String pValue) {
        if (pProperties != null) {
            pProperties.add(pKey);
            pProperties.add(System.getProperty(pKey));
        }
        if (pValue == null) {
            System.getProperties().remove(pKey);
        } else {
            System.setProperty(pKey, pValue);
        }
    }

    /**
     * Called to install the plugins proxy settings.
     */
    protected Object activateProxy() {
        if (settings == null) {
            return null;
        }
        final Proxy proxy = settings.getActiveProxy();
        if (proxy == null) {
            return null;
        }

        final List<String> properties = new ArrayList<String>();
        final String protocol = proxy.getProtocol();
        final String prefix = isEmpty(protocol) ? "" : (protocol + ".");

        final String host = proxy.getHost();
        final String hostProperty = prefix + "proxyHost";
        final String hostValue = isEmpty(host) ? null : host;
        setProperty(properties, hostProperty, hostValue);
        final int port = proxy.getPort();
        final String portProperty = prefix + "proxyPort";
        final String portValue = (port == 0 || port == -1) ? null : String.valueOf(port);
        setProperty(properties, portProperty, portValue);
        final String username = proxy.getUsername();
        final String userProperty = prefix + "proxyUser";
        final String userValue = isEmpty(username) ? null : username;
        setProperty(properties, userProperty, userValue);
        final String password = proxy.getPassword();
        final String passwordProperty = prefix + "proxyPassword";
        final String passwordValue = isEmpty(password) ? null : password;
        setProperty(properties, passwordProperty, passwordValue);
        final String nonProxyHosts = proxy.getNonProxyHosts();
        final String nonProxyHostsProperty = prefix + "nonProxyHosts";
        final String nonProxyHostsValue = isEmpty(nonProxyHosts) ? null : nonProxyHosts.replace(',', '|');
        setProperty(properties, nonProxyHostsProperty, nonProxyHostsValue);
        getLog().debug("Proxy settings: " + hostProperty + "=" + hostValue + ", " + portProperty + "=" + portValue
                + ", " + userProperty + "=" + userValue + ", " + passwordProperty + "="
                + (passwordValue == null ? "null" : "<PasswordNotLogged>") + ", " + nonProxyHostsProperty + "="
                + nonProxyHostsValue);
        return properties;
    }

    /**
     * Called to restore the proxy settings, which have been installed when the plugin was invoked.
     */
    protected void passivateProxy(Object pProperties) {
        if (pProperties == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        final List<String> properties = (List<String>) pProperties;
        for (Iterator<String> iter = properties.iterator(); iter.hasNext(); ) {
            final String key = iter.next();
            final String value = iter.next();
            setProperty(null, key, value);
        }
    }

    protected URL getResource(String pResource) throws MojoFailureException {
        try {
            return getLocator().getResource(pResource).getURL();
        } catch (ResourceNotFoundException exception) {
            throw new MojoFailureException("Could not find stylesheet: " + pResource);
        } catch (IOException e) {
            throw new MojoFailureException("Error while locating resource: " + pResource);
        }
    }

    protected ResourceManager getLocator() {
        if (!locatorInitialized) {
            locator.addSearchPath(FileResourceLoader.ID, getBasedir().getAbsolutePath());
            locatorInitialized = true;
        }
        return locator;
    }

    protected boolean isSkipping() {
        return skip;
    }

    void checkCatalogHandling() throws MojoFailureException {
        if (getCatalogHandling() == null) {
            throw new MojoFailureException("Illegal value for catalogHandling parameter");
        }
    }

    protected CatalogHandling getCatalogHandling() {
        return catalogHandling;
    }

    private ClassLoader getClassLoader() {
        if (classLoader == null) {
            List<URL> urls = new ArrayList<>();
            for (Artifact artifact : pluginDependencies) {
                try {
                    urls.add(artifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        }
        return classLoader;
    }
}
