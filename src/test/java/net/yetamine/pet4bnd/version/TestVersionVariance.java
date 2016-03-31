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
 * Tests {@link VersionVariance}.
 */
public final class TestVersionVariance {

    /**
     * Tests {@link VersionVariance#apply(Version)}.
     *
     * @param version
     *            the version to change. It must not be {@code null}.
     * @param variance
     *            the variance to apply. It must not be {@code null}.
     * @param expected
     *            the expected value. It must not be {@code null}.
     */
    @Test(dataProvider = "versions")
    public void test(Version version, VersionVariance variance, Version expected) {
        Assert.assertEquals(variance.apply(version), expected);
    }

    @SuppressWarnings("javadoc")
    @DataProvider(name = "versions")
    public static Object[][] versions() {
        return new Object[][] {
            // @formatter:off
            { Version.ZERO,                 VersionVariance.MAJOR,  new Version(1, 0, 0)        },

            { new Version(1, 0, 0, "q"),    VersionVariance.MAJOR,  new Version(2, 0, 0, "q")   },
            { new Version(1, 2, 0, "q"),    VersionVariance.MAJOR,  new Version(2, 0, 0, "q")   },
            { new Version(1, 0, 3, "q"),    VersionVariance.MAJOR,  new Version(2, 0, 0, "q")   },
            { new Version(1, 2, 3, "q"),    VersionVariance.MAJOR,  new Version(2, 0, 0, "q")   },
            { new Version(1, 2, 3),         VersionVariance.MAJOR,  new Version(2, 0, 0)        },

            { new Version(1, 0, 0, "q"),    VersionVariance.MINOR,  new Version(1, 1, 0, "q")   },
            { new Version(1, 2, 0, "q"),    VersionVariance.MINOR,  new Version(1, 3, 0, "q")   },
            { new Version(1, 0, 3, "q"),    VersionVariance.MINOR,  new Version(1, 1, 0, "q")   },
            { new Version(1, 2, 3, "q"),    VersionVariance.MINOR,  new Version(1, 3, 0, "q")   },
            { new Version(1, 2, 3),         VersionVariance.MINOR,  new Version(1, 3, 0)        },

            { new Version(1, 0, 0, "q"),    VersionVariance.MICRO,  new Version(1, 0, 1, "q")   },
            { new Version(1, 2, 0, "q"),    VersionVariance.MICRO,  new Version(1, 2, 1, "q")   },
            { new Version(1, 0, 3, "q"),    VersionVariance.MICRO,  new Version(1, 0, 4, "q")   },
            { new Version(1, 2, 3, "q"),    VersionVariance.MICRO,  new Version(1, 2, 4, "q")   },
            { new Version(1, 2, 3),         VersionVariance.MICRO,  new Version(1, 2, 4)        },

            { new Version(1, 0, 0, "q"),    VersionVariance.NONE,   new Version(1, 0, 0, "q")   },
            { new Version(1, 2, 0, "q"),    VersionVariance.NONE,   new Version(1, 2, 0, "q")   },
            { new Version(1, 0, 3, "q"),    VersionVariance.NONE,   new Version(1, 0, 3, "q")   },
            { new Version(1, 2, 3, "q"),    VersionVariance.NONE,   new Version(1, 2, 3, "q")   },
            { new Version(1, 2, 3),         VersionVariance.NONE,   new Version(1, 2, 3)        }
            // @formatter:on
        };
    }
}
