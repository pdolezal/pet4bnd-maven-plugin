package net.yetamine.pet4bnd.model;

import java.util.Objects;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * The default variance resolution strategy with no feedback.
 */
public class VersionResolver {

    /** Target bundle. */
    private final Bundle bundle;

    /**
     * Creates a new instance.
     *
     * @param resolutionTarget
     *            the bundle to resolve. It must not be {@code null}.
     */
    public VersionResolver(Bundle resolutionTarget) {
        bundle = Objects.requireNonNull(resolutionTarget);
    }

    /**
     * Resolves the effective versions of the bundle and all packages.
     *
     * @param bundle
     *            the bundle to resolve. It must not be {@code null}.
     */
    public static void resolve(Bundle bundle) {
        final BundleVersion bundleVersion = bundle.version();

        // Get the variance for resolving the bundle at first
        VersionVariance resolutionVariance = bundleVersion.variance();
        if (resolutionVariance == null) {
            resolutionVariance = VersionVariance.NONE;
        }

        // If the variance is not the maximum, it must be found out precisely
        if (resolutionVariance != VersionVariance.MAJOR) {
            for (PackageExport export : bundle.exports().values()) {
                final VersionVariance exportVariance = export.version().variance();
                if ((exportVariance != null) && (resolutionVariance.compareTo(exportVariance) < 0)) {
                    resolutionVariance = exportVariance; // Get the maximum of them
                    if (resolutionVariance == VersionVariance.MAJOR) {
                        break; // Short-circuit evaluation
                    }
                }
            }
        }

        // Resolve the bundle version
        bundleVersion.resolve(resolutionVariance.apply(bundleVersion.baseline()));
        if (bundleVersion.variance() != null) { // Clear it, but if necessary only
            bundleVersion.variance(VersionVariance.NONE);
        }

        // Resolve the packages now (inheritance shall apply this time)
        for (PackageExport export : bundle.exports().values()) {
            final PackageVersion exportVersion = export.version();

            final VersionVariance exportVariance = exportVersion.variance();
            if (exportVariance == null) { // Manually versioned package
                exportVersion.resolve(null);
                continue;
            }

            final Version exportBaseline = exportVersion.baseline();
            // If not inheriting the version, compute the new version, otherwise force inheritance
            exportVersion.resolve((exportBaseline != null) ? exportVariance.apply(exportBaseline) : null);
            exportVersion.variance(VersionVariance.NONE);
        }
    }

    /**
     * Returns the bundle to resolve.
     *
     * @return the bundle to resolve
     */
    public final Bundle bundle() {
        return bundle;
    }

    /**
     * Resolves the effective versions of the bundle and all packages.
     *
     * @return this instance
     */
    public final VersionResolver resolve() {
        resolve(bundle);
        return this;
    }

    /**
     * Returns the effective versions are valid.
     *
     * @return {@code true} if the effective versions are valid, {@code false}
     *         otherwise
     */
    public final boolean test() {
        boolean result = true;

        // Check the package versions
        for (PackageExport export : bundle.exports().values()) {
            final PackageVersion exportVersion = export.version();
            final Version exportConstraint = exportVersion.constraint();
            if (exportConstraint == null) {
                continue;
            }

            if (exportConstraint.compareTo(exportVersion.resolution()) <= 0) {
                if (constraintViolated(export)) {
                    return false;
                }

                result = false;
            }
        }

        // Check the bundle version
        final BundleVersion bundleVersion = bundle.version();
        final Version bundleConstraint = bundleVersion.constraint();
        if ((bundleConstraint != null) && (bundleConstraint.compareTo(bundleVersion.resolution()) <= 0)) {
            constraintViolated();
            return false;
        }

        return result;
    }

    /**
     * Reports a violation of a package version constraint.
     *
     * <p>
     * The default implementation just returns {@code true}, but the method is
     * supposed to be overridden to provide the feedback to users; although it
     * may throw an exception, which would be relayed to the caller, it is not
     * recommended.
     *
     * @param failingExport
     *            the offending export definition. It must not be {@code null}.
     *
     * @return {@code true} if the processing shall abort immediately, otherwise
     *         it proceeds as long as possible, providing feedback for all
     *         exports
     */
    protected boolean constraintViolated(PackageExport failingExport) {
        return true;
    }

    /**
     * Reports a violation of the bundle version constraint.
     *
     * <p>
     * The default implementation does nothing, but the method is supposed to be
     * overridden to provide the feedback to users; although it may throw an
     * exception, which would be relayed to the caller, it is not recommended.
     */
    protected void constraintViolated() {
        // Do nothing
    }
}
