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

import java.util.Optional;

import net.yetamine.pet4bnd.model.PackageVersion;
import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;

/**
 * Represents a definition statement for a package.
 */
public final class PackageVersionDefinition extends VersionDefinition implements PackageVersion {

    /** Inheritance source. */
    private VersionStatement inheritance;

    /**
     * Creates a new instance.
     */
    public PackageVersionDefinition() {
        // Default constructor
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#baseline()
     */
    @Override
    public Version baseline() {
        final Version result = super.baseline();
        return (result != null) ? result : inheritance.baseline();
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#baseline(net.yetamine.pet4bnd.version.Version)
     */
    @Override
    public void baseline(Version value) {
        super.baseline(value);
        inheritance = null;
    }

    /**
     * @see net.yetamine.pet4bnd.model.PackageVersion#inheritance()
     */
    public Optional<VersionStatement> inheritance() {
        return Optional.ofNullable(inheritance);
    }

    /**
     * @see net.yetamine.pet4bnd.model.PackageVersion#inherit(net.yetamine.pet4bnd.model.VersionStatement)
     */
    public void inherit(VersionStatement source) {
        if (source != null) {
            inheritance = source;
            return;
        }

        if (inheritance != null) {
            super.resolve(inheritance.resolution());
            super.baseline(inheritance.baseline());
            inheritance = null;
        }
    }

    /**
     * @see net.yetamine.pet4bnd.model.support.VersionDefinition#resolve()
     */
    @Override
    protected Version resolve() {
        return (inheritance != null) ? inheritance.resolution() : super.resolve();
    }
}
