package net.yetamine.pet4bnd.model;

import java.util.Optional;

/**
 * Represents a package export directive.
 */
public interface PackageExport {

    /**
     * Returns the name of the exported package.
     *
     * @return the name of the exported package
     */
    String packageName();

    /**
     * Returns the export version.
     *
     * @return the export version
     */
    PackageVersion version();

    /**
     * Returns the attributes of the package export.
     *
     * @return the attributes of the package export
     */
    Optional<String> attributes();

    /**
     * Restores the baselines from the resolution.
     */
    default void restore() {
        version().restore();
    }
}
