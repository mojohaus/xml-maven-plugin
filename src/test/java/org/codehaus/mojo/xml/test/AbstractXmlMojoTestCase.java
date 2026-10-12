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
import java.util.List;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@ProjectMojoTest
abstract class AbstractXmlMojoTestCase {

    private ProjectMojos mojos;

    @BeforeEach
    void setUpMojos(ProjectMojos mojos) {
        this.mojos = mojos;
    }

    protected abstract String getGoal();

    protected AbstractXmlMojo newMojo(String pDir) throws Exception {
        return mojos.lookup(getGoal(), new File(new File(MojoExtension.getBasedir(), pDir), "pom.xml"));
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
