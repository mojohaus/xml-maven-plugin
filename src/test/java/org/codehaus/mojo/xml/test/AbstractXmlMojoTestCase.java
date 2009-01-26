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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.mojo.xml.TransformMojo;
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
        AbstractXmlMojo vm = (AbstractXmlMojo) lookupMojo( getGoal(), testPom );
        setVariableValueToObject( vm, "basedir", new File( getBasedir(), pDir ) );
        final Build build = new Build();
        build.setDirectory( "target" );
        MavenProjectStub project = new MavenProjectStub()
        {
            public Build getBuild()
            {
                return build;
            }
        };
        setVariableValueToObject( vm, "project", project );
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
            TransformMojo.newTransformerFactory( "net.sf.saxon.TransformerFactoryImpl", Thread.currentThread()
                .getContextClassLoader() );
            return true;
        }
        catch ( NoSuchMethodException e )
        {
            return false;
        }
    }
}
