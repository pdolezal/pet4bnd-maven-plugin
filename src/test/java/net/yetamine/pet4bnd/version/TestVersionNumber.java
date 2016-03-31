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

package net.yetamine.pet4bnd.version;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests {@link VersionNumber}.
 */
public final class TestVersionNumber {

    private static final Version VERSION = new Version(1, 2, 3);

    /**
     * Tests both get and set functionality.
     *
     * @param number
     *            the number instance to test. It must not be {@code null}.
     * @param value
     *            the expected value
     * @param doubleCheck
     *            the instance for double checking
     */
    @Test(dataProvider = "numbers")
    public void test(VersionNumber number, int value, Version doubleCheck) {
        Assert.assertEquals(number.get(VERSION), value);

        final int increment = value + 1;
        final Version modified = number.set(VERSION, increment);
        Assert.assertEquals(number.get(modified), increment);
        Assert.assertEquals(modified, doubleCheck);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "numbers")
    public static Object[][] numbers() {
        return new Object[][] {
            // @formatter:off
            { VersionNumber.MAJOR, VERSION.major(), VERSION.major(VERSION.major() + 1) },
            { VersionNumber.MINOR, VERSION.minor(), VERSION.minor(VERSION.minor() + 1) },
            { VersionNumber.MICRO, VERSION.micro(), VERSION.micro(VERSION.micro() + 1) }
            // @formatter:on
        };
    }
}
