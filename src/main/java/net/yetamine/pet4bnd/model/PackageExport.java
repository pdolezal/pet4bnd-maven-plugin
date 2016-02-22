package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a template for a package export directive.
 */
public final class PackageExport extends VersionStatement {

    /** Attributes of the export directive. */
    private final Optional<String> attributes;
    /** Name of the package to export. */
    private final String packageName;

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to use. It must not be {@code null}.
     */
    PackageExport(VersionDefinition definition) {
        super(definition);
        packageName = Objects.requireNonNull(definition.name());
        attributes = Optional.ofNullable(definition.attributes());
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
}
