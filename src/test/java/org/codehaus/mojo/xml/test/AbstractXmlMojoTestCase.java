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
package org.codehaus.mojo.xml.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.resource.DefaultResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceLoader;
import org.codehaus.plexus.resource.loader.JarResourceLoader;
import org.codehaus.plexus.resource.loader.ResourceLoader;
import org.codehaus.plexus.resource.loader.ThreadContextClasspathResourceLoader;
import org.codehaus.plexus.resource.loader.URLResourceLoader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractXmlMojoTestCase
    extends AbstractMojoTestCase
{

    protected abstract String getGoal();

    protected AbstractXmlMojo newMojo( String pDir )
        throws Exception
    {
        File testPom = new File( new File( getBasedir(), pDir ), "pom.xml" );
        
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
        ProjectBuilder projectBuilder = this.lookup(ProjectBuilder.class);
        MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();
//        final Build build = new Build();
//        build.setDirectory( "target" );
//        project.setBuild(build);
        project.getBuild().setDirectory("target");
        AbstractXmlMojo vm = (AbstractXmlMojo) lookupConfiguredMojo(project, getGoal());
        setVariableValueToObject( vm, "basedir", new File( getBasedir(), pDir ) );
        final Log log = new SilentLog();
        DefaultResourceManager rm = new DefaultResourceManager();
        setVariableValueToObject( rm, "logger", log );
        setVariableValueToObject( vm, "locator", rm );
        final Map<String, ResourceLoader> resourceLoaders = new HashMap<String, ResourceLoader>();
        resourceLoaders.put( "file", new FileResourceLoader() );
        resourceLoaders.put( "jar", new JarResourceLoader() );
        resourceLoaders.put( "classloader", new ThreadContextClasspathResourceLoader() );
        URLResourceLoader url = new URLResourceLoader();
        setVariableValueToObject( url, "logger", log );
        resourceLoaders.put( "url", url );
        setVariableValueToObject( rm, "resourceLoaders", resourceLoaders );

//        MavenProjectStub project = new MavenProjectStub()
//        {
//            public Build getBuild()
//            {
//                return build;
//            }
//        };
//        setVariableValueToObject( vm, "project", project );
        return vm;
    }

    protected void runTest( final String pDir )
        throws Exception
    {
        newMojo( pDir ).execute();
    }

    protected Document parse( File pFile )
        throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating( false );
        dbf.setNamespaceAware( true );
        return dbf.newDocumentBuilder().parse( pFile );
    }

    protected boolean java1_6_Aware()
        throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            TransformMojo.newTransformerFactory( "net.sf.saxon.TransformerFactoryImpl",
                                                 Thread.currentThread().getContextClassLoader() );
            return true;
        }
        catch ( NoSuchMethodException e )
        {
            return false;
        }
    }
    
    
    
    @Override   //In maven-plugin-testing-harnes 2.1, this method had a simple error in it which resulted in
                //the configuration being incorrectly generated.  In later versions, the error has been corrected.
                //The error is annotated in the comments below.  This method should be removed when upgrading to later
                //versions.
    protected Mojo lookupConfiguredMojo( MavenSession session, MojoExecution execution )
        throws Exception, ComponentConfigurationException
    {
        MavenProject project = session.getCurrentProject();
        MojoDescriptor mojoDescriptor = execution.getMojoDescriptor();

        Mojo mojo = (Mojo) lookup( mojoDescriptor.getRole(), mojoDescriptor.getRoleHint() );

        ExpressionEvaluator evaluator = new PluginParameterExpressionEvaluator( session, execution );

        Xpp3Dom configuration = null;
        Plugin plugin = project.getPlugin( mojoDescriptor.getPluginDescriptor().getPluginLookupKey() );
        if ( plugin != null )
        {
            configuration = (Xpp3Dom) plugin.getConfiguration();
        }
        if ( configuration == null )
        {
            configuration = new Xpp3Dom( "configuration" );
        }
        //FIX: the parameters were in the wrong order on this call - they have been reversed
        configuration = Xpp3Dom.mergeXpp3Dom(  configuration ,execution.getConfiguration());
        //END FIX
        PlexusConfiguration pluginConfiguration = new XmlPlexusConfiguration( configuration );

        getContainer().lookup( ComponentConfigurator.class, "basic" ).configureComponent( mojo, pluginConfiguration, evaluator, getContainer().getContainerRealm() );

        return mojo;
    }

    
}
