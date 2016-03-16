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

/**
 * Represents a package export directive.
 */
public interface PackageExport {

    /**
     * Returns the name of the exported package.
     *
     * @return the name of the exported package
     */
    String packageName();

    /**
     * Returns the export version.
     *
     * @return the export version
     */
    PackageVersion version();

    /**
     * Returns the attributes of the package export.
     *
     * @return the attributes of the package export
     */
    Optional<String> attributes();

    /**
     * Restores the baselines from the resolution.
     */
    default void restore() {
        version().restore();
    }
}
