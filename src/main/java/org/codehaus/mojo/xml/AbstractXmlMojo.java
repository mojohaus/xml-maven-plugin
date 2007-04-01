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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
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
     * The system settings for Maven. This is the instance resulting from 
     * merging global- and user-level settings files.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

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
            return new Resolver( new File[0] );
        }

        for ( int i = 0; i < catalogFiles.length; i++ )
        {
            catalogFiles[i] = asAbsoluteFile( catalogs[i] );
        }
        return new Resolver( catalogFiles );
    }

    protected String[] getFileNames( File pDir, String[] pIncludes, String[] pExcludes )
        throws MojoFailureException
    {
        if ( pDir == null )
        {
            throw new MojoFailureException( "A ValidationSet requires a nonempty 'dir' child element." );
        }
        final File dir = asAbsoluteFile( pDir );
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( dir );
        if ( pIncludes != null && pIncludes.length > 0 )
        {
            ds.setIncludes( pIncludes );
        }
        if ( pExcludes != null && pExcludes.length > 0 )
        {
            ds.setExcludes( pExcludes );
        }
        ds.scan();
        return ds.getIncludedFiles();
    }

    protected File[] asFiles( File pDir, String[] pFileNames )
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

    protected File[] asFileList( File dir, String[] pFileNames )
    {
        if ( pFileNames == null )
        {
            return new File[0];
        }

        if ( !dir.isAbsolute() )
        {
            dir = new File( getBasedir(), dir.toString() );
        }

        File[] result = new File[pFileNames.length];
        for ( int i = 0;  i < pFileNames.length;  i++ )
        {
            result[i] = new File( dir, pFileNames[i] );
        }
        return result;
    }

    protected File[] getFiles( File pDir, String[] pIncludes, String[] pExcludes )
        throws MojoFailureException
    {
        return asFiles( pDir, getFileNames( pDir, pIncludes, pExcludes ) );
    }

    protected String[] getExcludes( String[] origExcludes, boolean skipDefaultExcludes )
    {
        if ( skipDefaultExcludes )
        {
            return origExcludes;
        }

        String[] defaultExcludes = FileUtils.getDefaultExcludes();
        if ( origExcludes == null  ||  origExcludes.length == 0 )
        {
            return defaultExcludes;
        }
        String[] result = new String[origExcludes.length + defaultExcludes.length];
        System.arraycopy( origExcludes, 0, result, 0, origExcludes.length );
        System.arraycopy( defaultExcludes, 0, result, origExcludes.length, defaultExcludes.length );
        return result;
    }

    private boolean isEmpty( String value )
    {
        return value == null  ||  value.trim().length() == 0;
    }

    private void setProperty( List pProperties, String pKey, String pValue )
    {
        if ( pProperties != null )
        {
            pProperties.add( pKey );
            pProperties.add( System.getProperty( pKey ) );
        }
        if ( pValue == null )
        {
            System.getProperties().remove( pKey );
        }
        else
        {
            System.setProperty( pKey, pValue );
        }
    }

    protected Object activateProxy()
    {
        if ( settings == null )
        {
            return null;
        }
        final Proxy proxy = settings.getActiveProxy();
        if ( proxy == null )
        {
            return null;
        }

        final List properties = new ArrayList();
        final String protocol = proxy.getProtocol();
        final String prefix = isEmpty( protocol ) ? "" : ( protocol + "." );

        final String host = proxy.getHost();
        final String hostProperty = prefix + "proxyHost";
        final String hostValue = isEmpty( host ) ? null : host;
        setProperty( properties, hostProperty, hostValue );
        final int port = proxy.getPort();
        final String portProperty = prefix + "proxyPort";
        final String portValue = ( port == 0 || port == -1 ) ? null : String.valueOf( port );
        setProperty( properties, portProperty, portValue );
        final String username = proxy.getUsername();
        final String userProperty = prefix + "proxyUser";
        final String userValue = isEmpty( username ) ? null : username;
        setProperty( properties, userProperty, userValue );
        final String password = proxy.getPassword();
        final String passwordProperty = prefix + "proxyPassword";
        final String passwordValue = isEmpty( password ) ? null : password;
        setProperty( properties, passwordProperty, passwordValue );
        final String nonProxyHosts = proxy.getNonProxyHosts();
        final String nonProxyHostsProperty = prefix + "nonProxyHosts";
        final String nonProxyHostsValue = isEmpty( nonProxyHosts ) ? null : nonProxyHosts.replace( ',' , '|' );
        setProperty( properties, nonProxyHostsProperty, nonProxyHostsValue );
        getLog().info( "Proxy settings: " + hostProperty + "=" + hostValue
                       + ", " + portProperty + "=" + portValue
                       + ", " + userProperty + "=" + userValue
                       + ", " + passwordProperty + "=" + passwordValue
                       + ", " + nonProxyHostsProperty + "=" + nonProxyHostsValue );
        return properties;
    }

    protected void passivateProxy( Object pProperties )
    {
        if ( pProperties == null )
        {
            return;
        }
        final List properties = (List) pProperties;
        for ( Iterator iter = properties.iterator();  iter.hasNext(); )
        {
            final String key = (String) iter.next();
            final String value = (String) iter.next();
            setProperty( null, key, value );
        }
    }
}
