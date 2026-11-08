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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@ProjectMojoTest
abstract class AbstractXmlMojoTestCase {

    private static final String FIXTURE_PREFIX = "src/test/";

    private static boolean fixturesCopied;

    private ProjectMojos mojos;

    @BeforeEach
    void setUpMojos(ProjectMojos mojos) {
        this.mojos = mojos;
    }

    protected abstract String getGoal();

    /**
     * Returns the copy of a test project under {@code target/test-fixtures}. The mojos run on the copy, so the files
     * they generate stay out of {@code src/test}, which the source release archives.
     *
     * @param pDir the project directory below the module, such as {@code src/test/it1}
     */
    protected static File fixture(String pDir) throws IOException {
        if (!pDir.startsWith(FIXTURE_PREFIX)) {
            throw new IllegalArgumentException("Not a test fixture: " + pDir);
        }
        File fixtures = new File(MojoExtension.getBasedir(), "target/test-fixtures");
        synchronized (AbstractXmlMojoTestCase.class) {
            if (!fixturesCopied) {
                // The projects refer to each other, such as a child to its parent POM, so copy them all at once.
                if (fixtures.exists()) {
                    FileUtils.deleteDirectory(fixtures);
                }
                copyTree(new File(MojoExtension.getBasedir(), FIXTURE_PREFIX).toPath(), fixtures.toPath());
                fixturesCopied = true;
            }
        }
        return new File(fixtures, pDir.substring(FIXTURE_PREFIX.length()));
    }

    private static void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                Path relative = source.relativize(path);
                if (isSkipped(relative)) {
                    continue;
                }
                Path destination = target.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /** Skips the unit tests' own sources and any output a build left in a project. */
    private static boolean isSkipped(Path relative) {
        if (relative.getNameCount() == 0) {
            return false;
        }
        String top = relative.getName(0).toString();
        if (top.equals("java") || top.equals("resources")) {
            return true;
        }
        for (Path name : relative) {
            if (name.toString().equals("target")) {
                return true;
            }
        }
        return false;
    }

    protected AbstractXmlMojo newMojo(String pDir) throws Exception {
        return mojos.lookup(getGoal(), new File(fixture(pDir), "pom.xml"));
    }

    protected AbstractXmlMojo newMojoWithArtifacts(String pDir, List<Artifact> artifacts) throws Exception {
        AbstractXmlMojo vm = newMojo(pDir);
        MojoExtension.setVariableValueToObject(vm, "pluginDependencies", artifacts);
        return vm;
    }

    protected void runTest(final String pDir) throws Exception {
        newMojo(pDir).execute();
    }

    protected Document parse(File pFile) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(pFile);
    }
}
