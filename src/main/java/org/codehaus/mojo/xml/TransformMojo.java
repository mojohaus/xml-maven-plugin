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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.xml.transformer.TransformationSet;


/**
 * The {@link TransformMojo} is used for transforming a
 * set of files using a common stylesheet.
 *
 * @goal transform
 * @phase generate-resources
 */
public class TransformMojo
    extends AbstractXmlMojo
{
    /**
     * Specifies one or more sets of files, which are being
     * transformed.
     * @parameter
     */
    private TransformationSet[] transformationSets;

    private Templates getTemplate( Resolver pResolver, TransformationSet pTransformationSet )
        throws MojoExecutionException
    {
        final String stylesheet = pTransformationSet.getStylesheet();
        File f = new File( stylesheet );
        if ( !f.isAbsolute() )
        {
            f = new File( getBasedir(), stylesheet );
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        if ( pResolver != null )
        {
            tf.setURIResolver( pResolver );
        }
        try
        {
            return tf.newTemplates( new StreamSource( f ) );
        }
        catch ( TransformerConfigurationException e )
        {
            throw new MojoExecutionException( "Failed to parse stylesheet " + stylesheet + ": " + e.getMessage(), e );
        }
    }

    private File getFile( File pDir, String pFile )
    {
        if ( new File( pFile ).isAbsolute() )
        {
            throw new IllegalStateException( "Output/Input file names must not be absolute." );
        }
        return new File( pDir, pFile );
    }

    private File getDir( String pDir )
    {
        if ( pDir == null || "".equals( pDir ) )
        {
            return getBasedir();
        }
        File f = new File( pDir );
        if ( f.isAbsolute() )
        {
            return f;
        }
        return new File( getBasedir(), pDir );
    }

    private void transform( Transformer pTransformer, File pInputDir, File pOutputDir, String pFile )
        throws MojoExecutionException
    {
        File input = getFile( pInputDir, pFile );
        File output = getFile( pOutputDir, pFile );
        File dir = output.getParentFile();
        dir.mkdirs();
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( output );
            pTransformer.transform( new StreamSource( input ), new StreamResult( fos ) );
            fos.close();
            fos = null;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create output file " + output.getPath() + ": "
                + e.getMessage(), e );
        }
        catch ( TransformerException e )
        {
            throw new MojoExecutionException( "Failed to transform input file " + input.getPath() + ": "
                + e.getMessage(), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( Throwable t )
                {
                    /* Ignore me */
                }
            }
        }
    }

    private void addToClasspath( String pOutputDir )
    {
        MavenProject project = getProject();
        for ( Iterator iter = project.getResources().iterator(); iter.hasNext(); )
        {
            Resource resource = (Resource) iter.next();
            if ( resource.getDirectory().equals( pOutputDir ) )
            {
                return;
            }
        }

        Resource resource = new Resource();
        resource.setDirectory( pOutputDir );
        resource.setFiltering( false );
        project.addResource( resource );
    }

    private File getOutputDir( String pOutputDir )
    {
        String dir;
        if ( pOutputDir == null )
        {
            MavenProject project = getProject();
            dir = project.getBuild().getDirectory();
            if ( dir == null )
            {
                throw new IllegalStateException( "The projects build directory is null." );
            }
            dir += "/generated-resources/xml/xslt";
        }
        else
        {
            dir = pOutputDir;
        }
        return getDir( dir );
    }

    private void transform( Resolver pResolver, TransformationSet pTransformationSet )
        throws MojoExecutionException, MojoFailureException
    {
        String[] fileNames = getFileNames( pTransformationSet.getDir(),
                                           pTransformationSet.getIncludes(),
                                           getExcludes( pTransformationSet.getExcludes(),
                                                        pTransformationSet.isSkipDefaultExcludes() ) );
        if ( fileNames == null || fileNames.length == 0 )
        {
            getLog().warn( "No files found for transformation by stylesheet " + pTransformationSet.getStylesheet() );
            return;
        }

        Templates template = getTemplate( pResolver, pTransformationSet );
        File inputDir = getDir( pTransformationSet.getDir() );
        File outputDir = getOutputDir( pTransformationSet.getOutputDir() );
        for ( int i = 0; i < fileNames.length; i++ )
        {
            final Transformer t;
            try
            {
                t = template.newTransformer();
                t.setURIResolver( pResolver );
            }
            catch ( TransformerConfigurationException e )
            {
                throw new MojoExecutionException( "Failed to create Transformer: " + e.getMessage(), e );
            }
            transform( t, inputDir, outputDir, fileNames[i] );
        }

        if ( pTransformationSet.isAddedToClasspath() )
        {
            addToClasspath( pTransformationSet.getOutputDir() );
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( transformationSets == null || transformationSets.length == 0 )
        {
            throw new MojoFailureException( "No ValidationSets configured." );
        }

        Resolver resolver = getResolver();
        for ( int i = 0; i < transformationSets.length; i++ )
        {
            transform( resolver, transformationSets[i] );
        }
    }
}
