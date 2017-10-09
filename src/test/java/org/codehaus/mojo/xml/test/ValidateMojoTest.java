package org.codehaus.mojo.xml.test;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.xml.ValidateMojo;

/**
 * Test case for the {@link ValidateMojo}.
 */
public class ValidateMojoTest
    extends AbstractXmlMojoTestCase
{
    protected String getGoal()
    {
        return "validate";
    }

    /**
     * Builds the it1 test project.
     */
    public void testIt1()
        throws Exception
    {
        runTest( "src/test/it1" );
    }

    /**
     * Builds the it2 test project.
     */
    public void testIt2()
        throws Exception
    {
        try
        {
            runTest( "src/test/it2" );
            fail( "Expected exception" );
        }
        catch ( MojoExecutionException e )
        {
            //validation exceptions no longer have an attached cause.
//            Throwable t = e.getCause();
//            assertNotNull( t );
//            assertTrue( t instanceof SAXParseException );
//            SAXParseException ex = (SAXParseException) t;
//            assertEquals( 20, ex.getLineNumber() );
        }
    }

    /**
     * Builds the it3 test project.
     */
    public void testIt3()
        throws Exception
    {
        runTest( "src/test/it3" );
    }

    /**
     * Builds the it9 test project.
     */
    public void testIt9()
        throws Exception
    {
        runTest( "src/test/it9" );
    }

    /**
     * Builds and runs the it12 test project (MOJO-1648)
     */
    public void testIt12()
        throws Exception
    {
        runTest( "src/test/it12" );
    }

    /**
     * Builds and runs the it13 test project (Issue #16)
     */
    public void testIt13()
        throws Exception
    {
        try{
            runTest( "src/test/it13" );
            fail("Catalog file does not exist - an exception should have been thrown");
        }
        catch(MojoExecutionException e){
            e.printStackTrace();
        }
    }

    /**
     * Builds and runs the it13 test project (Issue #16)
     */
    public void testIt14()
        throws Exception
    {
        try{
            runTest( "src/test/it14" );
            fail("Errorneous Directory name in config should have thrown an exception");
        }
        catch(MojoExecutionException e){
            e.printStackTrace();
        }
    }

    public void testIt15()
        throws Exception
    {
        try{
            runTest( "src/test/it15" );
        }
        catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    
    /*
        Notes on how this test works:
            - we try to validate a document using a public schema for which we have
            no mapping in our catalog.  Because the catalogHandling is set to "strict",
            the test should fail.
    */
    /**
     * Builds and runs the it16 test project (Issue #19 - adding a "strict" setting on all mojo)
     * @throws Exception 
     */
    public void testIt16()
        throws Exception
    {
        try{
            runTest( "src/test/it16" );
            fail();
        }
        catch(MojoExecutionException e){
           
        }
    }

    
    /**
     * Builds and runs the it17 test project (Issue #19 - adding a "strict" setting on all mojo).
     * This test is just a sanity test of the "local" setting for catalogHandling.
     * @throws Exception 
     */
    public void testIt17()
        throws Exception
    {
        try{
            runTest( "src/test/it17" );
        }
        catch(MojoExecutionException e){
            fail();
           
        }
    }

    
    /**
     * Builds and runs the xinclude test project
     */
    public void testXIncludeEnabled()
        throws Exception
    {
        runTest( "src/test/xinclude-xsd" );
    }
}
