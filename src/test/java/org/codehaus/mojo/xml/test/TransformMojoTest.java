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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.mojo.xml.transformer.TransformationSet;
import org.codehaus.plexus.components.io.filemappers.FileExtensionMapper;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Test case for the {@link TransformMojo}.
 */
public class TransformMojoTest
    extends AbstractXmlMojoTestCase
{
    protected String getGoal()
    {
        return "transform";
    }

    /**
     * Common code for the it4, it6 and it10 test projects.
     */
    public void runTestIt4( String dir, String targetFile )
        throws Exception
    {
        TransformMojo mojo = (TransformMojo) newMojo( dir );
        mojo.execute();
        Document doc1 = parse( new File( dir, "xml/doc1.xml" ) );
        doc1.normalize();
        Document doc2 = parse( new File( dir, "target/generated-resources/xml/xslt/" + targetFile ) );
        doc2.normalize();
        Element doc1Element = doc1.getDocumentElement();
        assertEquals( "doc1", doc1Element.getLocalName() );
        assertNull( doc1Element.getNamespaceURI() );
        Element doc2Element = doc2.getDocumentElement();
        assertEquals( "doc2", doc2Element.getLocalName() );
        assertNull( doc2Element.getNamespaceURI() );
        Node text1 = doc1Element.getFirstChild();
        assertNotNull( text1 );
        assertNull( text1.getNextSibling() );
        assertEquals( Node.TEXT_NODE, text1.getNodeType() );
        Node text2 = doc2Element.getFirstChild();
        assertNotNull( text2 );
        assertNull( text2.getNextSibling() );
        assertEquals( Node.TEXT_NODE, text2.getNodeType() );
        assertEquals( text1.getNodeValue(), text2.getNodeValue() );
    }

    /**
     * Builds the it4 test project.
     */
    public void testIt4()
        throws Exception
    {
        runTestIt4( "src/test/it4", "doc1.xml" );
    }

    /**
     * Builds the it5 test project.
     */
    public void testIt5()
        throws Exception
    {
        final String dir = "src/test/it5";
        runTest( dir );
        Document doc1 = parse( new File( dir, "xml/doc1.xml" ) );
        doc1.normalize();
        Document doc2 = parse( new File( dir, "target/generated-resources/xml/xslt/doc1.xml" ) );
        doc2.normalize();
        Element doc1Element = doc1.getDocumentElement();
        assertEquals( "doc1", doc1Element.getLocalName() );
        assertNull( doc1Element.getNamespaceURI() );
        Element doc2Element = doc2.getDocumentElement();
        assertEquals( "doc2", doc2Element.getLocalName() );
        assertNull( doc2Element.getNamespaceURI() );
        Node text1 = doc1Element.getFirstChild();
        assertNotNull( text1 );
        assertNull( text1.getNextSibling() );
        assertEquals( Node.TEXT_NODE, text1.getNodeType() );
        Node text2 = doc2Element.getFirstChild();
        assertNotNull( text2 );
        assertNull( text2.getNextSibling() );
        assertEquals( Node.TEXT_NODE, text2.getNodeType() );
        assertEquals(text2.getNodeValue(), "parameter passed in");
    }

    /**
     * Builds the it6 test project.
     */
    public void testIt6()
        throws Exception
    {
        FileExtensionMapper fileExtensionMapper = new FileExtensionMapper();
        fileExtensionMapper.setTargetExtension( ".fo" );
        runTestIt4( "src/test/it6", "doc1.fo" );
    }

    private String read( File file ) throws IOException
    {
        final StringBuffer sb = new StringBuffer();
        final Reader reader = new InputStreamReader( new FileInputStream( file ), "UTF-8" );
        final char[] buffer = new char[ 4096 ];
        for ( ;; )
        {
            final int res = reader.read( buffer );
            if ( res == -1 )
            {
                break;
            }
            if ( res > 0 )
            {
                sb.append( buffer, 0, res );
            }
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Builds the it7 test project.
     */
    public void testIt7() throws Exception
    {
        final File dir = new File( "src/test/it7" );
        final File target = new File( dir, "target/generated-resources/xml/xslt/doc1.xml" );
        TransformMojo mojo = (TransformMojo) newMojo( dir.getPath() );
        FileUtils.fileDelete( target.getPath() );
        mojo.execute();
        String result = read( target );
        assertFalse( result.startsWith( "<?xml" ) );
        mojo = (TransformMojo) newMojo( "src/test/it7" );
        TransformationSet[] transformationSets = (TransformationSet[]) getVariableValueFromObject( mojo, "transformationSets" );
        transformationSets[0].getOutputProperties()[0].setValue( "no" );
        FileUtils.fileDelete( target.getPath() );
        mojo.execute();
        result = read( target );
        assertTrue( result.startsWith( "<?xml" ) );
    }

    private String eval( Node contextNode, String str ) throws TransformerException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        final String xsl = "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
            + "<xsl:template match='*'>\n"
            + "<xsl:value-of select='" + str + "'/>\n"
            + "</xsl:template>\n"
            + "</xsl:stylesheet>\n";
        final StringWriter sw = new StringWriter();
        final Transformer t = TransformMojo.newTransformerFactory( org.apache.xalan.processor.TransformerFactoryImpl.class.getName(), getClass().getClassLoader()).newTransformer( new StreamSource( new StringReader( xsl ) ) );
        t.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        t.transform( new DOMSource( contextNode ), new StreamResult( sw ) );
        return sw.toString();
    }

    /**
     * Builds the it7 test project.
     */
    public void testIt8() throws Exception
    {
        if ( !java1_6_Aware() )
        {
        	System.out.println( " skip test due to non compliance jvm version need 1.6" );
            return;
        }
        final String dir = "src/test/it8";
        runTest( dir );
        Document doc1 = parse( new File( dir, "target/generated-resources/xml/xslt/doc1.xml" ) );
        
        assertEquals("SAXON 8.7 from Saxonica", eval( doc1, "/root/vendor" ) );
        assertEquals("http://www.saxonica.com/", eval( doc1, "/root/vendor-url" ) );
        assertEquals("2.0", eval( doc1, "/root/version" ) );
    }

    /**
     * Builds the it10 test project.
     */
    public void testIt10()
        throws Exception
    {
    	runTestIt4( "src/test/it10", "doc1.xml" );
    }

    /**
     * Builds the it11 test project, tests in-place modification.
     */
    public void testIt11()
    	throws Exception
    {
    	String projectPath = "src/test/it11";
    	File projectDirectory = new File( getBasedir(), projectPath );
    	File targetDirectory = new File( projectPath, "target" );
    	if ( targetDirectory.exists() ){
    		FileUtils.cleanDirectory( targetDirectory );
    	}
    	File xmlInputDirectory = new File( projectDirectory, "xml" );
    	File xmlOutputDirectory = new File( targetDirectory, "generated-resources/xml/xslt" );
    	/* copy to target since that is in an SCM-ignored directory */
    	FileUtils.copyDirectory( xmlInputDirectory, xmlOutputDirectory, "*.xml" , null );

    	TransformMojo mojo = (TransformMojo) newMojo( projectPath );
    	mojo.execute();

    	String fileToTransform = "doc1.xml";
    	Document doc1 = parse( new File( xmlInputDirectory, fileToTransform ) );
    	doc1.normalize();
    	Document doc2 = parse( new File( xmlOutputDirectory, fileToTransform ) );
    	doc2.normalize();
    	Element doc1Element = doc1.getDocumentElement();
    	assertEquals( "doc1", doc1Element.getLocalName() );
    	assertNull( doc1Element.getNamespaceURI() );
    	Element doc2Element = doc2.getDocumentElement();
    	assertEquals( "doc2", doc2Element.getLocalName() );
    	assertNull( doc2Element.getNamespaceURI() );
    	Node text1 = doc1Element.getFirstChild();
    	assertNotNull( text1 );
    	assertNull( text1.getNextSibling() );
    	assertEquals( Node.TEXT_NODE, text1.getNodeType() );
    	Node text2 = doc2Element.getFirstChild();
    	assertNotNull( text2 );
    	assertNull( text2.getNextSibling() );
    	assertEquals( Node.TEXT_NODE, text2.getNodeType() );
    	assertEquals( text1.getNodeValue(), text2.getNodeValue() );
    }
}
