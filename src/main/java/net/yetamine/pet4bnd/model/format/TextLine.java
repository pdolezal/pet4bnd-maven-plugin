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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides a line representation which consists of multiple fragments.
 */
final class TextLine implements TextFragment {

    /** Fragments of this line. */
    private final List<TextFragment> fragments = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public TextLine() {
        // Default constructor
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        switch (fragments.size()) {
            // @formatter:off
            case 0 : return "";
            case 1 : return TextFragment.toString(fragments.get(0));
            // @formatter:on
        }

        return fragments.stream().map(f -> TextFragment.toString(f)).collect(Collectors.joining());
    }

    /**
     * @see net.yetamine.pet4bnd.model.format.TextFragment#format()
     */
    public String format() {
        return toString();
    }

    /**
     * Appends a fragment.
     *
     * @param fragment
     *            the fragment to append. It must not be {@code null}.
     *
     * @return this instance
     */
    public TextLine append(TextFragment fragment) {
        fragments.add(fragment);
        return this;
    }

    /**
     * Appends a fragment.
     *
     * @param fragment
     *            the fragment to append. It must not be {@code null}.
     *
     * @return this instance
     */
    public TextLine append(String fragment) {
        Objects.requireNonNull(fragment);
        return append(() -> fragment);
    }

    /**
     * Provides the list of fragments of this line.
     *
     * <p>
     * The provided list be modified to change the line representation, however,
     * {@code null} values are not welcome and might be rejected.
     *
     * @return the list of fragments of this line
     */
    public List<TextFragment> fragments() {
        return fragments;
    }
}
