package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.yetamine.pet4bnd.model.format.PetFormat;

/**
 * Restores the baselines and resets the change information in the source file.
 */
@Mojo(name = "restore", requiresDirectInvocation = true)
public final class RestoreMojo extends AbstractPet4BndMojo {

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);

        final Log log = getLog();
        log.info(String.format("Updating definition file: %s", sourcePath));
        final PetFormat definition = resolveDefinition(parseSource(sourcePath));

        try {
            definition.restore();
            definition.store(sourcePath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        log.info(String.format("Target bundle version: %s", definition.version().resolution()));
    }
}
