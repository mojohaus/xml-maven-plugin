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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.xml.format.FormatFileSet;
import org.codehaus.mojo.xml.format.IndentCheckSaxHandler;
import org.codehaus.mojo.xml.format.XmlFormatViolation;
import org.codehaus.mojo.xml.format.XmlFormatViolationHandler;
import org.codehaus.plexus.util.DirectoryScanner;
import org.xml.sax.InputSource;

/**
 * An XML indentation check over a set of files.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@Mojo( defaultPhase = LifecyclePhase.VALIDATE, name = "check-format", threadSafe = true )
public class CheckFormatMojo
    extends AbstractXmlMojo
{

    private class ViolationCollector
        implements XmlFormatViolationHandler
    {
        private final Map<String, List<XmlFormatViolation>> violations =
            new LinkedHashMap<String, List<XmlFormatViolation>>();

        @Override
        public void handle( XmlFormatViolation violation )
        {
            List<XmlFormatViolation> list = violations.get( violation.getFile().getAbsolutePath() );
            if ( list == null )
            {
                list = new ArrayList<XmlFormatViolation>();
                violations.put( violation.getFile().getAbsolutePath(), list );
            }
            list.add( violation );
            if ( failOnFormatViolation )
            {
                getLog().error( violation.toString() );
            }
            else
            {
                getLog().warn( violation.toString() );
            }
        }

        public boolean hasViolations()
        {
            return !violations.isEmpty();
        }

        public boolean hasViolations( File file )
        {
            List<XmlFormatViolation> list = violations.get( file.getAbsolutePath() );
            return list != null && !list.isEmpty();
        }

    }

    /**
     * The encoding of files included in {@link #formatFileSets}. Note that the
     * {@code encoding can be set also per FormatFileSet}.
     */
    @Parameter( property = "xml.encoding", defaultValue = "${project.build.sourceEncoding}" )
    private String encoding;

    /**
     * Tells the mojo what to do in case XML formatting violations are found. if {@code true}, all violations will be
     * reported on the console as ERRORs and the build will fail. if {@code false}, all violations will be reported on
     * the console as WARNs and the build will proceed further.
     */
    @Parameter( property = "xml.failOnFormatViolation", defaultValue = "true" )
    private boolean failOnFormatViolation;

    /**
     * File patterns to include. The patterns are relative to the current project's {@code baseDir}.
     */
    @Parameter
    private List<FormatFileSet> formatFileSets = new ArrayList<FormatFileSet>();

    /**
     * The number of spaces expected for indentation. Note that {@code indentSize} can be configuread also per
     * {@link FormatFileSet}.
     */
    @Parameter( property = "xml.indentSize", defaultValue = "2" )
    private int indentSize;

    /** A {@link SAXParserFactory} */
    private SAXParserFactory saxParserFactory;

    /**
     * If set to {@code true}, the result of {@link FormatFileSet#getDefault(String, int)} will be appended to
     * {@link #formatFileSets} before the processing.
     */
    @Parameter( property = "xml.useDefaultFormatFileSet", defaultValue = "true" )
    private boolean useDefaultFormatFileSet;

    /**
     * Creates a new {@link CheckFormatMojo} instance.
     */
    public CheckFormatMojo()
    {
        super();
        this.saxParserFactory = SAXParserFactory.newInstance();
        this.saxParserFactory.setValidating( false );
    }

    /**
     * Checks the formatting of the given {@code file}. The file is read using the given {@code encoding} and the
     * violations are reported to the given {@code violationHandler}.
     *
     * @param file the file to check
     * @param encoding the encoding to use for reading the {@code file}
     * @param violationHandler the {@link XmlFormatViolationHandler} to report violations
     * @throws MojoExecutionException if there is any lover level exception reading or parsing the file.
     */
    private void check( File file, String encoding, XmlFormatViolationHandler violationHandler )
        throws MojoExecutionException
    {

        Reader in = null;
        try
        {
            in = new InputStreamReader( new FileInputStream( file ), encoding );
            SAXParser saxParser = saxParserFactory.newSAXParser();
            IndentCheckSaxHandler handler = new IndentCheckSaxHandler( file, indentSize, violationHandler );
            saxParser.parse( new InputSource( in ), handler );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Could not process file " + file.getAbsolutePath(), e );
        }
        finally
        {
            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
                {
                    getLog().error( "Could not close Reader for " + file.getAbsolutePath(), e );
                }
            }
        }
    }

    /**
     * Called by Maven for executing the Mojo.
     *
     * @throws MojoExecutionException Running the Mojo failed.
     * @throws MojoFailureException A configuration error was detected.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( isSkipping() )
        {
            getLog().debug( "Skipping execution, as demanded by user." );
            return;
        }

        if ( useDefaultFormatFileSet )
        {
            formatFileSets.add( FormatFileSet.getDefault( getBasedir(), encoding, indentSize ) );
        }
        if ( formatFileSets == null || formatFileSets.isEmpty() )
        {
            /* nothing to do */
            return;
        }

        ViolationCollector violationCollector = new ViolationCollector();

        int processedFileCount = 0;

        for ( FormatFileSet formatFileSet : formatFileSets )
        {
            String effectiveEncoding = formatFileSet.getEncoding();
            if ( effectiveEncoding == null )
            {
                effectiveEncoding = this.encoding;
            }
            String[] includedFiles = scan( formatFileSet );
            for ( String includedPath : includedFiles )
            {
                processedFileCount++;
                File file = new File( formatFileSet.getDirectory(), includedPath );
                check( file, effectiveEncoding, violationCollector );
                if ( getLog().isDebugEnabled() && !violationCollector.hasViolations( file ) )
                {
                    getLog().debug( "No XML formatting violations found in file " + file.getAbsolutePath() );
                }
            }
        }
        getLog().debug( "Checked the formatting of " + processedFileCount + " files" );

        if ( failOnFormatViolation && violationCollector.hasViolations() )
        {
            throw new MojoFailureException( "There are XML formatting violations. Check the above log for details." );
        }

    }

    /**
     * A {@link DirectoryScanner} boiler plate.
     *
     * @param fileSet {@link FileSet} to scan
     * @return the included paths
     */
    private String[] scan( FileSet fileSet )
    {
        File basedir = new File( fileSet.getDirectory() );
        if ( !basedir.exists() || !basedir.isDirectory() )
        {
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();

        @SuppressWarnings( "unchecked" )
        List<String> includes = fileSet.getIncludes();
        @SuppressWarnings( "unchecked" )
        List<String> excludes = fileSet.getExcludes();

        if ( includes != null && includes.size() > 0 )
        {
            scanner.setIncludes( includes.toArray( new String[0] ) );
        }

        if ( excludes != null && excludes.size() > 0 )
        {
            scanner.setExcludes( excludes.toArray( new String[0] ) );
        }

        scanner.setBasedir( basedir );

        scanner.scan();
        return scanner.getIncludedFiles();
    }

    /**
     * Sets the number of spaces for indentation.
     *
     * @param indentSize the number of spaces
     */
    public void setIndentSize( int indentSize )
    {
        this.indentSize = indentSize;
    }

}
