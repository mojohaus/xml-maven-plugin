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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.codehaus.mojo.xml.ValidateMojo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for the {@link ValidateMojo}.
 */
class ValidateMojoTest extends AbstractXmlMojoTestCase {
    @Override
    protected String getGoal() {
        return "validate";
    }

    /**
     * Builds the it1 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt1() throws Exception {
        runTest("src/test/it1");
    }

    /**
     * Builds the it2 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt2() throws Exception {
        assertThrows(MojoExecutionException.class, () -> runTest("src/test/it2"));
        // validation exceptions no longer have an attached cause.
        //            Throwable t = e.getCause();
        //            assertNotNull( t );
        //            assertTrue( t instanceof SAXParseException );
        //            SAXParseException ex = (SAXParseException) t;
        //            assertEquals( 20, ex.getLineNumber() );
    }

    /**
     * Builds the it3 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt3() throws Exception {
        runTest("src/test/it3");
    }

    /**
     * Builds the it9 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt9() throws Exception {
        runTest("src/test/it9");
    }

    /**
     * Builds and runs the it12 test project (MOJO-1648)
     * @throws Exception The test failed.
     */
    @Test
    void testIt12() throws Exception {
        runTest("src/test/it12");
    }

    /**
     * Builds and runs the it13 test project (Issue #16)
     * @throws Exception The test failed.
     */
    @Test
    void testIt13() throws Exception {
        assertThrows(
                MojoExecutionException.class,
                () -> runTest("src/test/it13"),
                "Catalog file does not exist - an exception should have been thrown");
    }

    /**
     * Builds and runs the it13 test project (Issue #16)
     * @throws Exception The test failed.
     */
    @Test
    void testIt14() throws Exception {
        assertThrows(
                MojoExecutionException.class,
                () -> runTest("src/test/it14"),
                "Errorneous Directory name in config should have thrown an exception");
    }

    /**
     * Builds, and runs the it15 project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt15() throws Exception {
        runTest("src/test/it15");
    }

    /**
     * Builds, and runs the it18 project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt18() throws Exception {
        runTest("src/test/it18");
    }

    /**
     * Builds the it19 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt19() throws Exception {
        runTest("src/test/it19");
    }

    /**
     * Builds the it19 test project.
     * @throws Exception The test failed.
     */
    @Test
    void testIt20() throws Exception {
        assertThrows(MojoExecutionException.class, () -> runTest("src/test/it20"));
        // validation exceptions no longer have an attached cause.
        //            Throwable t = e.getCause();
        //            assertNotNull( t );
        //            assertTrue( t instanceof SAXParseException );
        //            SAXParseException ex = (SAXParseException) t;
        //            assertEquals( 20, ex.getLineNumber() );
    }

    /**
     * Builds the multimodule/xsd-import test project.
     * @throws Exception The test failed.
     */
    @Test
    void testMultimoduleXsdImport() throws Exception {
        List<Artifact> artifacts = Arrays.asList(createStubArtifact(
                "src/test/multimodule/xsd-import/validation/xsd-classpath-catalog-0.1.jar",
                "org.codehaus.mojo.xml",
                "xsd-classpath-catalog",
                "0.1",
                "jar"));
        newMojoWithArtifacts("src/test/multimodule/xsd-import/validation", artifacts)
                .execute();
    }

    private static Artifact createStubArtifact(
            String path, String groupId, String artifactId, String version, String packaging) {
        File artifactFile = new File(MojoExtension.getBasedir(), path);

        ArtifactStub stub = new ArtifactStub();
        stub.setGroupId(groupId);
        stub.setArtifactId(artifactId);
        stub.setVersion(version);
        stub.setType(packaging);
        stub.setFile(artifactFile);
        return stub;
    }

    /**
     * Builds and runs the xinclude test project
     * @throws Exception The test failed.
     */
    @Test
    void testXIncludeEnabled() throws Exception {
        runTest("src/test/xinclude-xsd");
    }
}
