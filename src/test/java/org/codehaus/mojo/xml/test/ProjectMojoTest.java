package org.codehaus.mojo.xml.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Runs the test with {@link ProjectMojoExtension}: like {@code @MojoTest}, but mojos work on the project built from
 * their POM.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ProjectMojoExtension.class)
@interface ProjectMojoTest {}
