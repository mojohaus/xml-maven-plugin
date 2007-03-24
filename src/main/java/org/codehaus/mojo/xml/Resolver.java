/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.xml;

import java.io.File;
import java.io.IOException;

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
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * An implementation of {@link EntityResolver}, {@link URIResolver},
 * and {@link LSResourceResolver}, based on the Apache catalog resolver.
 */
public class Resolver
    implements EntityResolver, URIResolver, LSResourceResolver
{
    private final CatalogResolver resolver;

    /** Creates a new instance.
     * @param pFiles A set of files with catalog definitions to load
     * @throws MojoExecutionException An error occurred while loading the resolvers catalogs.
     */
    Resolver( File[] pFiles )
        throws MojoExecutionException
    {
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
                throw new MojoExecutionException( "Failed to parse catalog file: " + pFiles[i].getPath() + ": "
                    + e.getMessage(), e );
            }
        }        
    }

    /**
     * Implementation of {@link EntityResolver#resolveEntity(String, String)}.
     */
    public InputSource resolveEntity( String pPublicId, String pSystemId )
        throws SAXException, IOException
    {
        return resolver.resolveEntity( pPublicId, pSystemId );
    }

    /**
     * Implementation of {@link URIResolver#resolve(String, String)}.
     */
    public Source resolve( String pHref, String pBase )
        throws TransformerException
    {
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
            spf.setValidating( false );
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
        InputSource isource = resolver.resolveEntity( pPublicId, pSystemId );
        if ( isource == null )
        {
            return null;
        }
        LSInputImpl result = new LSInputImpl();
        result.setByteStream( isource.getByteStream() );
        result.setCharacterStream( isource.getCharacterStream() );
        result.setPublicId( isource.getPublicId() );
        result.setSystemId( isource.getSystemId() );
        result.setEncoding( isource.getEncoding() );
        return result;
    }
}
