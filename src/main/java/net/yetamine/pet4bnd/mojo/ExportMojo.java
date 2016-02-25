package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.yetamine.pet4bnd.format.Format2Map;
import net.yetamine.pet4bnd.model.Bundle;

/**
 * Dumps the version information of the definition elements.
 */
@Mojo(name = "export", defaultPhase = LifecyclePhase.VALIDATE)
public final class ExportMojo extends AbstractPet4BndMojo {

    /** Location of the target file if specified. */
    @Parameter(property = "pet4bnd.export", required = false)
    private String export;

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /** Requiring to be verbose. */
    @Parameter(defaultValue = "${pet4bnd.verbose}", property = "pet4bnd.verbose", required = false)
    private boolean verbose = true;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Log log = getLog();

        if ((export == null) || export.isEmpty()) {
            log.info("No output file specified, nothing will be generated.");
            return;
        }

        final Path outputPath = requirePath(export);
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);

        log.info(String.format("Loading definition file: %s", sourcePath));
        final Bundle definition = resolveDefinition(parseSource(sourcePath));
        final Format2Map format = new Format2Map(definition);

        try {
            log.info(String.format("Generating properties file: %s", outputPath));
            format.store(outputPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        log.debug("Storing the properties file finished successfully.");
        // Inform about the target version as the last common information
        log.info(String.format("Target bundle version: %s", definition.version().resolution()));

        if (verbose) { // Dump the result
            log.info("Generated properties:");
            format.content().forEach((n, v) -> log.info(new StringBuilder(n).append(" = ").append(v).toString()));
        }
    }
}
