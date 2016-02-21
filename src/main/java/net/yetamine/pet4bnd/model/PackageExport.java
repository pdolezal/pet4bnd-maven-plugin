package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a template for a package export directive.
 */
public final class PackageExport {

    /** Node with the version baseline. */
    private final ValueNode<Version> versionBaseline;
    /** Node with the version constraint. */
    private final ValueNode<Version> versionConstraint;
    /** Node with the version variance. */
    private final ValueNode<VersionVariance> versionVariance;
    /** Attributes of the export directive. */
    private final Optional<String> attributes;
    /** Name of the package to export. */
    private final String packageName;

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to use. It must not be {@code null}.
     */
    PackageExport(VersionDefinition definition) {
        packageName = Objects.requireNonNull(definition.name());
        attributes = Optional.ofNullable(definition.attributes());
        versionConstraint = definition.versionConstraint();
        versionVariance = definition.versionVariance();
        versionBaseline = definition.versionBaseline();
    }

    /**
     * Returns the name of the exported package.
     *
     * @return the name of the exported package
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Returns the attributes of the package export.
     *
     * @return the attributes of the package export
     */
    public Optional<String> attributes() {
        return attributes;
    }

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, or {@link Version#ZERO} if not present
     */
    public Version versionBaseline() {
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
    public PackageExport versionBaseline(Version value) {
        versionBaseline.set(value);
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
    public PackageExport versionConstraint(Version value) {
        versionConstraint.set(value);
        return this;
    }

    /**
     * Returns the version variance.
     *
     * @return the version variance, or {@link VersionVariance#NONE} if not
     *         present
     */
    public VersionVariance versionVariance() {
        return versionVariance.use().orElse(VersionVariance.NONE);
    }

    /**
     * Sets the version variance.
     *
     * @param value
     *            the version variance to set. It must not be {@code null}.
     *
     * @return this instance
     */
    public PackageExport versionVariance(VersionVariance value) {
        versionVariance.set(value);
        return this;
    }
}
