package net.yetamine.pet4bnd.model.support;

import java.util.Objects;

import net.yetamine.pet4bnd.model.VersionGroup;

/**
 * Represents a version group for packages.
 */
public final class PackageGroupDefinition extends VersionDefinition implements VersionGroup {

    /** Inheritance source. */
    private final String identifier;

    /**
     * Creates a new instance.
     *
     * @param name
     *            the name of the group. It must not be {@code null}.
     */
    public PackageGroupDefinition(String name) {
        identifier = Objects.requireNonNull(name);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionGroup#identifier()
     */
    public String identifier() {
        return identifier;
    }
}
