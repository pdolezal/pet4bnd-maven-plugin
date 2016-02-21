package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.yetamine.pet4bnd.format.Format2Bnd;
import net.yetamine.pet4bnd.model.Bundle;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates the export directive.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public final class GenerateMojo extends AbstractPet4BndMojo {

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /** Location of the output file. */
    @Parameter(defaultValue = "${pet4bnd.output}", property = "pet4bnd.output", required = false)
    private String output;

    /** Requiring to be verbose. */
    @Parameter(defaultValue = "${pet4bnd.verbose}", property = "pet4bnd.verbose", required = false)
    private boolean verbose = true;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);
        final Path outputPath = resolvePath(output).orElseGet(this::getDefaultOutputPath);

        final Log log = getLog();
        log.info(String.format("Loading definition file: %s", sourcePath));
        final Bundle definition = resolveDefinition(parseSource(sourcePath));
        final Format2Bnd format = new Format2Bnd(definition);

        try {
            log.info(String.format("Generating bnd file: %s", outputPath));
            format.store(outputPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        log.debug("Storing bnd file finished successfully.");

        if (verbose) {
            log.info("Package exports:");
            format.exports().forEach(log::info);
            log.info(""); // Empty line before bundle version comes
        }

        log.info(String.format("Target bundle version: %s", definition.options().versionBaseline()));
    }

    /**
     * Returns the default output path.
     *
     * @return the default output path
     */
    private Path getDefaultOutputPath() {
        return Paths.get(getProject().getBuild().getDirectory()).resolve("exports.bnd");
    }
}
