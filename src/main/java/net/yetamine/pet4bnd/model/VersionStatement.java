package net.yetamine.pet4bnd.model;

import java.util.Optional;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a version statement.
 */
public class VersionStatement {

    /** Node with the version baseline. */
    private final ValueNode<Version> versionBaseline;
    /** Node with the version constraint. */
    private final ValueNode<Version> versionConstraint;
    /** Node with the version variance. */
    private final ValueNode<VersionVariance> versionVariance;

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to use. It must not be {@code null}.
     */
    VersionStatement(VersionDefinition definition) {
        versionConstraint = definition.versionConstraint();
        versionVariance = definition.versionVariance();
        versionBaseline = definition.versionBaseline();
    }

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, or {@link Version#ZERO} if not present
     */
    public final Version versionBaseline() {
        return versionBaseline.use().orElse(Version.ZERO);
    }

    /**
     * Sets the version baseline.
     *
     * @param value
     *            the version baseline to set. It must not be {@code null}.
     *
     * @return this instance
     */
    public final VersionStatement versionBaseline(Version value) {
        versionBaseline.set(value);
        return this;
    }

    /**
     * Returns the version constraint.
     *
     * @return the version constraint, or an empty container if no constraint
     *         exists
     */
    public final Optional<Version> versionConstraint() {
        return versionConstraint.use();
    }

    /**
     * Sets the version constraint.
     *
     * @param value
     *            the version constraint to set. It may be {@code null} if no
     *            constraint shall exist.
     *
     * @return this instance
     */
    public final VersionStatement versionConstraint(Version value) {
        versionConstraint.set(value);
        return this;
    }

    /**
     * Returns the version variance.
     *
     * @return the version variance, or an empty container if the version is not
     *         managed automatically
     */
    public final Optional<VersionVariance> versionVariance() {
        return versionVariance.use();
    }

    /**
     * Sets the version variance.
     *
     * @param value
     *            the version variance to set. It must not be {@code null}.
     *
     * @return this instance
     */
    public final VersionStatement versionVariance(VersionVariance value) {
        versionVariance.set(value);
        return this;
    }
}
