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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;

/**
 * An implementation of {@link org.xml.sax.EntityResolver}, {@link URIResolver}, and {@link LSResourceResolver}, based
 * on the Apache catalog resolver.
 */
public class Resolver implements EntityResolver2, URIResolver, LSResourceResolver {
    private final ResourceManager locator;

    private final File baseDir;

    private final CatalogResolver resolver;

    private boolean validating;

    private boolean xincludeAware;

    private final AbstractXmlMojo.CatalogHandling catalogHandling;

    /**
     * Creates a new instance.
     *
     * @param pFiles A set of files with catalog definitions to load
     * @throws MojoExecutionException An error occurred while loading the resolvers catalogs.
     */
    Resolver(
            File pBaseDir,
            List<File> pFiles,
            List<URL> pUrls,
            ResourceManager pLocator,
            AbstractXmlMojo.CatalogHandling catalogHandling,
            boolean pLogging)
            throws MojoExecutionException {
        baseDir = pBaseDir;
        locator = pLocator;
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        if (pLogging) {
            System.err.println("Setting resolver verbosity to maximum.");
            manager.setVerbosity(Integer.MAX_VALUE);
        }
        resolver = new CatalogResolver(manager);
        for (File pFile : pFiles) {
            File file = pFile;
            try {
                resolver.getCatalog().parseCatalog(file.getPath());
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failed to parse catalog file: " + file.getPath() + ": " + e.getMessage(), e);
            }
        }
        for (URL pUrl : pUrls) {
            URL url = pUrl;
            try {
                resolver.getCatalog().parseCatalog(url);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to parse catalog URL: " + url + ": " + e.getMessage(), e);
            }
        }
        this.catalogHandling = catalogHandling;
    }

    /**
     * Implementation of {@link org.xml.sax.EntityResolver#resolveEntity(String, String)}.
     */
    public InputSource resolveEntity(String pPublicId, String pSystemId) throws SAXException, IOException {
        final InputSource source = resolver.resolveEntity(pPublicId, pSystemId);
        if (source != null) {
            return source;
        }

        URL url = resolve(pSystemId);
        if (url != null) {
            return asInputSource(url);
        }
        return null;
    }

    private InputSource asInputSource(URL url) throws IOException {
        InputSource isource = new InputSource(url.openStream());
        isource.setSystemId(url.toExternalForm());
        return isource;
    }

    /**
     * Implementation of {@link URIResolver#resolve(String, String)}.
     */
    public Source resolve(String pHref, String pBase) throws TransformerException {

        URL url = null;

        final Source source = resolver.resolve(pHref, pBase);

        if (source != null) {
            if (xincludeAware) {
                /*
                 * Avoid risky cast use correct resolved systemid
                 * to configure a xinclude aware source.
                 */
                try {
                    url = new URI(source.getSystemId()).toURL();
                } catch (Exception e) {
                    throw new TransformerException(e);
                }
            } else {
                return source;
            }
        }

        if (null == url) {
            url = resolve(
                    pHref); // probably should call new method resolve(String,URI) but left alone for legacy reasons.
        }

        if (url != null) {
            try {
                return asSaxSource(asInputSource(url));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                throw new TransformerException(e);
            }
        }
        return null;
    }

    private Source asSaxSource(InputSource isource) throws SAXException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(validating);
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(xincludeAware);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(this);
        return new SAXSource(xmlReader, isource);
    }

    private LSInput newLSInput(InputSource pSource) {
        final LSInputImpl lsInput = new LSInputImpl();
        lsInput.setByteStream(pSource.getByteStream());
        lsInput.setCharacterStream(pSource.getCharacterStream());
        lsInput.setPublicId(lsInput.getPublicId());
        lsInput.setSystemId(pSource.getSystemId());
        lsInput.setEncoding(pSource.getEncoding());
        return lsInput;
    }

    /**
     * Implementation of {@link LSResourceResolver#resolveResource(String, String, String, String, String)}.
     */
    public LSInput resolveResource(
            String pType, String pNamespaceURI, String pPublicId, String pSystemId, String pBaseURI) {
        pBaseURI = escapeWindowsDriveLetter(pBaseURI);
        if (pPublicId != null) {
            final InputSource isource = resolver.resolveEntity(pPublicId, pSystemId);
            if (isource != null) {
                return newLSInput(isource);
            }
        }
        InputSource isource = resolver.resolveEntity(pNamespaceURI, pSystemId);
        if (isource != null) {
            return newLSInput(isource);
        }
        URI baseURI = null;
        if (pBaseURI != null) {
            try {
                baseURI = new URI(pBaseURI);
            } catch (URISyntaxException ex) {
                baseURI = null; // or perhaps this should be an UndeclaredThrowableException
            }
        }

        URL url = resolve(pSystemId, baseURI);
        if (url != null) {
            try {
                isource = asInputSource(url);
            } catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
        }
        return isource == null ? null : newLSInput(isource);
    }

    static String escapeWindowsDriveLetter(String pBaseURI) {
        return pBaseURI == null ? null : pBaseURI.replaceFirst("file:([a-zA-Z]:)", "file:/$1");
    }

    /**
     * Sets, whether the Resolver should create validating parsers.
     * @param pValidating True, if created parsers should validate. Otherwise false.
     */
    public void setValidating(boolean pValidating) {
        validating = pValidating;
    }

    /**
     * Returns, whether the Resolver should create validating parsers.
     * @return True, if created parsers should validate. Otherwise false.
     */
    public boolean isValidating() {
        return validating;
    }

    private URL resolveAsResource(String pResource) {
        return Thread.currentThread().getContextClassLoader().getResource(pResource);
    }

    private URL resolveAsFile(String pResource) {
        File f = new File(baseDir, pResource);
        if (!f.isFile()) {
            f = new File(pResource);
            if (!f.isFile()) {
                return null;
            }
        }
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private URL resolveAsURL(String pResource, URI pBaseURI) {
        InputStream stream = null;
        try {
            final URL url = new URL(pResource);
            stream = url.openStream();
            stream.close();
            stream = null;
            return url;
        } catch (IOException e) {
            // fall through to relative URI resolution
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    // Ignore me
                }
            }
        }
        try {
            URI resourceASURI = new URI(pResource);
            if (pBaseURI != null && !resourceASURI.isAbsolute() && pBaseURI.isAbsolute()) {
                resourceASURI = pBaseURI.resolve(resourceASURI);
                final URL url = resourceASURI.toURL();
                stream = url.openStream();
                stream.close();
                stream = null;
                return url;
            }
        } catch (URISyntaxException | IOException ex) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    // Ignore me
                }
            }
        }
        return null;
    }

    /**
     * Attempts to resolve the given URI.
     * @param pResource The URI to resolve.
     * @return The URL, which is being referred to by the URI. Null, if no such URL can be found.
     */
    public URL resolve(String pResource) {
        return resolve(pResource, (URI) null);
    }

    private URL resolve(String pResource, URI pBaseURI) {
        if (pResource == null) {
            return null;
        }

        if (pResource.startsWith("resource:")) {
            String res = pResource.substring("resource:".length());
            return resolveAsResource(res);
        }

        URL url = resolveAsResource(pResource);
        if (url == null) {
            url = resolveAsURL(pResource, null); // original style resolution
            if (url == null) {
                url = resolveAsFile(pResource);
            }
            if (url == null) { // relative URL resolution
                url = resolveAsURL(pResource, pBaseURI);
            }
        }

        if (url == null) {
            return null;
        }
        try {
            return locator.getResource(url.toExternalForm()).getURL();
        } catch (ResourceNotFoundException | IOException e) {
            return null;
        }
    }

    /**
     * Implementation of {@link EntityResolver2#getExternalSubset(String, String)}
     */
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    /**
     * Implementation of {@link EntityResolver2#resolveEntity(String, String, String, String)}
     */
    public InputSource resolveEntity(String pName, String pPublicId, String pBaseURI, String pSystemId)
            throws SAXException, IOException {
        final InputSource source = resolver.resolveEntity(pPublicId, pSystemId);
        if (source != null) {
            return source;
        }
        URI baseURI = null;
        if (pBaseURI != null) {
            try {
                baseURI = new URI(pBaseURI);
            } catch (URISyntaxException ex) {
                throw new SAXException("Incorrectly formatted base URI", ex);
            }
        }
        URL url = resolve(pSystemId, baseURI);
        if (url != null) {
            return asInputSource(url);
        }
        return null;
    }

    /**
     * Returns, whether the transformer should create xinclude aware XML parsers for reading XML documents. The default
     * value is false.
     * @return True, if transformers parser should be xinclud aware. Otherwise false.
     */
    public boolean isXincludeAware() {
        return xincludeAware;
    }

    /**
     * Sets, whether the transformer should create xinclude aware XML parsers for reading XML documents. The default value
     * is false.
     * @param pXIncludeAware True, if transformers parser should be xinclud aware. Otherwise false.
     */
    public void setXincludeAware(boolean pXIncludeAware) {
        xincludeAware = pXIncludeAware;
    }

    String filterPossibleURI(String pResource) {
        switch (catalogHandling) {
            case strict:
                return null;
            case local:
                try {
                    URI resourceAsURI = new URI(pResource);
                    String scheme = resourceAsURI.getScheme();
                    if (scheme == null || "file".equals(scheme)) {
                        break;
                    }
                    return null;
                } catch (URISyntaxException ex) {
                    return null;
                }
            default:
                break;
        }
        return pResource;
    }
}
