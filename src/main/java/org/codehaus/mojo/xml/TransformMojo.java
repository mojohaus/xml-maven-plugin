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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.xml.transformer.NameValuePair;
import org.codehaus.mojo.xml.transformer.TransformationSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.xml.sax.InputSource;


/**
 * The TransformMojo is used for transforming a set of files using a common stylesheet.
 *
 * @goal transform
 * @phase generate-resources
 */
public class TransformMojo extends AbstractXmlMojo
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

    /**
     * Transformer factory use. By default, the systems default transformer
     * factory is used.
     * <b>If you use this feature you must use at least jdk 1.6</b>
     * @parameter expression="${xml.transformerFactory}"
     */
    private String transformerFactory;

    private void setFeature( TransformerFactory pTransformerFactory, String name, Boolean value )
        throws MojoExecutionException
    {
        // Try to use the method setFeature, which isn't available until JAXP 1.3
        Method m;
        try
        {
            m = pTransformerFactory.getClass().getMethod( "setFeature", new Class[]{ String.class, boolean.class } );
        }
        catch ( NoSuchMethodException e )
        {
            m = null;
        }
        if ( m == null )
        {
            // Not available, try to use setAttribute
            pTransformerFactory.setAttribute( name, value );
        }
        else
        {
            try
            {
                m.invoke( pTransformerFactory, new Object[]{ name, value } );
            }
            catch ( IllegalAccessException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            catch ( InvocationTargetException e )
            {
                Throwable t = e.getTargetException();
                throw new MojoExecutionException( t.getMessage(), t );
            }
        }
    }

    private Templates getTemplate( Resolver pResolver, Source stylesheet, TransformationSet transformationSet )
        throws MojoExecutionException, MojoFailureException
    {

        TransformerFactory tf = getTransformerFactory();
        if ( pResolver != null )
        {
            tf.setURIResolver( pResolver );
        }
        NameValuePair[] features = transformationSet.getFeatures();
        if ( features != null )
        {
            for ( int i = 0;  i < features.length;  i++ )
            {
                final NameValuePair feature = features[i];
                final String name = feature.getName();
                if ( name == null  ||  name.length() == 0 )
                {
                    throw new MojoFailureException( "A features name is missing or empty." );
                }
                final String value = feature.getValue();
                if ( value == null )
                {
                    throw new MojoFailureException( "No value specified for feature " + name );
                }
                setFeature( tf, name, Boolean.valueOf( value ) );
            }
        }
        NameValuePair[] attributes = transformationSet.getAttributes();
        if ( attributes != null )
        {
            for ( int i = 0;  i < attributes.length;  i++ )
            {
                final NameValuePair attribute = attributes[i];
                final String name = attribute.getName();
                if ( name == null  ||  name.length() == 0 )
                {
                    throw new MojoFailureException( "An attributes name is missing or empty." );
                }
                final String value = attribute.getValue();
                if ( value == null )
                {
                    throw new MojoFailureException( "No value specified for attribute " + name );
                }
                tf.setAttribute( name, value );
            }
        }
        try
        {
            return tf.newTemplates( stylesheet );
        }
        catch ( TransformerConfigurationException e )
        {
            throw new MojoExecutionException( "Failed to parse stylesheet " + stylesheet + ": " + e.getMessage(), e );
        }
    }
    
    /**
     * Creates a new instance of {@link TransformerFactory}.
     */
    private TransformerFactory getTransformerFactory( ) throws MojoFailureException, MojoExecutionException
    {
        if ( transformerFactory == null )
        {
            return TransformerFactory.newInstance();
        }
        
        try
        {
            return newTransformerFactory( transformerFactory, Thread.currentThread().getContextClassLoader() );
        }
        catch ( NoSuchMethodException exception )
        {
            throw new MojoFailureException( "JDK6 required when using transformerFactory parameter" );
        }
        catch ( IllegalAccessException exception )
        {
            throw new MojoExecutionException( "Cannot instantiate transformer factory", exception );
        }
        catch ( InvocationTargetException exception )
        {
            throw new MojoExecutionException( "Cannot instantiate transformer factory", exception );
        }
    }
    
    // public for use by unit test
    public static TransformerFactory newTransformerFactory( String factoryClassName, ClassLoader classLoader )
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        // use reflection to avoid JAXP 1.4 (and hence JDK6) requirement

        Class[] methodTypes = new Class[] { String.class, ClassLoader.class };
        
        Method method = TransformerFactory.class.getDeclaredMethod( "newInstance", methodTypes );
        
        Object[] methodArgs = new Object[] { factoryClassName, classLoader };
        
        return (TransformerFactory) method.invoke( null, methodArgs );
    }

    private File getFile( File pDir, String pFile )
    {
        if ( new File( pFile ).isAbsolute() )
        {
            throw new IllegalStateException( "Output/Input file names must not be absolute." );
        }
        return new File( pDir, pFile );
    }

    private File getDir( File pDir )
    {
        if ( pDir == null )
        {
            return getBasedir();
        }
        return asAbsoluteFile( pDir );
    }

    private void addToClasspath( File pOutputDir )
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
        resource.setDirectory( pOutputDir.getPath() );
        resource.setFiltering( false );
        project.addResource( resource );
    }

    private File getOutputDir( File pOutputDir )
    {
        if ( pOutputDir == null )
        {
            MavenProject project = getProject();
            String dir = project.getBuild().getDirectory();
            if ( dir == null )
            {
                throw new IllegalStateException( "The projects build directory is null." );
            }
            dir += "/generated-resources/xml/xslt";
            return asAbsoluteFile( new File( dir ) );
        }
        return asAbsoluteFile( pOutputDir );
    }

    private static String getAllExMsgs( Throwable ex, boolean includeExName )
    {
        StringBuffer sb = new StringBuffer( ( includeExName ? ex.toString() : ex.getLocalizedMessage() ) );
        while ( ( ex = ex.getCause() ) != null )
        {
            sb.append( "\nCaused by: " + ex.toString() );
        }

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
                        getLog().warn( "Skipping URL '" + no
                                           + "' from up-to-date check due to error while opening connection: "
                                           + getAllExMsgs( ex, true ) );
                    }

                }

                getLog().debug( ( oldest ? "Depends " : "Produces " ) + no + ": " + new Date( fileModifTime ) );

                if ( ( fileModifTime > timeStamp ) ^ !oldest )
                {
                    timeStamp = fileModifTime;
                }
            } // end if file null.
        } // end filesloop

        if ( timeStamp == Long.MIN_VALUE )
        {
            // no older file found
            return Long.MAX_VALUE; // assume re-execution required. 
        }
        else if ( timeStamp == Long.MAX_VALUE )
        {
            // no younger file found
            return Long.MIN_VALUE; // assume re-execution required.
        }

        return timeStamp;
    }

    /**
     * @return true to indicate results are up-to-date, that is, when the latest 
     *          from input files is earlier than the younger from the output 
     *          files (meaning no re-execution required).
     */
    protected boolean isUpdToDate( List dependsFiles, List producesFiles )
    {
        // The older timeStamp of all input files;
        long inputTimeStamp = findLastModified( dependsFiles, true );

        // The younger of all destination files.
        long destTimeStamp = producesFiles == null
            ? Long.MIN_VALUE : findLastModified( producesFiles, false ); 
    
            getLog().debug( "Depends timeStamp: " + inputTimeStamp + ", produces timestamp: " + destTimeStamp );

        return inputTimeStamp < destTimeStamp; 
    }

    private void transform( Transformer pTransformer, File input, File output, Resolver pResolver )
        throws MojoExecutionException
    {
        File dir = output.getParentFile();
        dir.mkdirs();
        getLog().info( "Transforming file: " + input.getPath() );
        FileOutputStream fos = null;
        try
        {
        	final boolean transformInPlace = output.equals( input );
        	File tmpOutput = null;
        	if ( transformInPlace ) {
        		tmpOutput = File.createTempFile( "xml-maven-plugin", "xml" );
        		tmpOutput.deleteOnExit();
                fos = new FileOutputStream( tmpOutput );
        	}
        	else
        	{
        		fos = new FileOutputStream( output );
        	}

        	final String parentFile = input.getParent() == null
                ? null : input.getParentFile().toURI().toURL().toExternalForm();
            pTransformer.transform( pResolver.resolve( input.toURI().toURL().toExternalForm(),
                                                       parentFile ), new StreamResult( fos ) );
            fos.close();
            fos = null;
            if ( transformInPlace ) {
            	FileUtils.copyFile( tmpOutput, output );
            	/* tmpOutput is a temporary file */
            	tmpOutput.delete();
            }
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

    private File getOutputFile( File targetDir, String pName, FileMapper[] pFileMappers )
    {
        String name = pName;
        if ( pFileMappers != null )
        {
            for ( int i = 0;  i < pFileMappers.length;  i++ )
            {
                name = pFileMappers[i].getMappedFileName( name );
            }
        }
        return getFile( targetDir, name );
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

        String stylesheetName = pTransformationSet.getStylesheet();
        if ( stylesheetName == null )
        {
            getLog().warn( "No stylesheet configured." );
            return;
        }

        final URL stylesheetUrl = getResource( stylesheetName );
        Templates template;
        InputStream stream = null;
        try
        {
        	stream = stylesheetUrl.openStream();
        	InputSource isource = new InputSource( stream );
        	isource.setSystemId( stylesheetUrl.toExternalForm() );
        	template = getTemplate( pResolver, new SAXSource( isource ), pTransformationSet );
        	stream.close();
        	stream = null;
        }
        catch ( IOException e )
        {
        	throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
        	IOUtil.close( stream );
        }
        
        int filesTransformed = 0;
        File inputDir = getDir( pTransformationSet.getDir() );
        File outputDir = getOutputDir( pTransformationSet.getOutputDir() );
        for ( int i = 0; i < fileNames.length; i++ )
        {
            final Transformer t;
            
            File input = getFile( inputDir, fileNames[i] );
            File output = getOutputFile( outputDir, fileNames[i], pTransformationSet.getFileMappers() );

            // Perform up-to-date-check.
            boolean needsTransform = forceCreation;
            if ( !needsTransform )
            {
                List dependsFiles = new ArrayList();
                List producesFiles = new ArrayList();

                // Depends from pom.xml file for when project configuration changes.
                dependsFiles.add( getProject().getFile() );
                if ( "file".equals( stylesheetUrl.getProtocol() ) )
                {
                	dependsFiles.add( new File( stylesheetUrl.getFile() ) );
                }
                List catalogFiles = new ArrayList();
                List catalogUrls = new ArrayList();
                setCatalogs( catalogFiles, catalogUrls );
                dependsFiles.addAll( catalogFiles );
                dependsFiles.add( input );
                File[] files = asFiles( getBasedir(), pTransformationSet.getOtherDepends() );
                for ( int j = 0;  j < files.length;  j++ )
                {
                    dependsFiles.add( files[j] );
                }

                producesFiles.add( output );
                
                needsTransform = !isUpdToDate( dependsFiles, producesFiles );
            }
            
            if ( !needsTransform )
            {
                getLog().debug( "Skipping XSL transformation.  File " + fileNames[i] + " is up-to-date." );
            }
            else
            {
                filesTransformed++;
                
                // Perform transformation.
                try
                {
                    t = newTransformer( template, pTransformationSet );
                    t.setURIResolver( pResolver );

                    NameValuePair[] parameters = pTransformationSet.getParameters();
                    if ( parameters != null )
                    {
                        for ( int j = 0;  j < parameters.length;  j++  )
                        {
                            NameValuePair key = parameters[j];
                            t.setParameter( key.getName(), key.getValue() );
                        }
                    }
                    
                    transform( t, input, output, pResolver );
                    
                }
                catch ( TransformerConfigurationException e )
                {
                    throw new MojoExecutionException( "Failed to create Transformer: " + e.getMessage(), e );
                }
            }
        } // end file loop

        if ( filesTransformed > 0 )
        {
            getLog().info( "Transformed " + filesTransformed + " file(s)." );
        }
        
        if ( pTransformationSet.isAddedToClasspath() )
        {
            addToClasspath( pTransformationSet.getOutputDir() );
        }
    }

    private Transformer newTransformer( Templates template, TransformationSet pTransformationSet )
        throws TransformerConfigurationException, MojoExecutionException, MojoFailureException
    {
        Transformer t = template.newTransformer();
        NameValuePair[] properties = pTransformationSet.getOutputProperties();
        if ( properties != null )
        {
            for ( int i = 0;  i < properties.length;  i++ )
            {
                final String name = properties[i].getName();
                if ( name == null  ||  "".equals( name ) )
                {
                    throw new MojoFailureException( "Missing or empty output property name" );
                }
                final String value = properties[i].getValue();
                if ( value == null )
                {
                    throw new MojoFailureException( "Missing value for output property " + name );
                }
                try
                {
                    t.setOutputProperty( name, value );
                }
                catch ( IllegalArgumentException e )
                {
                    throw new MojoExecutionException( "Unsupported property name or value: "
                                                      + name + " => "
                                                      + value
                                                      + e.getMessage(), e );
                }
            }
        }
        return t;
    }

    /**
     * Called by Maven to run the plugin.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( transformationSets == null || transformationSets.length == 0 )
        {
            throw new MojoFailureException( "No TransformationSets configured." );
        }

        Object oldProxySettings = activateProxy();
        try
        {
            Resolver resolver = getResolver( );
            for ( int i = 0; i < transformationSets.length; i++ )
            {
                TransformationSet transformationSet = transformationSets[i];
                resolver.setValidating( transformationSet.isValidating() );
                transform( resolver, transformationSet );
            }
        }
        finally
        {
            passivateProxy( oldProxySettings );
        }
    }
}
