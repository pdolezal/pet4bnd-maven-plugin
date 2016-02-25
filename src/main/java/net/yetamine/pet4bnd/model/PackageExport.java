package net.yetamine.pet4bnd.model;

import java.util.Optional;

import net.yetamine.pet4bnd.version.VersionVariance;

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
     * Restores the baseline from the resolution.
     */
    default void restore() {
        final PackageVersion exportVersion = version();

        if (exportVersion.baseline() != null) { // Do not override inheritance
            exportVersion.baseline(exportVersion.resolution());
        }

        if (exportVersion.variance() != null) { // Do not override manual versions
            exportVersion.variance(VersionVariance.NONE);
        }
    }
}
