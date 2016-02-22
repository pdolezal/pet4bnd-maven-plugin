package net.yetamine.pet4bnd.model;

import java.util.Optional;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a version statement.
 */
public interface VersionStatement {

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, or {@link Version#ZERO} if not present
     */
    Version baseline();

    /**
     * Sets the version baseline.
     *
     * @param value
     *            the version baseline to set. It must not be {@code null}.
     *
     * @return this instance
     */
    VersionStatement baseline(Version value);

    /**
     * Returns the version constraint.
     *
     * @return the version constraint, or an empty container if no constraint
     *         exists
     */
    Optional<Version> constraint();

    /**
     * Sets the version constraint.
     *
     * @param value
     *            the version constraint to set. It may be {@code null} if no
     *            constraint shall exist.
     *
     * @return this instance
     */
    VersionStatement constraint(Version value);

    /**
     * Returns the version variance.
     *
     * @return the version variance, or an empty container if the version is not
     *         managed automatically
     */
    Optional<VersionVariance> variance();

    /**
     * Sets the version variance.
     *
     * @param value
     *            the version variance to set. It must not be {@code null}.
     *
     * @return this instance
     */
    VersionStatement variance(VersionVariance value);
}
