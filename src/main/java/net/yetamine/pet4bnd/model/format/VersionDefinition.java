package net.yetamine.pet4bnd.model.format;

import java.util.Optional;

import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a definition statement.
 */
final class VersionDefinition implements VersionStatement {

    /** Node with the version baseline (mandatory, should not stay empty). */
    private final ValueNode<Version> baseline = new ValueNode<>();
    /** Node with the version constraint (optional, may stay empty). */
    private final ValueNode<Version> constraint = new ValueNode<>();
    /** Node with the version variance (optional, may stay empty). */
    private final ValueNode<VersionVariance> variance = new ValueNode<>();
    /** Identifier of this definition. */
    private String identifier;

    /**
     * Creates a new instance.
     */
    public VersionDefinition() {
        // Default constructor
    }

    /**
     * Returns the version baseline node.
     *
     * @return the version baseline node
     */
    public ValueNode<Version> baselineNode() {
        return baseline;
    }

    /**
     * Returns the version constraint node.
     *
     * @return the version constraint node
     */
    public ValueNode<Version> constraintNode() {
        return constraint;
    }

    /**
     * Returns the version variance node.
     *
     * @return the version variance node
     */
    public ValueNode<VersionVariance> varianceNode() {
        return variance;
    }

    /**
     * Sets the identifier of this definition.
     *
     * @param value
     *            the identifier
     *
     * @return this instance
     */
    public VersionDefinition identifier(String value) {
        identifier = value;
        return this;
    }

    /**
     * Returns the identifier of this definition.
     *
     * @return the identifier of this definition, or {@code null} if none
     */
    public String identifier() {
        return identifier;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline()
     */
    public Version baseline() {
        return baseline.use().orElse(Version.ZERO);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline(net.yetamine.pet4bnd.version.Version)
     */
    public VersionDefinition baseline(Version value) {
        baseline.set(value);
        return this;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint()
     */
    public Optional<Version> constraint() {
        return constraint.use();
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint(net.yetamine.pet4bnd.version.Version)
     */
    public VersionDefinition constraint(Version value) {
        constraint.set(value);
        return this;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance()
     */
    public Optional<VersionVariance> variance() {
        return variance.use();
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance(net.yetamine.pet4bnd.version.VersionVariance)
     */
    public VersionDefinition variance(VersionVariance value) {
        variance.set(value);
        return this;
    }
}
