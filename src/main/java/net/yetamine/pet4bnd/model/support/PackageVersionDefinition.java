package net.yetamine.pet4bnd.model.support;

import java.util.Optional;

import net.yetamine.pet4bnd.model.PackageVersion;
import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;

/**
 * Represents a definition statement for a package.
 */
public final class PackageVersionDefinition extends VersionDefinition implements PackageVersion {

    /** Inheritance source. */
    private VersionStatement inheritance;

    /**
     * Creates a new instance.
     */
    public PackageVersionDefinition() {
        // Default constructor
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#baseline()
     */
    @Override
    public Version baseline() {
        final Version result = super.baseline();
        return (result != null) ? result : inheritance.baseline();
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#baseline(net.yetamine.pet4bnd.version.Version)
     */
    @Override
    public void baseline(Version value) {
        super.baseline(value);
        inheritance = null;
    }

    /**
     * @see net.yetamine.pet4bnd.model.PackageVersion#inheritance()
     */
    public Optional<VersionStatement> inheritance() {
        return Optional.ofNullable(inheritance);
    }

    /**
     * @see net.yetamine.pet4bnd.model.PackageVersion#inherit(net.yetamine.pet4bnd.model.VersionStatement)
     */
    public void inherit(VersionStatement source) {
        if (source != null) {
            inheritance = source;
            return;
        }

        if (inheritance != null) {
            super.resolve(inheritance.resolution());
            super.baseline(inheritance.baseline());
            inheritance = null;
        }
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#resolve()
     */
    @Override
    protected Version resolve() {
        return (inheritance != null) ? inheritance.resolution() : super.resolve();
    }
}
