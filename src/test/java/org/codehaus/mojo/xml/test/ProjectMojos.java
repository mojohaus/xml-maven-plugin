package org.codehaus.mojo.xml.test;

import java.io.File;

import org.apache.maven.plugin.Mojo;

/**
 * Looks up mojos from code, for tests that pick the POM at run time. Take it as a parameter of a test or
 * {@code @BeforeEach} method.
 */
interface ProjectMojos {

    /**
     * The mojo of {@code goal}, configured from {@code pom} and working on the project built from it.
     *
     * @param goal the goal of the mojo
     * @param pom the POM file
     * @param <T> the type of the mojo
     * @return the configured mojo
     * @throws Exception if the project cannot be built or the mojo cannot be configured
     */
    <T extends Mojo> T lookup(String goal, File pom) throws Exception;
}
