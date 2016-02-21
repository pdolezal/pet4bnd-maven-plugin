package net.yetamine.pet4bnd.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a bundle.
 */
public class Bundle {

    /** Bundle options description. */
    private final BundleOptions options;
    /** Packages that the bundle exports. */
    private final Map<String, PackageExport> exports;

    /**
     * Creates a new instance.
     *
     * @param bundleOptions
     *            the bundle options. It must not be {@code null}.
     * @param packageExports
     *            the package exports. It must not be {@code null}.
     */
    protected Bundle(BundleOptions bundleOptions, Map<String, PackageExport> packageExports) {
        exports = Objects.requireNonNull(packageExports);
        options = Objects.requireNonNull(bundleOptions);
    }

    /**
     * Finds the variance of the bundle if matching the constraints.
     *
     * @param feedback
     *            the feedback to record errors. It must not be {@code null}.
     *
     * @return the variance of the bundle
     */
    public final Optional<VersionVariance> resolution(Feedback feedback) {
        Objects.requireNonNull(feedback);

        boolean varianceViolation = false;
        VersionVariance bundleVariance = VersionVariance.NONE;
        for (PackageExport export : exports.values()) {
            final VersionVariance variance = export.versionVariance();
            if (bundleVariance.compareTo(variance) < 0) { // Record the largest one
                bundleVariance = variance;
            }

            final Optional<Version> constraint = export.versionConstraint();

            if (constraint.isPresent()) { // Check the version constraint against the effective version
                final Version exportVersion = variance.apply(export.versionBaseline());
                final Version exportConstraint = constraint.get();

                if (exportConstraint.compareTo(exportVersion) <= 0) {
                    final String f = "Package '%s': target version %s violates version constraint %s.";
                    feedback.fail(String.format(f, export.packageName(), exportVersion, exportConstraint));
                    varianceViolation = true;
                }
            }
        }

        if (varianceViolation) {
            return Optional.empty();
        }

        // Check the bundle version
        final Version bundleVersion = bundleVariance.apply(options.versionBaseline());
        final Optional<Version> constraint = options.versionConstraint();
        if (constraint.map(v -> v.compareTo(bundleVersion) <= 0).orElse(false)) {
            final String f = "Target bundle version %s violates version restriction to %s.";
            feedback.fail(String.format(f, bundleVersion, constraint));
            return Optional.empty();
        }

        return Optional.of(bundleVariance);
    }

    /**
     * Resolves the packages and bundle versions.
     *
     * @param bundleVariance
     *            the bundle variance. It must not be {@code null}.
     *
     * @throws IllegalStateException
     *             if the conditions are not acceptable. To find out the bundle
     *             variance and to check if the resolution is possible, some of
     *             the {@link #resolution()} overload is available.
     */
    public final void resolve(VersionVariance bundleVariance) {
        final Version bundleVersion = bundleVariance.apply(options.versionBaseline());
        final Optional<Version> bundleConstraint = options.versionConstraint();
        if (bundleConstraint.map(v -> v.compareTo(bundleVersion) <= 0).orElse(false)) {
            throw new IllegalStateException();
        }

        for (PackageExport export : exports.values()) {
            final Version version = export.versionVariance().apply(export.versionBaseline());
            final Optional<Version> constraint = export.versionConstraint();
            if (constraint.map(v -> v.compareTo(version) <= 0).orElse(false)) {
                throw new IllegalStateException();
            }

            // Reset the package version to the new baseline
            export.versionVariance(VersionVariance.NONE);
            export.versionBaseline(version);
        }

        options.versionBaseline(bundleVersion);
    }

    /**
     * Finds the variance of the bundle if matching the constraints.
     *
     * @return the variance of the bundle
     */
    public final Optional<VersionVariance> resolution() {
        return resolution(Feedback.none());
    }

    /**
     * Provides the view of the package exports.
     *
     * @return the package exports
     */
    public final Map<String, PackageExport> exports() {
        return exports;
    }

    /**
     * Provides the bundle option.
     *
     * @return the bundle options
     */
    public final BundleOptions options() {
        return options;
    }
}
