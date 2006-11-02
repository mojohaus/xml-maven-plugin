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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/** Abstract base class for the plugins Mojo's.
 */
public abstract class AbstractXmlMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The base directory, relative to which directory names are
     * interpreted.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /** An XML catalog file, which is being used to resolve
     * entities.
     * @parameter
     */
    private File[] catalogs;

    protected MavenProject getProject()
    {
        return project;
    }

    protected File getBasedir()
    {
        return basedir;
    }

    protected File asAbsoluteFile( String path )
    {
        return asAbsoluteFile( new File(path) );
    }

    protected File asAbsoluteFile( File f )
    {
        if ( f.isAbsolute() )
        {
            return f;
        }
        return new File( getBasedir(), f.getPath() );
    }

    protected File[] getCatalogs()
    {
        if ( catalogs == null )
        {
            return new File[0];
        }

        File[] catalogFiles = new File[catalogs.length];
        for ( int i = 0; i < catalogFiles.length; i++ )
        {
            catalogFiles[i] = asAbsoluteFile( catalogs[i] );
        }
        return catalogFiles;
    }

    protected Resolver getResolver()
        throws MojoExecutionException
    {
        File[] catalogFiles = getCatalogs();
        if ( catalogFiles == null || catalogFiles.length == 0 )
        {
            return null;
        }

        for ( int i = 0; i < catalogFiles.length; i++ )
        {
            catalogFiles[i] = asAbsoluteFile( catalogs[i] );
        }
        return new Resolver( catalogFiles );
    }

    protected String[] getFileNames( String pDir, List pIncludes, List pExcludes )
        throws MojoFailureException
    {
        final String dirName = pDir;
        if ( dirName == null || "".equals( dirName ) )
        {
            throw new MojoFailureException( "A ValidationSet requires a nonempty 'dir' child element." );
        }
        final File dir = asAbsoluteFile( dirName );
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( dir );
        if ( pIncludes != null && pIncludes.size() > 0 )
        {
            ds.setIncludes( (String[]) pIncludes.toArray( new String[pIncludes.size()] ) );
        }
        if ( pExcludes != null && pExcludes.size() > 0 )
        {
            ds.setExcludes( (String[]) pExcludes.toArray( new String[pExcludes.size()] ) );
        }
        ds.scan();
        return ds.getIncludedFiles();
    }

    protected File[] asFiles( String pDir, String[] pFileNames )
    {
        if ( pFileNames == null )
        {
            return new File[0];
        }

        File dir = asAbsoluteFile( pDir );
        File[] result = new File[pFileNames.length];
        for ( int i = 0; i < pFileNames.length; i++ )
        {
            result[i] = new File( dir, pFileNames[i] );
        }
        return result;
    }

    protected List asFileList( File dir, List pFileNames )
    {
        if ( pFileNames == null )
        {
            return Collections.EMPTY_LIST;
        }

        if ( !dir.isAbsolute() )
        {
            dir = new File( getBasedir(), dir.toString() );
        }
        List result = new ArrayList();
        for ( ListIterator i = pFileNames.listIterator(); i.hasNext(); )
        {
            i.set( new File( dir, (String) i.next() ) );
        }
        return result;
    }

    protected File[] getFiles( String pDir, List pIncludes, List pExcludes )
        throws MojoFailureException
    {
        return asFiles( pDir, getFileNames( pDir, pIncludes, pExcludes ) );
    }

    protected List getExcludes( List origExcludes, boolean skipDefaultExcludes )
    {
        if ( skipDefaultExcludes )
            return origExcludes;

        origExcludes.addAll( Arrays.asList( FileUtils.getDefaultExcludes() ) );

        return origExcludes;
    }

}
