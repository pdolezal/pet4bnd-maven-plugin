package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.version.Version;

/**
 * Describes bundle version options.
 */
public final class BundleOptions {

    /** Version baseline for this bundle. */
    private final ValueNode<Version> versionBaseline;
    /** Version constraints for this bundle. */
    private final ValueNode<Version> versionConstraint;

    /**
     * Creates a new instance.
     *
     * @param versionBaselineNode
     *            the bundle version baseline. It must not be {@code null}.
     * @param versionConstraintNode
     *            the bundle version constraint. It must not be {@code null}.
     */
    BundleOptions(ValueNode<Version> versionBaselineNode, ValueNode<Version> versionConstraintNode) {
        versionBaseline = Objects.requireNonNull(versionBaselineNode);
        versionConstraint = Objects.requireNonNull(versionConstraintNode);
    }

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to use. It must not be {@code null}.
     */
    BundleOptions(VersionDefinition definition) {
        this(definition.versionBaseline(), definition.versionConstraint());
    }

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, or {@link Version#ZERO} if not present
     */
    public Version versionBaseline() {
        return versionBaseline.get();
    }

    /**
     * Sets the version baseline.
     *
     * @param value
     *            the version baseline to set. It must not be {@code null}.
     *
     * @return this instance
     */
    public BundleOptions versionBaseline(Version value) {
        versionBaseline.set(Objects.requireNonNull(value));
        return this;
    }

    /**
     * Returns the version constraint.
     *
     * @return the version constraint, or an empty container if no constraint
     *         exists
     */
    public Optional<Version> versionConstraint() {
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
    public BundleOptions versionConstraint(Version value) {
        versionConstraint.set(value);
        return this;
    }
}
