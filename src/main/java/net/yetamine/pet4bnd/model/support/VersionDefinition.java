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

package net.yetamine.pet4bnd.model.support;

import java.util.Objects;
import java.util.Optional;

import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Provides a base for implementing {@link VersionStatement}.
 */
public abstract class VersionDefinition implements VersionStatement {

    /** Current resolution. */
    private Version resolution;
    /** Version baseline. */
    private Version baseline = Version.ZERO;
    /** Version constraint (default is none). */
    private Optional<Version> constraint = Optional.empty();
    /** Version variance (default is unspecified, i.e., fixed baseline). */
    private Optional<VersionVariance> variance = Optional.empty();

    /**
     * Prepares a new instance.
     */
    protected VersionDefinition() {
        // Default constructor
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(baseline().toString());
        constraint().ifPresent(o -> result.append(" < ").append(o));
        variance().ifPresent(o -> result.append(" @ ").append(o));
        return result.append(" # ").append(resolution()).toString();
    }

    /**
     * Returns the resolution.
     *
     * @see net.yetamine.pet4bnd.model.VersionStatement#resolution()
     */
    public final Version resolution() {
        if (resolution != null) {
            return resolution;
        }

        final Version result = resolve();
        assert (result != null);
        return result;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#resolve(net.yetamine.pet4bnd.version.Version)
     */
    public void resolve(Version value) {
        resolution = value;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline()
     */
    public Version baseline() {
        return baseline;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#baseline(net.yetamine.pet4bnd.version.Version)
     */
    public void baseline(Version value) {
        baseline = Objects.requireNonNull(value);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint()
     */
    public Optional<Version> constraint() {
        return constraint;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#constraint(net.yetamine.pet4bnd.version.Version)
     */
    public void constraint(Version value) {
        constraint = Optional.ofNullable(value);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance()
     */
    public Optional<VersionVariance> variance() {
        return variance;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#variance(net.yetamine.pet4bnd.version.VersionVariance)
     */
    public void variance(VersionVariance value) {
        variance = Optional.ofNullable(value);
    }

    /**
     * Returns the resolution dynamically when no override given.
     *
     * <p>
     * This method is invoked by {@link #resolution()} when no override is given
     * and the result of this method is returned then, which implies that is may
     * not return {@code null}.
     *
     * <p>
     * The default implementation applies {@link #variance()} (if present, or
     * {@link VersionVariance#NONE} if absent) to {@link #baseline()}, so the
     * result follows the usual resolution algorithm.
     *
     * @return the resolution
     */
    protected Version resolve() {
        final Version version = baseline();
        return variance().map(v -> v.apply(version)).orElse(version);
    }
}
