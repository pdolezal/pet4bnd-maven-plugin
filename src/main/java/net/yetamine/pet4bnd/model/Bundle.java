package net.yetamine.pet4bnd.model;

import java.util.Map;

import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a bundle.
 */
public interface Bundle {

    /**
     * Provides the bundle version.
     *
     * @return the bundle version
     */
    BundleVersion version();

    /**
     * Provides the view of the package exports.
     *
     * @return the package exports
     */
    Map<String, PackageExport> exports();

    /**
     * Restores the baseline from the resolution.
     */
    default void restore() {
        final BundleVersion bundleVersion = version();
        bundleVersion.baseline(bundleVersion.resolution());
        if (bundleVersion.variance() != null) {
            bundleVersion.variance(VersionVariance.NONE);
        }

        exports().values().forEach(PackageExport::restore);
    }
}
