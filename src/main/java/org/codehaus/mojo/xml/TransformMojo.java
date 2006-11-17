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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
 * The TransformMojo is used for transforming a set of files using a common stylesheet.
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

    /**
     * Whether creating the transformed files should be forced.
     * @parameter expression="${xml.forceCreation}" default-value="false"
     */
    private boolean forceCreation;

    private Templates getTemplate( Resolver pResolver, File stylesheet )
        throws MojoExecutionException
    {

        TransformerFactory tf = TransformerFactory.newInstance();
        if ( pResolver != null )
        {
            tf.setURIResolver( pResolver );
        }
        try
        {
            return tf.newTemplates( new StreamSource( stylesheet ) );
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

    protected static String getAllExMsgs( Throwable ex, boolean includeExName )
    {
        StringBuffer sb = new StringBuffer( ( includeExName ? ex.toString() : ex.getLocalizedMessage() ) );
        while ( ( ex = ex.getCause() ) != null )
            sb.append( "\nCaused by: " + ex.toString() );

        return sb.toString();
    }

    /**
     * 
     * @param files the fileNames or URLs to scan their lastModified timestamp.
     * @param oldest if true, returns the latest modificationDate of all files,
     *      otherwise returns the earliest.
     * @return the older or younger last modification timestamp of all files.
     */
    protected long findLastModified( List/*<Object>*/files, boolean oldest )
    {
        long timeStamp = ( oldest ? Long.MIN_VALUE : Long.MAX_VALUE );
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            Object no = it.next();

            if ( no != null )
            {
                long fileModifTime;
                if ( no instanceof File )
                {
                    fileModifTime = ( (File) no ).lastModified();
                }
                else // either URL or filePath
                {
                    
                    String sdep = no.toString();

                    try
                    {
                        URL url = new URL( sdep );

                        URLConnection uCon = url.openConnection();
                        uCon.setUseCaches( false );

                        fileModifTime = uCon.getLastModified();

                    }
                    catch ( MalformedURLException e )
                    {
                        fileModifTime = new File( sdep ).lastModified();

                    }
                    catch ( IOException ex )
                    {
                        fileModifTime = ( oldest ? Long.MIN_VALUE : Long.MAX_VALUE );
                        getLog().warn(
                                       "Skipping URL '" + no
                                           + "' from up-to-date check due to error while opening connection: "
                                           + getAllExMsgs( ex, true ) );
                    }

                }

                getLog().debug( ( oldest ? "Depends " : "Produces " ) + no + ": " + new Date( fileModifTime ) );

                if ( ( fileModifTime > timeStamp ) ^ !oldest )
                    timeStamp = fileModifTime;
            } // end if file null.
        }// end filesloop

        if ( timeStamp == Long.MIN_VALUE ) // no older file found
            return Long.MAX_VALUE; // assume re-execution required. 
        else if ( timeStamp == Long.MAX_VALUE ) // no younger file found
            return Long.MIN_VALUE; // assume re-execution required.

        return timeStamp;
    }

    /**
     * @return true to indicate results are up-to-date, that is, when the latest 
     *          from input files is earlier than the younger from the output 
     *          files (meaning no re-execution required).
     */
    protected boolean isUpdToDate(List dependsFiles, List producesFiles) {
        
        // The older timeStamp of all input files;
        long inputTimeStamp = findLastModified(dependsFiles, true);

        // The younger of all destination files.
        long destTimeStamp = (producesFiles == null? Long.MIN_VALUE: findLastModified(producesFiles, false)); 
    
            getLog().debug("Depends timeStamp: " + inputTimeStamp + ", produces timestamp: " + destTimeStamp);

        return inputTimeStamp < destTimeStamp; 
    }

    private void transform( Transformer pTransformer, File input, File output )
        throws MojoExecutionException
    {
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

        final String stylesheetPath = pTransformationSet.getStylesheet();
        File stylesheet = new File( stylesheetPath );
        if ( !stylesheet.isAbsolute() )
        {
            stylesheet = new File( getBasedir(), stylesheetPath );
        }
        Templates template = getTemplate( pResolver, stylesheet );
        
        int filesTransformed = 0;
        File inputDir = getDir( pTransformationSet.getDir() );
        File outputDir = getOutputDir( pTransformationSet.getOutputDir() );
        for ( int i = 0; i < fileNames.length; i++ )
        {
            final Transformer t;
            
            File input = getFile( inputDir, fileNames[i] );
            File output = getFile( outputDir, fileNames[i] );

            // Perform up-to-date-check.
            boolean needsTransform = forceCreation;
            if ( !needsTransform )
            {
                List dependsFiles = new ArrayList();
                List producesFiles = new ArrayList();
                
                dependsFiles.add( getProject().getFile() );// Depends from pom.xml file for when project configuration changes.
                dependsFiles.add( stylesheet );
                dependsFiles.add( Arrays.asList( getCatalogs() ) );
                dependsFiles.add( input );
                dependsFiles.addAll( asFileList( getBasedir(), pTransformationSet.getOtherDepends() ) );

                producesFiles.add( output );
                
                needsTransform = !isUpdToDate( dependsFiles, producesFiles );
            }
            
            if (!needsTransform)
            {
                getLog().info("Skipping XSL transformation.  File " + fileNames[i] + " is up-to-date.");
            }
            else
            {
                filesTransformed++;
                
                // Perform transformation.
                try
                {
                    t = template.newTransformer();
                    t.setURIResolver( pResolver );

                    transform( t, input, output );
                    
                }
                catch ( TransformerConfigurationException e )
                {
                    throw new MojoExecutionException( "Failed to create Transformer: " + e.getMessage(), e );
                }
            }

        }// end file loop

        if (filesTransformed > 0)
            getLog().info("Transformed " + filesTransformed + " file(s).");

        
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
