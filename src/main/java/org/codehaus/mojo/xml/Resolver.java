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
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;


/**
 * An implementation of {@link org.xml.sax.EntityResolver},
 * {@link URIResolver}, and {@link LSResourceResolver},
 * based on the Apache catalog resolver.
 */
public class Resolver
    implements EntityResolver2, URIResolver, LSResourceResolver
{
    private final File baseDir;
    private final CatalogResolver resolver;
    private boolean validating;

    /** Creates a new instance.
     * @param pFiles A set of files with catalog definitions to load
     * @throws MojoExecutionException An error occurred while loading the resolvers catalogs.
     */
    Resolver( File pBaseDir, File[] pFiles )
        throws MojoExecutionException
    {
        baseDir = pBaseDir;
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties( true );
        resolver = new CatalogResolver( manager );
        for ( int i = 0; i < pFiles.length; i++ )
        {
            try
            {
                resolver.getCatalog().parseCatalog( pFiles[i].toURI().toURL().toExternalForm() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to parse catalog file: "
                                                  + pFiles[i].getPath() + ": "
                                                  + e.getMessage(), e );
            }
        }        
    }

    /**
     * Implementation of {@link org.xml.sax.EntityResolver#resolveEntity(String, String)}.
     */
    public InputSource resolveEntity( String pPublicId, String pSystemId )
        throws SAXException, IOException
    {
        URL url = resolve( pSystemId );
        if ( url != null )
        {
            return asInputSource( url );
        }
        return resolver.resolveEntity( pPublicId, pSystemId );
    }

    private InputSource asInputSource( URL url ) throws IOException
    {
        InputSource isource = new InputSource( url.openStream() );
        isource.setSystemId( url.toExternalForm() );
        return isource;
    }

    /**
     * Implementation of {@link URIResolver#resolve(String, String)}.
     */
    public Source resolve( String pHref, String pBase )
        throws TransformerException
    {
        URL url = resolve( pHref );
        if ( url != null )
        {
            try
            {
                return new SAXSource( asInputSource( url ) );
            }
            catch ( IOException e )
            {
                throw new TransformerException( e );
            }
        }
        Source source = resolver.resolve( pHref, pBase );
        if ( source == null )
        {
            return source;
        }
        InputSource isource = SAXSource.sourceToInputSource( source );
        if ( isource == null )
        {
            return source;
        }
        XMLReader xmlReader;
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating( validating );
            spf.setNamespaceAware( true );
            xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setEntityResolver( this );
            return new SAXSource( xmlReader, isource );
        }
        catch ( ParserConfigurationException e )
        {
            throw new TransformerException( e.getMessage(), e );
        }
        catch ( SAXException e )
        {
            throw new TransformerException( e.getMessage(), e );
        }
        
    }

    /**
     * Implementation of {@link LSResourceResolver#resolveResource(String, String, String, String, String)}.
     */
    public LSInput resolveResource( String pType, String pNamespaceURI, String pPublicId, String pSystemId,
                                    String pBaseURI )
    {
        final InputSource isource;
        URL url = resolve( pSystemId );
        if ( url != null )
        {
            try
            {
                isource = asInputSource( url );
            }
            catch ( IOException e )
            {
                throw new UndeclaredThrowableException( e );
            }
        }
        else
        {
            isource = resolver.resolveEntity( pPublicId, pSystemId );
            if ( isource == null )
            {
                return null;
            }
        }
        LSInputImpl result = new LSInputImpl();
        result.setByteStream( isource.getByteStream() );
        result.setCharacterStream( isource.getCharacterStream() );
        result.setPublicId( isource.getPublicId() );
        result.setSystemId( isource.getSystemId() );
        result.setEncoding( isource.getEncoding() );
        return result;
    }

    /**
     * Sets, whether the Resolver should create validating parsers.
     */
    public void setValidating ( boolean pValidating )
    {
        validating = pValidating;
    }

    /**
     * Returns, whether the Resolver should create validating parsers.
     */
    public boolean isValidating ( )
    {
        return validating;
    }

    private URL resolveAsResource( String pResource )
    {
        return Thread.currentThread().getContextClassLoader().getResource( pResource );
    }

    private URL resolveAsFile( String pResource )
    {
        File f = new File( baseDir, pResource );
        if ( !f.isFile() )
        {
            f = new File( pResource );
            if ( !f.isFile() )
            {
                return null;
            }
        }
        try
        {
            return f.toURI().toURL();
        }
        catch ( MalformedURLException e )
        {
            return null;
        }
    }

    private URL resolveAsURL( String pResource )
    {
        InputStream stream = null;
        try
        {
            final URL url = new URL( pResource );
            stream = url.openStream();
            stream.close();
            stream = null;
            return url;
        }
        catch ( IOException e )
        {
            return null;
        }
        finally
        {
            if ( stream != null )
            {
                try
                {
                    stream.close();
                }
                catch ( Throwable t )
                {
                    // Ignore me
                }
            }
        }
    }

    /**
     * Attempts to resolve the given URI.
     */
    public URL resolve( String pResource )
    {
        if ( pResource == null )
        {
            return null;
        }

        if ( pResource.startsWith( "resource:" ) )
        {
            String res = pResource.substring( "resource:".length() );
            return resolveAsResource( res );
        }

        URL url = resolveAsResource( pResource );
        if ( url == null )
        {
            url = resolveAsURL( pResource );
            if ( url == null )
            {
                url = resolveAsFile( pResource );
            }
        }
        return url;
    }

    /**
     * Implementation of {@link EntityResolver2#getExternalSubset(String, String)}
     */
    public InputSource getExternalSubset( String name, String baseURI ) throws SAXException, IOException
    {
        return null;
    }

    /**
     * Implementation of {@link EntityResolver2#resolveEntity(String, String, String, String)}
     */
    public InputSource resolveEntity( String pName, String pPublicId, String pBaseURI, String pSystemId )
        throws SAXException, IOException
    {
        URL url = resolve( pSystemId );
        if ( url != null )
        {
            return asInputSource( url );
        }
        return resolver.resolveEntity( pPublicId, pSystemId );
    }
}
