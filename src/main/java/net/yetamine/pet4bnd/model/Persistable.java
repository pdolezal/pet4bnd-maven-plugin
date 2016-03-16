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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A persistable object.
 */
public interface Persistable {

    /**
     * Stores the encapsulated object in the given sink.
     *
     * @param sink
     *            the sink to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    void persist(OutputStream sink) throws IOException;

    /**
     * Stores the encapsulated object in the given file.
     *
     * @param path
     *            the path to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    default void store(Path path) throws IOException {
        try (OutputStream sink = Files.newOutputStream(path)) {
            persist(sink);
        }
    }
}
