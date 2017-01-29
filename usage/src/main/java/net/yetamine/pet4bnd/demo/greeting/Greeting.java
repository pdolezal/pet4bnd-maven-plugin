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

package net.yetamine.pet4bnd.demo.greeting;

/**
 * A greeting facility.
 */
public final class Greeting {

    /**
     * Prevents creating instances of this class.
     */
    private Greeting() {
        throw new AssertionError();
    }

    /**
     * Returns a greeting from a list of arguments.
     *
     * @param args
     *            the arguments to use. It must not be {@code null}.
     *
     * @return the greeting
     */
    public static String from(String... args) {
        final StringBuilder result = new StringBuilder();
        for (int i = args.length; i-- > 0;) {
            result.append(args[i]);
        }

        return result.toString();
    }
}
