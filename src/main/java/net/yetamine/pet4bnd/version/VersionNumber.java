package net.yetamine.pet4bnd.version;

/**
 * Represents the version number (a part) of a version.
 *
 * <p>
 * The version number parts are ordered according to their significance and
 * usual order in the version representation: major, minor, micro.
 */
public enum VersionNumber {

    /**
     * The major version part.
     */
    MAJOR {

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#get(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public int get(Version version) {
            return version.major();
        }

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#set(net.yetamine.pet4bnd.version.Version,
         *      int)
         */
        @Override
        public Version set(Version version, int value) {
            return version.major(value);
        }
    },

    /**
     * The minor version part.
     */
    MINOR {

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#get(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public int get(Version version) {
            return version.minor();
        }

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#set(net.yetamine.pet4bnd.version.Version,
         *      int)
         */
        @Override
        public Version set(Version version, int value) {
            return version.minor(value);
        }
    },

    /**
     * The micro version part.
     */
    MICRO {

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#get(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public int get(Version version) {
            return version.micro();
        }

        /**
         * @see net.yetamine.pet4bnd.version.VersionNumber#set(net.yetamine.pet4bnd.version.Version,
         *      int)
         */
        @Override
        public Version set(Version version, int value) {
            return version.micro(value);
        }
    };

    /**
     * Returns the version number according to this component.
     *
     * @param version
     *            the version to use. It must not be {@code null}.
     *
     * @return the version number according to this component
     *
     * @see java.util.function.ToIntFunction#applyAsInt(java.lang.Object)
     */
    public abstract int get(Version version);

    /**
     * Sets the version number according to this component.
     *
     * @param version
     *            the version to adjust. It must not be {@code null}.
     * @param value
     *            the new number of the version. It must not be negative.
     *
     * @return the adjusted version
     */
    public abstract Version set(Version version, int value);
}
