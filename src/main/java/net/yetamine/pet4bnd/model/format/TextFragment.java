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

package net.yetamine.pet4bnd.model.format;

/**
 * Represents a formattable text fragment.
 */
interface TextFragment {

    /**
     * Formats the content of this instance to a string value.
     *
     * @return the formatted content, or {@code null} if no formatting possible
     *         or the result shall be omitted
     */
    String format();

    /**
     * Formats the content of the given value to a string value.
     *
     * @param value
     *            the value to format
     *
     * @return the result of the formatting, an empty string if the value is
     *         {@code null}
     */
    static String toString(TextFragment value) {
        if (value != null) {
            final String result = value.format();
            if (result != null) {
                return result;
            }
        }

        return "";
    }
}
