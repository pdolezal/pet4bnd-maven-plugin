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

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Represents a version statement.
 */
public interface VersionStatement {

    /**
     * Tests if the given version is valid with the respect to the constraint.
     *
     * @param version
     *            the version to test. It must not be {@code null}.
     *
     * @return {@code true} if no constraint exists or the given version
     *         satisfies the constraint
     */
    default boolean test(Version version) {
        return constraint().map(c -> version.compareTo(c) < 0).orElse(Boolean.TRUE);
    }

    /**
     * Tests if the version resolution is valid with the respect to the
     * constraint.
     *
     * @return {@code true} if no constraint exists or the version resolution
     *         satisfies the constrain
     */
    default boolean test() {
        return test(resolution());
    }

    /**
     * Restores the baseline from the resolution.
     *
     * <p>
     * The default implementation sets the baseline and resets the variance.
     */
    default void restore() {
        baseline(resolution());
        variance().ifPresent(v -> variance(VersionVariance.NONE));
    }

    /**
     * Sets the version resolution.
     *
     * @param value
     *            the value to set. It may be {@code null} to enable the default
     *            resolution algorithm.
     */
    void resolve(Version value);

    /**
     * Returns the version resolution.
     *
     * <p>
     * Until overridden, the method shall use the default resolution algorithm,
     * which derives the result from the other properties.
     *
     * @return the version resolution
     */
    Version resolution();

    /**
     * Returns the version baseline.
     *
     * @return the version baseline
     */
    Version baseline();

    /**
     * Sets the version baseline.
     *
     * @param value
     *            the version baseline to set. It must not be {@code null}.
     */
    void baseline(Version value);

    /**
     * Returns the version constraint.
     *
     * @return the version constraint, or an empty container if no constraint
     *         exists
     */
    Optional<Version> constraint();

    /**
     * Sets the version constraint.
     *
     * @param value
     *            the version constraint to set. It may be {@code null} if no
     *            constraint shall exist.
     */
    void constraint(Version value);

    /**
     * Returns the version baseline variance.
     *
     * @return the version variance, or an empty container if no variance given
     *         and the version resolution should be equal to the baseline always
     */
    Optional<VersionVariance> variance();

    /**
     * Sets the version baseline variance.
     *
     * @param value
     *            the version baseline variance to set. It may be {@code null}
     *            for disabling the variance influence (i.e., fix the baseline)
     */
    void variance(VersionVariance value);
}
