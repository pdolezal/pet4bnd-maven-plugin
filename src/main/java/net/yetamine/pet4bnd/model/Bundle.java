package net.yetamine.pet4bnd.model;

import java.util.Map;

/**
 * Represents a bundle.
 */
public interface Bundle {

    /**
     * Provides the bundle version.
     *
     * @return the bundle version
     */
    VersionStatement version();

    /**
     * Provides the view of the package exports.
     *
     * @return the package exports
     */
    Map<String, PackageExport> exports();
}
