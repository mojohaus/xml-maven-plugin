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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
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

    /** An XML catalog file, or URL, which is being used to resolve
     * entities.
     * @parameter
     */
    private String[] catalogs;
    
    /**
     * Plexus resource manager used to obtain XSL.
     * 
     * @component
     * @required
     * @readonly
     */
    private ResourceManager locator;

    private boolean locatorInitialized;
    
    /**
     * Returns the maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Returns the projects base directory.
     */
    protected File getBasedir()
    {
        return basedir;
    }

    /**
     * Converts the given file into an file with an absolute
     * path.
     */
    protected File asAbsoluteFile( File f )
    {
        if ( f.isAbsolute() )
        {
            return f;
        }
        return new File( getBasedir(), f.getPath() );
    }

    /**
     * Returns the plugins catalog files.
     */
    protected void setCatalogs( List pCatalogFiles, List pCatalogUrls )
    {
        if ( catalogs == null  ||  catalogs.length == 0 )
        {
            return;
        }

        for ( int i = 0; i < catalogs.length; i++ )
        {
        	try
        	{
        		URL url = new URL( catalogs[i] );
        		pCatalogUrls.add( url );
        	}
        	catch ( MalformedURLException e )
        	{
                pCatalogFiles.add( asAbsoluteFile( new File( catalogs[i] ) ) );
        	}
        }
    }

    /**
     * Creates a new resolver.
     */
    protected Resolver getResolver()
        throws MojoExecutionException
    {
    	List catalogFiles = new ArrayList();
    	List catalogUrls = new ArrayList();
    	setCatalogs( catalogFiles, catalogUrls );

        return new Resolver( getBasedir(), catalogFiles, catalogUrls, getLocator(),
        		getLog().isDebugEnabled() );
    }

    /**
     * Scans a directory for files and returns a set of path names.
     */
    protected String[] getFileNames( File pDir, String[] pIncludes, String[] pExcludes )
        throws MojoFailureException
    {
        if ( pDir == null )
        {
            throw new MojoFailureException( "A ValidationSet or TransformationSet"
                                            + " requires a nonempty 'dir' child element." );
        }
        final File dir = asAbsoluteFile( pDir );
        if ( !dir.isDirectory() )
        {
            getLog().warn( "The directory " + dir.getPath()
                           + ", which is a base directory of a ValidationSet or TransformationSet, does not exist." );
            return new String[0];
        }
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

    /**
     * Converts the given set of file names into a set of {@link File}
     * instances. The file names may be relative to the given base directory.
     */
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

    /**
     * Scans a directory for files and returns a set of {@link File} instances.
     */
    protected File[] getFiles( File pDir, String[] pIncludes, String[] pExcludes )
        throws MojoFailureException
    {
        return asFiles( pDir, getFileNames( pDir, pIncludes, pExcludes ) );
    }

    /**
     * Calculates the exclusions to use when searching files.
     */
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

    /**
     * Called to install the plugins proxy settings.
     */
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
        getLog().debug( "Proxy settings: " + hostProperty + "=" + hostValue
                       + ", " + portProperty + "=" + portValue
                       + ", " + userProperty + "=" + userValue
                       + ", " + passwordProperty + "=" + (passwordValue == null ? "null" : "<PasswordNotLogged>")
                       + ", " + nonProxyHostsProperty + "=" + nonProxyHostsValue );
        return properties;
    }

    /**
     * Called to restore the proxy settings, which have been installed
     * when the plugin was invoked.
     */
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

    protected URL getResource( String pResource )
    	throws MojoFailureException
    {
        try
        {
        	return getLocator().getResource( pResource ).getURL();
        }
        catch ( ResourceNotFoundException exception )
        {
            throw new MojoFailureException( "Could not find stylesheet: " + pResource );
        }
        catch ( IOException e )
        {
        	throw new MojoFailureException( "Error while locating resource: " + pResource );
        }
    }

    protected ResourceManager getLocator()
    {
		if ( !locatorInitialized )
    	{
        	locator.addSearchPath( FileResourceLoader.ID, getBasedir().getAbsolutePath() );
    		locatorInitialized = true;
    	}
		return locator;
	}
}
