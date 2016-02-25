package net.yetamine.pet4bnd.model;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a definition statement.
 */
public abstract class VersionDefinition implements VersionStatement {

    /** Current resolution. */
    private Version resolution;
    /** Version baseline. */
    private Version baseline;
    /** Version constraint. */
    private Version constraint;
    /** Version variance. */
    private VersionVariance variance;

    /**
     * Prepares a new instance.
     */
    protected VersionDefinition() {
        // Default constructor
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#resolve(net.yetamine.pet4bnd.version.Version)
     */
    public void resolve(Version value) {
        resolution = value;
    }

    /**
     * Returns the resolution.
     *
     * <p>
     * This method should be overridden because it just provides the backing
     * field content, which might not be always appropriate.
     *
     * @see net.yetamine.pet4bnd.model.VersionStatement#resolution()
     */
    public Version resolution() {
        return resolution;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline()
     */
    public Version baseline() {
        return baseline;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline(net.yetamine.pet4bnd.version.Version)
     */
    public void baseline(Version value) {
        baseline = value;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint()
     */
    public Version constraint() {
        return constraint;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint(net.yetamine.pet4bnd.version.Version)
     */
    public void constraint(Version value) {
        constraint = value;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance()
     */
    public VersionVariance variance() {
        return variance;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance(net.yetamine.pet4bnd.version.VersionVariance)
     */
    public void variance(VersionVariance value) {
        variance = value;
    }
}
