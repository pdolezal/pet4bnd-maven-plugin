package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * The default variance resolution strategy with no feedback.
 */
public class VersionResolver {

    /** Target bundle. */
    private final Bundle bundle;
    /** Resolution result. */
    private Optional<VersionVariance> resolution = Optional.empty();

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
     * Determines the resolution if possible.
     *
     * @return this instance
     */
    public final VersionResolver determine() {
        resolution = Optional.ofNullable(determineVariance());
        return this;
    }

    /**
     * Returns the resolution if exists.
     *
     * @return the resolution provided by {@link #determine()} if a resolution
     *         is possible
     */
    public final Optional<VersionVariance> resolution() {
        return resolution;
    }

    /**
     * Resolves the packages and bundle versions with the respect to the given
     * variance.
     *
     * <p>
     * To check if the resolution is possible, use {@link #determine()}, after
     * which, no change of the bundle should occur, otherwise risking a failure
     * of this method.
     *
     * @throws IllegalStateException
     *             if the conditions are not acceptable
     */
    public final void resolve() {
        resolveVariance(resolution.orElseThrow(IllegalStateException::new));
    }

    /**
     * Reports a violation of the bundle version constraint.
     *
     * <p>
     * The default implementation does nothing, but the method is supposed to be
     * overridden to provide the feedback to users; it may throw an exception,
     * which would be relayed to the caller, but it is not recommended.
     *
     * @param targetBundle
     *            the bundle. It must not be {@code null}.
     * @param targetVersion
     *            the target bundle version. It must not be {@code null}.
     */
    protected void fail(Bundle targetBundle, Version targetVersion) {
        // Do nothing
    }

    /**
     * Reports a violation of the bundle version constraint.
     *
     * <p>
     * The default implementation just returns {@code true}, but the method is
     * supposed to be overridden to provide the feedback to users; it may throw
     * an exception, which would be relayed to the caller, but it is not
     * recommended.
     *
     * @param export
     *            the offending export definition. It must not be {@code null}.
     * @param exportVersion
     *            the target export version. It must not be {@code null}.
     *
     * @return {@code true} if the processing shall abort immediately, otherwise
     *         it proceeds as long as possible, providing feedback for all
     *         exports
     */
    protected boolean fail(PackageExport export, Version exportVersion) {
        return true;
    }

    /**
     * Determines the bundle variance.
     *
     * @return the bundle variance, or {@code null} if not possible
     */
    private VersionVariance determineVariance() {
        final VersionStatement versionStatement = bundle.version();
        VersionVariance result = versionStatement.variance().orElse(VersionVariance.NONE);
        boolean violatingVariance = false;

        for (PackageExport export : bundle.exports().values()) {
            final VersionStatement version = export.version();
            final VersionVariance variance = version.variance().orElse(VersionVariance.NONE);
            if (result.compareTo(variance) < 0) { // Record the largest one
                result = variance;
            }

            final Optional<Version> constraint = version.constraint();

            if (constraint.isPresent()) { // Check the version constraint against the effective version
                final Version exportVersion = variance.apply(version.baseline());
                final Version exportConstraint = constraint.get();

                if (exportConstraint.compareTo(exportVersion) <= 0) {
                    if (fail(export, exportVersion)) {
                        return null;
                    }

                    violatingVariance = true;
                }
            }
        }

        if (violatingVariance) {
            return null;
        }

        // Check the bundle version
        final Version version = result.apply(versionStatement.baseline());
        final Optional<Version> constraint = versionStatement.constraint();
        if (constraint.map(v -> v.compareTo(version) <= 0).orElse(false)) {
            fail(bundle, version);
            return null;
        }

        return result;
    }

    /**
     * Resolves to the given variance.
     *
     * @param variance
     *            the variance to resolve to. It must not be {@code null}.
     *
     * @throws IllegalStateException
     *             if the resolution not possible
     */
    private final void resolveVariance(VersionVariance variance) {
        final VersionStatement versionStatement = bundle.version();
        final Version version = variance.apply(versionStatement.baseline());
        final Optional<Version> constraint = versionStatement.constraint();
        if (constraint.map(v -> v.compareTo(version) <= 0).orElse(false)) {
            throw new IllegalStateException();
        }

        versionStatement.variance().ifPresent(v -> {
            versionStatement.variance(VersionVariance.NONE);
        });

        for (PackageExport export : bundle.exports().values()) {
            final VersionStatement packageVersion = export.version();
            final Optional<VersionVariance> packageVariance = packageVersion.variance();
            final Version exportVersion = packageVariance.orElse(VersionVariance.NONE).apply(packageVersion.baseline());
            final Optional<Version> packageConstraint = packageVersion.constraint();
            if (packageConstraint.map(v -> v.compareTo(exportVersion) <= 0).orElse(false)) {
                throw new IllegalStateException();
            }

            packageVariance.ifPresent(v -> {
                packageVersion.variance(VersionVariance.NONE);
                packageVersion.baseline(exportVersion);
            });
        }

        versionStatement.baseline(version);
    }
}
