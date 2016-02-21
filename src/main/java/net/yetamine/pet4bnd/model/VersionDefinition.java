package net.yetamine.pet4bnd.model;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a definition statement.
 */
final class VersionDefinition {

    /** Node with the version baseline (mandatory, should not stay empty). */
    private final ValueNode<Version> versionBaseline = new ValueNode<>();
    /** Node with the version constraint (optional, may stay empty). */
    private final ValueNode<Version> versionConstraint = new ValueNode<>();
    /** Node with the version variance (optional, may stay empty). */
    private final ValueNode<VersionVariance> versionVariance = new ValueNode<>();
    /** Attributes of the object. */
    private String attributes;
    /** Name of the object. */
    private String name;

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
    public ValueNode<Version> versionBaseline() {
        return versionBaseline;
    }

    /**
     * Returns the version constraint node.
     *
     * @return the version constraint node
     */
    public ValueNode<Version> versionConstraint() {
        return versionConstraint;
    }

    /**
     * Returns the version variance node.
     *
     * @return the version variance node
     */
    public ValueNode<VersionVariance> versionVariance() {
        return versionVariance;
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes, or {@code null} if none
     */
    public String attributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param value
     *            the attributes
     *
     * @return this instance
     */
    public VersionDefinition attributes(String value) {
        attributes = value;
        return this;
    }

    /**
     * Sets the name of this definition.
     *
     * @param value
     *            the name
     *
     * @return this instance
     */
    public VersionDefinition name(String value) {
        name = value;
        return this;
    }

    /**
     * Returns the name of this definition.
     *
     * @return the name of this definition, or {@code null} if none
     */
    public String name() {
        return name;
    }
}
