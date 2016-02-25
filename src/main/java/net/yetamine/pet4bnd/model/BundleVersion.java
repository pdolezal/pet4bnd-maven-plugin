package net.yetamine.pet4bnd.model;

import net.yetamine.pet4bnd.version.Version;

/**
 * Represents a version statement for a package.
 */
public interface BundleVersion extends VersionStatement {

    /**
     * Returns the version baseline.
     *
     * @return the version baseline, never {@code null}
     *
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline()
     */
    Version baseline();
}
