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

package net.yetamine.pet4bnd.mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.yetamine.pet4bnd.testing.TestOutput;
import net.yetamine.pet4bnd.testing.TestResources;

/**
 * Tests {@link PomVersionEditor}.
 */
public final class TestPomVersionEditor {

    /**
     * Performs a test.
     *
     * @param resourceSet
     *            the name of the resource set to test. It must not be
     *            {@code null}.
     * @param expectedVersion
     *            expected version
     * @param desiredVersion
     *            version to set
     *
     * @throws Exception
     *             if something goes very wrong
     */
    @Test(dataProvider = "resources")
    public void test(String resourceSet, String expectedVersion, String desiredVersion) throws Exception {
        final PomVersionEditor editor;
        try (InputStream is = TestResources.openInputStream(resourceSet + ".xml")) {
            editor = new PomVersionEditor(is);
        }

        Assert.assertEquals(editor.version(), expectedVersion);

        editor.version(desiredVersion);
        final byte[] result = TestOutput.toBytes(editor);
        final byte[] expect = TestResources.loadBytes(resourceSet + ".xml+change");
        Assert.assertEquals(result, expect);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "resources")
    public static Object[][] resources() throws IOException {
        final List<Object[]> result = new ArrayList<>();

        final Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = TestResources.openBufferedReader("/test-mojo/poms", charset)) {
            for (String line; (line = reader.readLine()) != null;) {
                final String[] items = line.split("\\s*,\\s*");

                final Object[] params = {               // @formatter:break
                        "/test-mojo/" + items[0],       // Resource set name
                        items[1],                       // Version to expect
                        items[2]                        // Version to set
                };

                result.add(params);
            }
        }

        return result.toArray(new Object[result.size()][]);
    }
}
