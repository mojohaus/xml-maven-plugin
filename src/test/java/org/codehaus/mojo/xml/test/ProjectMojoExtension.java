package org.codehaus.mojo.xml.test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import static org.mockito.Mockito.lenient;

/**
 * {@code MojoExtension} gives the mojo an empty project, so the POM's directory, build and parent are missing. This
 * loads the project built from the POM into it before the mojo is configured.
 */
class ProjectMojoExtension extends MojoExtension {

    private static final ExtensionContext.Namespace HARNESS = ExtensionContext.Namespace.create("MojoExtension");

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == ProjectMojos.class
                || super.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        if (parameterContext.getParameter().getType() != ProjectMojos.class) {
            return super.resolveParameter(parameterContext, extensionContext);
        }
        return new ProjectMojos() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends Mojo> T lookup(String goal, File pom) throws Exception {
                MavenProject project = load(extensionContext, build(extensionContext, pom));
                PluginDescriptor descriptor =
                        extensionContext.getStore(HARNESS).get(PluginDescriptor.class, PluginDescriptor.class);
                String[] coord = mojoCoordinates(goal, descriptor);
                Xpp3Dom pomDom = Xpp3DomBuilder.build(new XmlStreamReader(pom));
                Xpp3Dom configuration = withInherited(extractPluginConfiguration(coord[1], pomDom), project, coord);
                return (T) lookupMojo(extensionContext, coord, configuration, descriptor, pom.toPath());
            }
        };
    }

    /**
     * The configuration from the POM, over the plugin's configuration in the built project, which adds what the POM
     * inherits from its parent or plugin management, as a build would.
     */
    private static Xpp3Dom withInherited(Xpp3Dom fromPom, MavenProject project, String[] coord) {
        Plugin plugin = project.getPlugin(coord[0] + ":" + coord[1]);
        if (plugin == null || plugin.getConfiguration() == null) {
            return fromPom;
        }
        return Xpp3Dom.mergeXpp3Dom(fromPom, new Xpp3Dom((Xpp3Dom) plugin.getConfiguration()));
    }

    private MavenProject build(ExtensionContext context, File pom) throws Exception {
        ProjectBuildingRequest request = new DefaultMavenExecutionRequest().getProjectBuildingRequest();
        request.setRepositorySession(new DefaultRepositorySystemSession());
        request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        return getContainer(context)
                .lookup(ProjectBuilder.class)
                .build(pom, request)
                .getProject();
    }

    /** Loads {@code built} into the project the harness gives the mojo, and returns that project. */
    private MavenProject load(ExtensionContext context, MavenProject built) throws Exception {
        MavenProject project = getContainer(context).lookup(MavenProject.class);
        project.setModel(built.getModel());
        project.setOriginalModel(built.getOriginalModel());
        project.setFile(built.getFile());
        project.setParent(built.getParent());
        project.setParentFile(built.getParentFile());
        project.setArtifact(built.getArtifact());
        MavenSession session = getContainer(context).lookup(MavenSession.class);
        if (session.getSettings() == null) {
            lenient().doReturn(new Settings()).when(session).getSettings();
        }
        List<MavenProject> projects = Collections.singletonList(project);
        lenient().doReturn(projects).when(session).getProjects();
        lenient().doReturn(projects).when(session).getAllProjects();
        return project;
    }
}
