package net.yetamine.pet4bnd.model.support;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.PackageVersion;

/**
 * Represents a package export directive.
 */
public final class PackageExportDefinition implements PackageExport {

    /** Name of this package. */
    private final String packageName;
    /** Export version definition. */
    private final PackageVersion version;
    /** Attributes of the export directive. */
    private final Optional<String> attributes;

    /**
     * Creates a new instance.
     *
     * @param exportIdentifier
     *            the package name. It must not be {@code null}.
     * @param versionDefinition
     *            the definition to use. It must not be {@code null}.
     * @param exportAttributes
     *            the attributes
     */
    public PackageExportDefinition(String exportIdentifier, PackageVersion versionDefinition, String exportAttributes) {
        packageName = Objects.requireNonNull(exportIdentifier);
        version = Objects.requireNonNull(versionDefinition);
        attributes = Optional.ofNullable(exportAttributes);
    }

    /**
     * Returns the name of the exported package.
     *
     * @return the name of the exported package
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Returns the attributes of the package export.
     *
     * @return the attributes of the package export
     */
    public Optional<String> attributes() {
        return attributes;
    }

    /**
     * @see net.yetamine.pet4bnd.model.PackageExport#version()
     */
    public PackageVersion version() {
        return version;
    }
}
