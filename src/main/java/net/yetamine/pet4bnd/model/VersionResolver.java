package net.yetamine.pet4bnd.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
     * Resolves the versions for the bundle and all packages.
     *
     * @param bundle
     *            the bundle to resolve. It must not be {@code null}.
     */
    public static void resolve(Bundle bundle) {
        final BundleVersion bundleVersion = bundle.version();

        // Get the variance for resolving the bundle at first
        VersionVariance resolutionVariance = bundleVersion.variance().orElse(VersionVariance.NONE);
        // If the variance is not the maximum, it must be found out precisely
        if (resolutionVariance != VersionVariance.MAJOR) {
            for (PackageExport export : bundle.exports().values()) {
                final VersionVariance variance = export.version().variance().orElse(VersionVariance.NONE);

                if (resolutionVariance.compareTo(variance) < 0) {
                    resolutionVariance = variance; // Get the maximum of them
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

        // Record the inheritance sources and the effective variances for them
        final Map<VersionStatement, VersionVariance> sources = new HashMap<>();
        // Force default resolution for all exports and get their inheritance sources
        bundle.exports().values().stream().map(PackageExport::version).forEach(version -> {
            version.inheritance().filter(source -> (source != bundleVersion)).ifPresent(source -> {
                // Remember the source, if not done already, with its current variance
                final VersionVariance current = sources.computeIfAbsent(source, v -> {
                    return v.variance().orElse(VersionVariance.NONE);
                });

                // If there is any variance, greater than the current one, update the record
                version.variance().filter(v -> current.compareTo(v) < 0).ifPresent(v -> sources.put(source, v));
            });

            version.resolve(null);
        });

        // Force the resolution for the sources then
        sources.forEach((source, variance) -> source.resolve(variance.apply(source.baseline())));
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
     * Resolves the versions of the bundle and all packages.
     *
     * @return this instance
     */
    public final VersionResolver resolve() {
        resolve(bundle);
        return this;
    }

    /**
     * Tests if all version resolutions for the bundle and all packages are
     * valid.
     *
     * @return {@code true} if all version resolutions are valid, {@code false}
     *         otherwise
     */
    public final boolean test() {
        boolean result = true;

        // Check the package versions first to get the full report possibly
        for (PackageExport export : bundle.exports().values()) {
            if (export.version().test()) {
                continue;
            }

            if (constraintViolated(export)) {
                return false;
            }

            result = false;
        }

        // Check the bundle version
        if (bundle.version().test()) {
            return result;
        }

        constraintViolated();
        return false;
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
