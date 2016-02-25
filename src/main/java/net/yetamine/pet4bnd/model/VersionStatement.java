package net.yetamine.pet4bnd.model;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a version statement.
 */
public interface VersionStatement {

    /**
     * Tests if the effective version is valid with the respect to the
     * constraint.
     *
     * @return {@code true} if no constraint exists or the effective version is
     *         below the constraint
     */
    default boolean test() {
        final Version constraint = constraint();
        return (constraint == null) || (resolution().compareTo(constraint) < 0);
    }

    /**
     * Overrides the effective version.
     *
     * @param value
     *            the value to set. It may be {@code null} to restore default
     *            resolution (i.e., deriving the result from the baseline).
     */
    void resolve(Version value);

    /**
     * Returns the effective version.
     *
     * <p>
     * Until overridden, the result is the baseline with the variance applied,
     * or when the baseline is inherited, then the inherited effective version
     * (hence ignoring the local variance then).
     *
     * @return the effective version
     */
    Version resolution();

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, or {@code null} if the baseline shall be
     *         inherited
     */
    Version baseline();

    /**
     * Sets the version baseline.
     *
     * @param value
     *            the version baseline to set. It may be {@code null} if no the
     *            value shall be rather inherited (if possible). If inheritance
     *            is not applicable, the current value shall be preserved.
     */
    void baseline(Version value);

    /**
     * Returns the version constraint.
     *
     * @return the version constraint, or {@code null} if no constraint exists
     */
    Version constraint();

    /**
     * Sets the version constraint.
     *
     * @param value
     *            the version constraint to set. It may be {@code null} if no
     *            constraint shall exist.
     */
    void constraint(Version value);

    /**
     * Returns the version variance.
     *
     * @return the version variance, or {@code null} if no variance given and
     *         the effective version should be equal to the baseline always
     */
    VersionVariance variance();

    /**
     * Sets the version variance.
     *
     * @param value
     *            the version variance to set. It may be {@code null} for
     *            disabling the variance influence.
     */
    void variance(VersionVariance value);
}
