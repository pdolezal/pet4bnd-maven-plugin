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

package net.yetamine.pet4bnd.support;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents a testing resource.
 */
public final class Resource {

    /** Name of the resource. */
    private final String name;

    /**
     * Creates a new instance.
     *
     * @param resource
     *            the resource to represent. It must not be {@code null}.
     */
    public Resource(String resource) {
        name = Objects.requireNonNull(resource);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Resource[%s]", name);
    }

    /**
     * Returns the name of this resource.
     *
     * @return the name of this resource
     */
    public String name() {
        return name;
    }

    /**
     * Opens the resource as a stream.
     *
     * @return the stream for the resource
     *
     * @throws IOException
     *             if the resource is missing
     */
    public InputStream inputStream() throws IOException {
        final InputStream result = Resource.class.getResourceAsStream(name);

        if (result == null) {
            throw new IOException(String.format("Missing resource '%s'.", name));
        }

        return result;
    }

    /**
     * Opens the resource as a reader.
     *
     * @param charset
     *            the charset of the resource. It must not be {@code null}.
     *
     * @return the reader for the resource
     *
     * @throws IOException
     *             if the resource is missing
     */
    public BufferedReader bufferedReader(Charset charset) throws IOException {
        Objects.requireNonNull(charset); // Evaluate before creating any stream
        return new BufferedReader(new InputStreamReader(inputStream(), charset));
    }

    /**
     * Provides the resource as a byte array.
     *
     * @return the bytes of the resource, or {@code null} if the resource is
     *         missing
     *
     * @throws IOException
     *             if the resource is missing
     */
    public byte[] toBytes() throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();

        final byte[] buffer = new byte[1024];
        try (InputStream is = Resource.class.getResourceAsStream(name)) {
            if (is == null) {
                return null;
            }

            for (int length; (length = is.read(buffer)) != -1;) {
                result.write(buffer, 0, length);
            }
        }

        return result.toByteArray();
    }

    /**
     * Provides the resource as a {@link Properties} instance.
     *
     * @return the properties, or {@code null} if the resource is missing
     *
     * @throws IOException
     *             if the input could not be read
     */
    public Properties toProperties() throws IOException {
        final Properties result; // Keep this variable here to prevent FindBugs being confused
        try (InputStream is = Resource.class.getResourceAsStream(name)) {
            if (is == null) {
                return null;
            }

            result = new Properties();
            result.load(is);
        }

        return result;
    }
}
