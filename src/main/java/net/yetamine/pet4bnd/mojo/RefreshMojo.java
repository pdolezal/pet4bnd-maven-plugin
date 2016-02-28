package net.yetamine.pet4bnd.mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.model.BundleVersion;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Updates the POM version to the most suitable snapshot version.
 */
@Mojo(name = "refresh", requiresDirectInvocation = true)
public final class RefreshMojo extends AbstractPet4BndMojo {

    /** Snapshot qualifier for Maven. */
    private static final String SNAPSHOT_QUALIFIER = "-SNAPSHOT";

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
        final BundleVersion bundleVersion = definition.version();
        final Version baseline = bundleVersion.baseline();
        log.info(String.format("Bundle version baseline: %s", baseline));

        // Compute the version, but cut off the qualifier as it would be replaced anyway
        final Version targetVersion = computeTargetVersion(baseline.qualifier(null), bundleVersion.constraint());
        final String snapshotVersion = targetVersion.toString() + SNAPSHOT_QUALIFIER;
        log.info(String.format("Target bundle version: %s", snapshotVersion));
        assert (targetVersion.compareTo(bundleVersion.resolution()) <= 0);

        try {
            final Path pomPath = pom.toPath();
            log.info(String.format("Updating POM file: %s", pomPath));
            new PomVersionEditor(pomPath).version(snapshotVersion).store(pomPath);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Computes the next snapshot version below the constraint (if any).
     *
     * @param baseline
     *            the version baseline to adjust. It must not be {@code null}.
     * @param constraint
     *            the optional constraint. It must not be {@code null}.
     *
     * @return the next snapshot version
     *
     * @throws MojoExecutionException
     *             if the constraint does not allow to raise the version
     *             baseline
     */
    private static Version computeTargetVersion(Version baseline, Optional<Version> constraint) throws MojoExecutionException {
        if (!constraint.isPresent()) { // No constraint, use the next major version
            return VersionVariance.MAJOR.apply(baseline);
        }

        final Version versionConstraint = constraint.get();
        return Stream.of(VersionVariance.MAJOR, VersionVariance.MINOR, VersionVariance.MICRO)   // Try all adequate version changes, from the major one
                .map(variance -> variance.apply(baseline))                                      // Apply to get the candidate version
                .filter(version -> version.compareTo(versionConstraint) < 0)                    // The candidate version must still be constrained!
                .findFirst().orElseThrow(() -> {
                    return new MojoExecutionException("Version constraint effectively freezes the version.");
                });
    }
}
