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

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.xml.validation.ValidationSet;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * The ValidatorMojo's task is the validation of XML files against a given schema.
 *
 * @goal validate
 * @phase test
 */
public class ValidateMojo
    extends AbstractXmlMojo
{
    /**
     * Specifies a set of document types, which are being
     * validated.
     * @parameter
     */
    private ValidationSet[] validationSets;

    private Schema getSchema( Resolver pResolver, ValidationSet pValidationSet )
        throws MojoExecutionException
    {
        final String publicId = pValidationSet.getPublicId();
        final String systemId = pValidationSet.getSystemId();
        if ( ( publicId == null || "".equals( publicId ) ) && ( systemId == null || "".equals( systemId ) ) )
        {
            return null;
        }

        getLog().debug( "Loading schema with public Id " + publicId + ", system Id " + systemId );
        InputSource inputSource = null;
        if ( pResolver != null )
        {
            try
            {
                inputSource = pResolver.resolveEntity( publicId, systemId );
            }
            catch ( SAXException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        if ( inputSource == null )
        {
            inputSource = new InputSource();
            inputSource.setPublicId( publicId );
            inputSource.setSystemId( systemId );
        }
        final SAXSource saxSource = new SAXSource( inputSource );

        String schemaLanguage = pValidationSet.getSchemaLanguage();
        if ( schemaLanguage == null || "".equals( schemaLanguage ) )
        {
            schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        }
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( schemaLanguage );
            if ( pResolver != null )
            {
                schemaFactory.setResourceResolver( pResolver );
            }
            return schemaFactory.newSchema( saxSource );
        }
        catch ( SAXException e )
        {
            throw new MojoExecutionException( "Failed to load schema with public ID " + publicId + ", system ID "
                + systemId + ": " + e.getMessage(), e );
        }
    }

    private void validate( final Resolver pResolver, ValidationSet pValidationSet, Schema pSchema, File pFile )
        throws MojoExecutionException
    {
        try
        {
            if ( pSchema == null )
            {
                getLog().debug( "Parsing " + pFile.getPath() );
                parse( pResolver, pValidationSet, pFile );
            }
            else
            {
                getLog().debug( "Validating " + pFile.getPath() );
                Validator validator = pSchema.newValidator();
                if ( pResolver != null )
                {
                    validator.setResourceResolver( pResolver );
                }
                pSchema.newValidator().validate( new StreamSource( pFile ) );
            }
        }
        catch ( SAXParseException e )
        {
            final String publicId = e.getPublicId();
            final String systemId = e.getSystemId();
            final int lineNum = e.getLineNumber();
            final int colNum = e.getColumnNumber();
            final String location;
            if ( publicId == null && systemId == null && lineNum == -1 && colNum == -1 )
            {
                location = "";
            }
            else
            {
                final StringBuffer loc = new StringBuffer();
                String sep = "";
                if ( publicId != null )
                {
                    loc.append( "Public ID " );
                    loc.append( publicId );
                    sep = ", ";
                }
                if ( systemId != null )
                {
                    loc.append( sep );
                    loc.append( systemId );
                    sep = ", ";
                }
                if ( lineNum != -1 )
                {
                    loc.append( sep );
                    loc.append( "line " );
                    loc.append( lineNum );
                    sep = ", ";
                }
                if ( colNum != -1 )
                {
                    loc.append( sep );
                    loc.append( " column " );
                    loc.append( colNum );
                    sep = ", ";
                }
                location = loc.toString();
            }
            final String msg = "While parsing " + pFile.getPath() + ( "".equals( location ) ? "" : ", at " + location )
                + ": " + e.getMessage();
            throw new MojoExecutionException( msg, e );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "While parsing " + pFile + ": " + e.getMessage(), e );
        }
    }

    private SAXParserFactory newSAXParserFactory( ValidationSet pValidationSet )
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating( pValidationSet.isValidating() );
        spf.setNamespaceAware( true );
        return spf;
    }

    private void parse( Resolver pResolver, ValidationSet pValidationSet, File pFile )
        throws IOException, SAXException, ParserConfigurationException
    {
        XMLReader xr = newSAXParserFactory( pValidationSet ).newSAXParser().getXMLReader();
        if ( pResolver != null )
        {
            xr.setEntityResolver( pResolver );
        }
        xr.setErrorHandler( new ErrorHandler()
        {
            public void error( SAXParseException pException )
                throws SAXException
            {
                throw pException;
            }

            public void fatalError( SAXParseException pException )
                throws SAXException
            {
                throw pException;
            }

            public void warning( SAXParseException pException )
                throws SAXException
            {
                throw pException;
            }

        } );
        xr.parse( pFile.toURI().toURL().toExternalForm() );
    }

    private void validate( Resolver pResolver, ValidationSet pValidationSet )
        throws MojoExecutionException, MojoFailureException
    {
        final Schema schema = getSchema( pResolver, pValidationSet );
        final File[] files = getFiles( pValidationSet.getDir(), 
                                       pValidationSet.getIncludes(), 
                                       getExcludes( pValidationSet.getExcludes(), pValidationSet.isSkipDefaultExcludes() ) );
        if ( files.length == 0 )
        {
            getLog().info(
                           "No matching files found for ValidationSet with public ID " + pValidationSet.getPublicId()
                               + ", system ID " + pValidationSet.getSystemId() + "." );
        }
        for ( int i = 0; i < files.length; i++ )
        {
            validate( pResolver, pValidationSet, schema, files[i] );
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( validationSets == null || validationSets.length == 0 )
        {
            throw new MojoFailureException( "No ValidationSets configured." );
        }

        Resolver resolver = getResolver();
        for ( int i = 0; i < validationSets.length; i++ )
        {
            validate( resolver, validationSets[i] );
        }
    }
}
