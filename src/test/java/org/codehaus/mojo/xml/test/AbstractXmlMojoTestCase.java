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

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractXmlMojoTestCase extends AbstractMojoTestCase {

    protected abstract String getGoal();

    protected AbstractXmlMojo newMojo(String pDir) throws Exception {
        File testPom = new File(new File(getBasedir(), pDir), "pom.xml");

        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
        buildingRequest.setRepositorySession(new DefaultRepositorySystemSession());
        ProjectBuilder projectBuilder = this.lookup(ProjectBuilder.class);
        MavenProject project = projectBuilder.build(testPom, buildingRequest).getProject();
        project.getBuild().setDirectory("target");
        AbstractXmlMojo vm = (AbstractXmlMojo) lookupConfiguredMojo(project, getGoal());
        setVariableValueToObject(vm, "basedir", new File(getBasedir(), pDir));
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
