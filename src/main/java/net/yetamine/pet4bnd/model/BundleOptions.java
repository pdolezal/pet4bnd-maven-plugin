package net.yetamine.pet4bnd.model;

/**
 * Describes bundle version options.
 */
public final class BundleOptions extends VersionStatement {

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to use. It must not be {@code null}.
     */
    BundleOptions(VersionDefinition definition) {
        super(definition);
    }
}
