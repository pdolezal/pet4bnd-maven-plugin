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
import java.io.IOException;
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
import net.yetamine.pet4bnd.model.VersionResolver;
import net.yetamine.pet4bnd.model.format.PetFormat;
import net.yetamine.pet4bnd.model.format.PetParser;
import net.yetamine.pet4bnd.support.Resource;

/**
 * Tests main parts of the tool using a set of input and output files.
 */
public final class TestCore {

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
        final byte[] bnd = new Resource(resourceSet + ".bnd").toBytes();
        if (bnd != null) { // There is some output expected
            final Format2Bnd format2bnd = new Format2Bnd(pet, true).timestamp(null);
            final byte[] persisted = format2bnd.toBytes();
            Assert.assertEquals(persisted, bnd);
        }

        // Compare the properties output
        final Properties properties = new Resource(resourceSet + ".properties").toProperties();
        if (properties != null) { // There is some output expected
            Assert.assertEquals(new Format2Map(pet).toProperties(), properties);
        }

        // Test restoring the versions
        final byte[] restore = new Resource(resourceSet + ".pet+restore").toBytes();
        if (restore != null) { // There is some output expected
            pet.restore();
            final byte[] persisted = pet.toBytes();
            Assert.assertEquals(persisted, restore);
        }
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "resources")
    public static Object[][] resources() throws IOException {
        final List<Object[]> result = new ArrayList<>();

        final Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = new Resource("/test-core/test-cases").bufferedReader(charset)) {
            for (String line; (line = reader.readLine()) != null;) {
                final String[] items = line.split("\\s*,\\s*");

                final Object[] params = {               // @formatter:break
                        "/test-core/" + items[0],       // Resource set name
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
        try (BufferedReader reader = new Resource(resourceName).bufferedReader(charset)) {
            for (String line; (line = reader.readLine()) != null;) {
                result.accept(line);
            }
        }

        return result.finish();
    }
}
