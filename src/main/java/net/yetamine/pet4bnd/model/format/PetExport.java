package net.yetamine.pet4bnd.model.format;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.VersionStatement;

/**
 * Represents a template for a package export directive.
 */
final class PetExport implements PackageExport {

    /** Definition of the export. */
    private final VersionDefinition definition;
    /** Attributes of the export directive. */
    private final Optional<String> attributes;

    /**
     * Creates a new instance.
     *
     * @param version
     *            the definition to use. It must not be {@code null}.
     * @param exportAttributes
     *            the attributes
     */
    public PetExport(VersionDefinition version, String exportAttributes) {
        definition = Objects.requireNonNull(version);
        attributes = Optional.ofNullable(exportAttributes);
    }

    /**
     * Returns the name of the exported package.
     *
     * @return the name of the exported package
     */
    public String packageName() {
        return definition.identifier();
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
    public VersionStatement version() {
        return definition;
    }
}
