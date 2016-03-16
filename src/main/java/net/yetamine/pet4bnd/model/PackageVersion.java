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

package net.yetamine.pet4bnd.model;

import java.util.Optional;

import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a package version statement.
 */
public interface PackageVersion extends VersionStatement {

    /**
     * Returns the version inheritance source if any.
     *
     * @return the version inheritance source, or an empty container if none
     */
    Optional<VersionStatement> inheritance();

    /**
     * Indicates if the version information inherits from another source.
     *
     * @return {@code true} if the version information inherits from another
     *         source
     */
    default boolean inheriting() {
        return inheritance().isPresent();
    }

    /**
     * Sets the version inheritance source.
     *
     * <p>
     * Setting the inheritance source makes the version resolution and baseline
     * dynamic, getting the values from the source. When setting no source, the
     * values are set the to the current values of the previous source. Setting
     * the baseline sets the inheritance source to none.
     *
     * @param source
     *            the source to inherit. It may be {@code null} to set no
     *            source.
     */
    void inherit(VersionStatement source);

    /**
     * Restores the baseline from the resolution.
     *
     * <p>
     * The default implementation sets the baseline and resets the variance if
     * not inheriting; otherwise relies on the source restoring, resetting the
     * the variance only.
     *
     * @see net.yetamine.pet4bnd.model.VersionStatement#restore()
     */
    default void restore() {
        if (inheriting()) {
            variance().ifPresent(v -> variance(VersionVariance.NONE));
            return; // Do not override the baseline to prevent losing the inheritance
        }

        VersionStatement.super.restore();
    }
}
