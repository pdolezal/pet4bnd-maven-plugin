package net.yetamine.pet4bnd.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.version.Version;

/**
 * Updates the POM version to the bundle target version.
 */
@Mojo(name = "release", requiresDirectInvocation = true)
public final class ReleaseMojo extends AbstractPet4BndMojo {

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /** Location of the source file. */
    @Parameter(defaultValue = "${project.file}", property = "pet4bnd.pom", required = true)
    private File pom;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);

        final Log log = getLog();
        log.info(String.format("Loading definition file: %s", sourcePath));
        final Bundle definition = resolveDefinition(parseSource(sourcePath));
        final Version version = definition.version().baseline();
        log.info(String.format("Target bundle version: %s", version));

        try {
            final Path pomPath = pom.toPath();
            log.info(String.format("Updating POM file: %s", pomPath));
            new PomVersionEditor(pomPath).version(version.toString()).store(pomPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
