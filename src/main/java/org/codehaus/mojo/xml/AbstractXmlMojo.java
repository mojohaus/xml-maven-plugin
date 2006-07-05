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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

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

    protected File[] getCatalogs()
    {
        if ( catalogs == null )
        {
            return new File[0];
        }

        File[] catalogFiles = new File[catalogs.length];
        for ( int i = 0; i < catalogFiles.length; i++ )
        {
            File f = catalogs[i];
            if ( !f.isAbsolute() )
            {
                f = new File( getBasedir(), f.getPath() );
            }
            catalogFiles[i] = f;
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
            File f = catalogs[i];
            if ( !f.isAbsolute() )
            {
                f = new File( basedir, f.getPath() );
            }
            catalogFiles[i] = f;
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
        final File f = new File( dirName );
        if ( f.isAbsolute() )
        {
            throw new MojoFailureException( "Invalid absolute path: " + f.getPath() );
        }
        final File dir = new File( getBasedir(), dirName );
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

        File dir = new File( pDir );
        if ( !dir.isAbsolute() )
        {
            dir = new File( getBasedir(), pDir );
        }
        File[] result = new File[pFileNames.length];
        for ( int i = 0; i < pFileNames.length; i++ )
        {
            result[i] = new File( dir, pFileNames[i] );
        }
        return result;
    }

    protected File[] getFiles( String pDir, List pIncludes, List pExcludes )
        throws MojoFailureException
    {
        return asFiles( pDir, getFileNames( pDir, pIncludes, pExcludes ) );
    }
}
