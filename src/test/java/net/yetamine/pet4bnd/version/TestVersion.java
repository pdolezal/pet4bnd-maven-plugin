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
 * Tests {@link Version}.
 */
public final class TestVersion {

    /**
     * Tests {@link Version#ZERO}.
     */
    @Test
    public void testZero() {
        Assert.assertEquals(Version.ZERO.major(), 0);
        Assert.assertEquals(Version.ZERO.minor(), 0);
        Assert.assertEquals(Version.ZERO.micro(), 0);
        Assert.assertNull(Version.ZERO.qualifier());

        Assert.assertEquals(Version.ZERO, new Version(0, 0, 0));
    }

    /**
     * Tests construction.
     */
    @Test
    public void testConstruction() {
        final Version v1 = new Version(1, 2, 3);
        Assert.assertEquals(v1.major(), 1);
        Assert.assertEquals(v1.minor(), 2);
        Assert.assertEquals(v1.micro(), 3);
        Assert.assertNull(Version.ZERO.qualifier());

        final Version v2 = new Version(3, 2, 1, "QUALIFIER");
        Assert.assertEquals(v2.major(), 3);
        Assert.assertEquals(v2.minor(), 2);
        Assert.assertEquals(v2.micro(), 1);
        Assert.assertEquals(v2.qualifier(), "QUALIFIER");
    }

    /**
     * Tests wrong construction parameters.
     *
     * @param major
     *            the major version number
     * @param minor
     *            the minor version number
     * @param micro
     *            the micro version number
     */
    @Test(dataProvider = "constructionParamsFailure", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionParamsFailure(int major, int minor, int micro) {
        Assert.assertNotNull(new Version(major, minor, micro));
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "constructionParamsFailure")
    public static Object[][] constructionParamsFailure() {
        return new Object[][] { { -1, 0, 0 }, { 0, -1, 0 }, { 0, 0, -1 } };
    }

    /**
     * Tests wrong construction parameters.
     *
     * @param qualifier
     *            the qualifier
     */
    @Test(dataProvider = "constructionQualifierFailure", expectedExceptions = IllegalArgumentException.class)
    public void testConstructionQualifierFailure(String qualifier) {
        Assert.assertNotNull(new Version(0, 0, 0, qualifier));
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "constructionQualifierFailure")
    public static Object[][] constructionQualifierFailure() {
        return new Object[][] { { "" }, { "some space" } };
    }

    /**
     * Tests {@link Version#toString()}.
     *
     * @param version
     *            the version to test. It must not be {@code null}.
     * @param string
     *            the expected string
     */
    @Test(dataProvider = "strings")
    public void testToString(Version version, String string) {
        Assert.assertEquals(version.toString(), string);
    }

    /**
     * Tests {@link Version#valueOf(CharSequence)}.
     *
     * @param version
     *            the expected version. It must not be {@code null}.
     * @param string
     *            the string to parse. It must not be {@code null}.
     */
    @Test(dataProvider = "strings")
    public void testValueOf(Version version, String string) {
        Assert.assertEquals(Version.valueOf(string), version);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "strings")
    public static Object[][] strings() {
        return new Object[][] {
            // @formatter:off
            { Version.ZERO,                             "0.0.0"                     },
            { new Version(3, 2, 1),                     "3.2.1"                     },
            { new Version(3, 2, 1, null),               "3.2.1"                     },
            { new Version(3, 2, 1, "qualifier"),        "3.2.1.qualifier"           },
            { new Version(3, 2, 1, "q-W@2016/03/31.1"), "3.2.1.q-W@2016/03/31.1"    }
            // @formatter:on
        };
    }

    /**
     * Tests {@link Version#valueOf(CharSequence)}.
     *
     * @param version
     *            the expected version. It must not be {@code null}.
     * @param string
     *            the string to parse. It must not be {@code null}.
     */
    @Test(dataProvider = "parsingSpecials")
    public void testValueOfSpecials(Version version, String string) {
        Assert.assertEquals(Version.valueOf(string), version);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "parsingSpecials")
    public static Object[][] parsingSpecials() {
        return new Object[][] {
            // @formatter:off
            { Version.ZERO,                             "0"                         },
            { Version.ZERO,                             "0.0"                       },
            { new Version(3, 0, 0),                     "3"                         },
            { new Version(3, 2, 0, null),               "3.2"                       },
            { new Version(3, 2, 0, "qualifier"),        "3.2.qualifier"             },
            { new Version(3, 0, 0, "q-W@2016/03/31.1"), "3.q-W@2016/03/31.1"        }
            // @formatter:on
        };
    }

    /**
     * Tests wrong strings to parse.
     *
     * @param string
     *            the string to parse. It must not be {@code null}.
     */
    @Test(dataProvider = "parsingFailure", expectedExceptions = IllegalArgumentException.class)
    public void testParsingFailure(String string) {
        Version.valueOf(string);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "parsingFailure")
    public static Object[][] parsingFailure() {
        return new Object[][] { { "a" }, { "a.q" }, { "0.q q" }, { "0. q" }, { "0.q " } };
    }

    /**
     * Tests {@link Version#valueOf(CharSequence)}.
     *
     * @param a
     *            the instance to invoke the value on. It must not be
     *            {@code null}.
     * @param b
     *            the instance to compare to. It must not be {@code null}.
     * @param result
     *            the result of the comparison the string to parse. It must not
     *            be {@code null}.
     */
    @Test(dataProvider = "comparing")
    public void testCompareTo(Version a, Version b, int result) {
        Assert.assertEquals(sgn(a.compareTo(b)), result);
        Assert.assertEquals(sgn(b.compareTo(a)), -result);

        if (result == 0) {
            Assert.assertEquals(a, b);
            Assert.assertEquals(a.hashCode(), b.hashCode());
        }
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "comparing")
    public static Object[][] comparing() {
        return new Object[][] {
            // @formatter:off
            { Version.ZERO,                             Version.ZERO,                   0  },
            { Version.ZERO,                             new Version(0, 0, 0, "q"),      -1 },
            { new Version(3, 2, 1),                     new Version(3, 2, 1),           0  },
            { new Version(3, 2, 1),                     new Version(3, 2, 1, "q"),      -1 },
            { new Version(3, 2, 1, "a"),                new Version(3, 2, 1, "q"),      -1 },
            { new Version(3, 2, 1, "q"),                new Version(3, 2, 1, "q"),      0  },
            { new Version(3, 2, 1, null),               new Version(3, 2, 1, "q"),      -1 },
            { new Version(1, 0, 0),                     new Version(1, 0, 1),           -1 },
            { new Version(1, 0, 0),                     new Version(1, 1, 0),           -1 },
            { new Version(1, 0, 0),                     new Version(2, 0, 0),           -1 },
            { new Version(3, 2, 0),                     new Version(3, 2, 1),           -1 }
            // @formatter:on
        };
    }

    /**
     * Computes signum of an integer value.
     *
     * @param value
     *            the value to process
     *
     * @return signum of an integer value
     */
    private static int sgn(int value) {
        return (value < 0) ? -1 : ((value > 0) ? 1 : 0);
    }
}
