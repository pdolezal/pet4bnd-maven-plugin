/*
 * Copyright 2016 Yetamine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
