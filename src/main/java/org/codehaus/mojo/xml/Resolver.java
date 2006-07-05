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

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class Resolver
    implements EntityResolver, URIResolver, LSResourceResolver
{
    private final CatalogResolver resolver;

    /** Creates a new instance.
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

    public InputSource resolveEntity( String pPublicId, String pSystemId )
        throws SAXException, IOException
    {
        return resolver.resolveEntity( pPublicId, pSystemId );
    }

    public Source resolve( String pHref, String pBase )
        throws TransformerException
    {
        return resolver.resolve( pHref, pBase );
    }

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
