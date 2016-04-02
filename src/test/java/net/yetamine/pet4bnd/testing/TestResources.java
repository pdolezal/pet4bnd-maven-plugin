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

package net.yetamine.pet4bnd.testing;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;

/**
 * Support utilities for testing.
 */
public final class TestResources {

    /**
     * Opens a resource as a stream.
     *
     * @param resourceName
     *            the name of the resource
     *
     * @return the stream for the resource
     *
     * @throws IOException
     *             if the resource is missing
     */
    public static InputStream openInputStream(String resourceName) throws IOException {
        final InputStream result = TestResources.class.getResourceAsStream(resourceName);

        if (result == null) {
            throw new IOException(String.format("Missing resource '%s'.", resourceName));
        }

        return result;
    }

    /**
     * Opens a resource as a reader.
     *
     * @param resourceName
     *            the name of the resource
     * @param charset
     *            the charset of the resource. It must not be {@code null}.
     *
     * @return the reader for the resource
     *
     * @throws IOException
     *             if the resource is missing
     */
    public static BufferedReader openBufferedReader(String resourceName, Charset charset) throws IOException {
        Objects.requireNonNull(charset);
        return new BufferedReader(new InputStreamReader(openInputStream(resourceName), charset));
    }

    /**
     * Loads a resource as a byte array.
     *
     * @param resourceName
     *            the name of the resource
     *
     * @return the bytes of the resource, or {@code null} if the resource is
     *         missing
     *
     * @throws IOException
     *             if the resource is missing
     */
    public static byte[] loadBytes(String resourceName) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();

        final byte[] buffer = new byte[1024];
        try (InputStream is = TestResources.class.getResourceAsStream(resourceName)) {
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
     * Loads a resource as a {@link Properties} instance.
     *
     * @param resourceName
     *            the name of the resource
     *
     * @return the properties, or {@code null} if the resource is missing
     *
     * @throws IOException
     *             if the input could not be read
     */
    public static Properties loadProperties(String resourceName) throws IOException {
        try (InputStream is = TestResources.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }

            final Properties result = new Properties();
            result.load(is);
            return result;
        }
    }

    private TestResources() {
        throw new AssertionError();
    }
}
