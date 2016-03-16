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

import net.yetamine.pet4bnd.model.VersionGroup;

/**
 * Represents a version group for packages.
 */
public final class PackageGroupDefinition extends VersionDefinition implements VersionGroup {

    /** Inheritance source. */
    private final String identifier;

    /**
     * Creates a new instance.
     *
     * @param name
     *            the name of the group. It must not be {@code null}.
     */
    public PackageGroupDefinition(String name) {
        identifier = Objects.requireNonNull(name);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionGroup#identifier()
     */
    public String identifier() {
        return identifier;
    }
}
