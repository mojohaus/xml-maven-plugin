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
     * Builds the it4 test project.
     */
    public void testIt4()
        throws Exception
    {
        final String dir = "src/test/it4";
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
        assertEquals( text1.getNodeValue(), text2.getNodeValue() );
    }
}
