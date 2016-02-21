package net.yetamine.pet4bnd.version;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Provides the usual version adjustment according to the modification scope.
 */
public enum VersionVariance implements UnaryOperator<Version> {

    /**
     * No modification occured.
     */
    NONE {

        /**
         * @see net.yetamine.pet4bnd.version.VersionVariance#apply(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public Version apply(Version version) {
            return Objects.requireNonNull(version);
        }
    },

    /**
     * A change that affects implementation details, but no client or
     * implementation of an interface (increments the micro version).
     */
    MICRO {

        /**
         * @see net.yetamine.pet4bnd.version.VersionVariance#apply(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public Version apply(Version version) {
            return version.micro(version.micro() + 1);
        }
    },

    /**
     * A change that affects an interface in the way that does not break its
     * clients, but requires possibly changes of implementations of the
     * interface (increments the minor version).
     */
    MINOR {

        /**
         * @see net.yetamine.pet4bnd.version.VersionVariance#apply(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public Version apply(Version version) {
            return new Version(version.major(), version.minor() + 1, 0, version.qualifier());
        }
    },

    /**
     * A breaking change of the interface, requiring the change of the major
     * version.
     */
    MAJOR {

        /**
         * @see net.yetamine.pet4bnd.version.VersionVariance#apply(net.yetamine.pet4bnd.version.Version)
         */
        @Override
        public Version apply(Version version) {
            return new Version(version.major() + 1, 0, 0, version.qualifier());
        }
    };

    /**
     * Returns the adjusted version.
     *
     * @param version
     *            the version to adjust. It must not be {@code null}.
     *
     * @return the adjusted version
     *
     * @see java.util.function.Function#apply(java.lang.Object)
     */
    public abstract Version apply(Version version);
}
