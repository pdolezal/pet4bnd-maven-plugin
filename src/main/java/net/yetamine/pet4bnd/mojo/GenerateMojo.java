package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.yetamine.pet4bnd.format.Format2Bnd;
import net.yetamine.pet4bnd.model.Bundle;

/**
 * Generates the export directive.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public final class GenerateMojo extends AbstractPet4BndMojo {

    /** Location of the output file. */
    @Parameter(property = "pet4bnd.output", required = false)
    private String output;

    /** Location of the source file. */
    @Parameter(defaultValue = "${pet4bnd.source}", property = "pet4bnd.source", required = false)
    private String source;

    /** Generate the bundle version. */
    @Parameter(defaultValue = "release", required = false)
    private String bundleVersion;

    /** Requiring to be verbose. */
    @Parameter(defaultValue = "${pet4bnd.verbose}", property = "pet4bnd.verbose", required = false)
    private boolean verbose = true;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        final Log log = getLog();

        if ((output == null) || output.isEmpty()) {
            log.info("No output file specified, nothing will be generated.");
            return;
        }

        final Path outputPath = requirePath(output);
        final Path sourcePath = resolvePath(source).orElseGet(this::getDefaultSourcePath);

        log.info(String.format("Loading definition file: %s", sourcePath));
        final Bundle definition = resolveDefinition(parseSource(sourcePath));
        final boolean bundleVersionRequired = isBundleVersionRequired();
        final Format2Bnd format = new Format2Bnd(definition, bundleVersionRequired);

        try {
            log.info(String.format("Generating bnd file: %s", outputPath));
            format.store(outputPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        log.debug("Storing bnd file finished successfully.");

        if (verbose) {
            if (definition.exports().isEmpty()) {
                log.info("Package exports: none");
            } else {
                log.info("Package exports:");
                format.exports().forEach(log::info);
                log.info(""); // Empty line before bundle version comes
            }
        }

        log.info(String.format("Target bundle version: %s", definition.version().resolution()));
        if (!bundleVersionRequired) { // Report it for completeness (could be confusing otherwise)
            log.info("Target bundle version omitted from the output as requested.");
        }
    }

    /**
     * Returns if the project should generate the Bundle-Version directive.
     *
     * @return {@code true} if the project settings shall result in generating
     *         the Bundle-Version directive
     */
    private boolean isBundleVersionRequired() {
        switch (bundleVersion.toLowerCase()) {
            case "always":
            case "true":
            case "yes":
                return true;

            case "snapshot":
                return isProjectVersionSnapshot();

            default:
                getLog().warn(String.format("Unknown bundle version directive '%s'.", bundleVersion));
                // Fall through

            case "release": // This is the usual default
                return !isProjectVersionSnapshot();

            case "never":
            case "false":
            case "no":
                return false;

        }
    }

    /**
     * Tests if the project's version is a SNAPSHOT.
     *
     * @return {@code true} if the project's version is a SNAPSHOT
     */
    private boolean isProjectVersionSnapshot() {
        final String version = getProject().getVersion();
        return ((version != null) && version.endsWith("-SNAPSHOT"));
    }
}
