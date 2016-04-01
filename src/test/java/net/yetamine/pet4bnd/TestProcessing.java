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

package net.yetamine.pet4bnd;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.yetamine.pet4bnd.format.Format2Bnd;
import net.yetamine.pet4bnd.format.Format2Map;
import net.yetamine.pet4bnd.model.Persistable;
import net.yetamine.pet4bnd.model.VersionResolver;
import net.yetamine.pet4bnd.model.format.PetFormat;
import net.yetamine.pet4bnd.model.format.PetParser;

/**
 * Tests main parts of the tool using a set of input and output files.
 */
public final class TestProcessing {

    /**
     * Performs a test.
     *
     * @param resourceSet
     *            the name of the resource set to test. It must not be
     *            {@code null}.
     * @param resolvable
     *            expected result of the version resolver
     * @param errors
     *            expected parsing error count
     * @param warnings
     *            expected parsing warning count
     *
     * @throws Exception
     *             if something goes very wrong
     */
    @Test(dataProvider = "resources")
    public void test(String resourceSet, boolean resolvable, int errors, int warnings) throws Exception {
        // Parse the input from the resource set
        final PetParser parser = parse(resourceSet);
        Assert.assertEquals(parser.errorCount(), errors);
        Assert.assertEquals(parser.warningCount(), warnings);

        // Resolve versions
        final PetFormat pet = parser.result().get();
        Assert.assertEquals(new VersionResolver(pet).resolve().test(), resolvable);

        // Compare the bnd output
        final byte[] bnd = loadBytes(resourceSet + ".bnd");
        if (bnd != null) { // There is some output expected
            final Format2Bnd format2bnd = new Format2Bnd(pet, true).timestamp(null);
            final byte[] persisted = persist(format2bnd);
            Assert.assertEquals(persisted, bnd);
        }

        // Compare the properties output
        final Properties properties = loadProperties(resourceSet + ".properties");
        if (properties != null) { // There is some output expected
            Assert.assertEquals(new Format2Map(pet).toProperties(), properties);
        }

        // Test restoring the versions
        final byte[] restore = loadBytes(resourceSet + ".pet+restore");
        if (restore != null) { // There is some output expected
            pet.restore();
            final byte[] persisted = persist(pet);
            Assert.assertEquals(persisted, restore);
        }
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "resources")
    public static Object[][] resources() throws IOException {
        final List<Object[]> result = new ArrayList<>();

        final Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(open("/running/test-cases"), charset))) {
            for (String line; (line = reader.readLine()) != null;) {
                final String[] items = line.split("\\s*,\\s*");

                final Object[] params = {
                        "/running/" + items[0],         // Resource set name
                        Boolean.valueOf(items[1]),      // Resolvable
                        Integer.valueOf(items[2]),      // Error count
                        Integer.valueOf(items[3])       // Warning count
                    };

                result.add(params);
            }
        }

        return result.toArray(new Object[result.size()][]);
    }

    // Helper methods

    /**
     * Loads properties from the resource.
     *
     * @param resourceName
     *            the name of the resource
     *
     * @return the properties, or {@code null} if the resource is missing
     *
     * @throws IOException
     *             if the input could not be read
     */
    private static Properties loadProperties(String resourceName) throws IOException {
        try (InputStream is = TestProcessing.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }

            final Properties result = new Properties();
            result.load(is);
            return result;
        }
    }

    /**
     * Persists an object to a byte array.
     *
     * @param persistable
     *            the object to persist. It must not be {@code null}.
     *
     * @return the persisted data
     *
     * @throws IOException
     *             if the output could not be written
     */
    private static byte[] persist(Persistable persistable) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        persistable.persist(result);
        return result.toByteArray();
    }

    /**
     * Parses the pet4bnd input file from the given resource set.
     *
     * @param resourceSet
     *            the resource set name. It must not be {@code null}.
     *
     * @return the parser with the data read
     *
     * @throws IOException
     *             if the input could not be read
     */
    private static PetParser parse(String resourceSet) throws IOException {
        assert (resourceSet != null);

        final PetParser result = new PetParser();
        final String resourceName = resourceSet + ".pet";
        final Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(open(resourceName), charset))) {
            for (String line; (line = reader.readLine()) != null;) {
                result.accept(line);
            }
        }

        return result.finish();
    }

    /**
     * Loads the bytes of the resource.
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
    private static byte[] loadBytes(String resourceName) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();

        final byte[] buffer = new byte[1024];
        try (InputStream is = TestProcessing.class.getResourceAsStream(resourceName)) {
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
     * Opens a stream for the resource.
     *
     * @param resourceName
     *            the name of the resource
     *
     * @return the stream for the resource
     *
     * @throws IOException
     *             if the resource is missing
     */
    private static InputStream open(String resourceName) throws IOException {
        final InputStream result = TestProcessing.class.getResourceAsStream(resourceName);

        if (result == null) {
            throw new IOException(String.format("Missing resource '%s'.", resourceName));
        }

        return result;
    }
}
