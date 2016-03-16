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

import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.PackageVersion;

/**
 * Represents a package export directive.
 */
public final class PackageExportDefinition implements PackageExport {

    /** Name of this package. */
    private final String packageName;
    /** Export version definition. */
    private final PackageVersion version;
    /** Attributes of the export directive. */
    private final Optional<String> attributes;

    /**
     * Creates a new instance.
     *
     * @param exportIdentifier
     *            the package name. It must not be {@code null}.
     * @param versionDefinition
     *            the definition to use. It must not be {@code null}.
     * @param exportAttributes
     *            the attributes
     */
    public PackageExportDefinition(String exportIdentifier, PackageVersion versionDefinition, String exportAttributes) {
        packageName = Objects.requireNonNull(exportIdentifier);
        version = Objects.requireNonNull(versionDefinition);
        attributes = Optional.ofNullable(exportAttributes);
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

    /**
     * @see net.yetamine.pet4bnd.model.PackageExport#version()
     */
    public PackageVersion version() {
        return version;
    }
}
