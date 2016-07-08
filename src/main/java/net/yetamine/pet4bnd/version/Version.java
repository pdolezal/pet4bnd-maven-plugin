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

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version.
 */
public final class Version implements Serializable, Comparable<Version> {

    /** Serializable version: 1 */
    private static final long serialVersionUID = 1L;

    /** Representation of version <i>0.0.0</i>. */
    public static final Version ZERO = new Version(0, 0, 0);

    // @formatter:off
    /** Pattern for parsing a version string. */
    private static final Pattern PATTERN
    = Pattern.compile("(?<major>\\d+)(\\.(?<minor>\\d+))?(\\.(?<micro>\\d+))?(\\.(?<qualifier>\\S+))?");
    // @formatter:on

    /** Major version number. */
    private final int major;
    /** Minor version number. */
    private final int minor;
    /** Micro version number. */
    private final int micro;
    /** Optional qualifier. */
    private final String qualifier;

    /**
     * Creates a new instance.
     *
     * @param maj
     *            the major version number. It must not be negative.
     * @param min
     *            the minor version number. It must not be negative.
     * @param mic
     *            the micro version number. It must not be negative.
     * @param qual
     *            the qualifier. It may be {@code null} (when missing), but not
     *            empty if present.
     */
    public Version(int maj, int min, int mic, String qual) {
        major = check(maj, "Major version must not negative.");
        minor = check(min, "Minor version must not negative.");
        micro = check(mic, "Micro version must not negative.");

        if (qual != null) {
            if (qual.isEmpty()) {
                throw new IllegalArgumentException("Qualifier may be missing, but not empty.");
            }

            if (qual.chars().anyMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Qualifier may not contain whitespace characters.");
            }
        }

        qualifier = qual;
    }

    /**
     * Creates a new instance.
     *
     * @param maj
     *            the major version number. It must not be negative.
     * @param min
     *            the minor version number. It must not be negative.
     * @param mic
     *            the micro version number. It must not be negative.
     */
    public Version(int maj, int min, int mic) {
        this(maj, min, mic, null);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(major).append('.').append(minor).append('.').append(micro);
        if (qualifier != null) { // Qualifier present
            result.append('.').append(qualifier);
        }

        return result.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Version) {
            final Version o = (Version) obj;
            return (major == o.major) && (minor == o.minor) && (micro == o.micro)
                    && Objects.equals(qualifier, o.qualifier);
        }

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, micro, qualifier);
    }

    /**
     * The ordering of versions respect the comparison of the major, minor and
     * micro version numbers in this order. A version without any qualifier is
     * lesser than a version with a qualifier and qualifiers (if present) use
     * the lexicographic ordering.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Version o) {
        int result = Integer.compare(major, o.major);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(minor, o.minor);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(micro, o.micro);
        if (result != 0) {
            return result;
        }

        if (qualifier == null) { // This can't be greater anymore
            return (o.qualifier == null) ? 0 : -1;
        }

        // This can't be less anymore, but both may be yet equal if qualifiers are equal
        return (o.qualifier == null) ? 1 : qualifier.compareTo(o.qualifier);
    }

    /**
     * Parses the version.
     *
     * @param value
     *            the value to parse. It must not be {@code null}.
     *
     * @return the version representation
     */
    public static Version valueOf(CharSequence value) {
        final Matcher matcher = PATTERN.matcher(value);

        if (matcher.matches()) {
            return from(matcher);
        }

        throw new IllegalArgumentException("Not a valid version: " + value);
    }

    /**
     * Returns the major version number.
     *
     * @return the major version number
     */
    public int major() {
        return major;
    }

    /**
     * Returns the minor version number.
     *
     * @return the minor version number
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns the micro version number.
     *
     * @return the micro version number
     */
    public int micro() {
        return micro;
    }

    /**
     * Returns the qualifier.
     *
     * @return the qualifier, or {@code null} if missing
     */
    public String qualifier() {
        return qualifier;
    }

    /**
     * Returns a version with different major version number.
     *
     * @param value
     *            the value to change. It must not be negative.
     *
     * @return the altered version
     */
    public Version major(int value) {
        return new Version(value, minor, micro, qualifier);
    }

    /**
     * Returns a version with different minor version number.
     *
     * @param value
     *            the value to change. It must not be negative.
     *
     * @return the altered version
     */
    public Version minor(int value) {
        return new Version(major, value, micro, qualifier);
    }

    /**
     * Returns a version with different micro version number.
     *
     * @param value
     *            the value to change. It must not be negative.
     *
     * @return the altered version
     */
    public Version micro(int value) {
        return new Version(major, minor, value, qualifier);
    }

    /**
     * Returns a version with different qualifier.
     *
     * @param value
     *            the value to change. It may be {@code null}, but not empty.
     *
     * @return the altered version
     */
    public Version qualifier(String value) {
        return new Version(major, minor, micro, value);
    }

    /**
     * Constructs the version from a filled {@link Matcher} instance.
     *
     * @param matcher
     *            the matcher to use. It must not be {@code null}.
     *
     * @return the version representation
     */
    private static Version from(Matcher matcher) {
        final int major = Integer.parseInt(matcher.group("major"));
        final String minor = matcher.group("minor");
        final String micro = matcher.group("micro");
        final String qualifier = matcher.group("qualifier");

        return new Version(major, convert(minor), convert(micro), qualifier);
    }

    /**
     * Checks the version number.
     *
     * @param version
     *            the version number to check
     * @param message
     *            the error message if the version number is negative
     *
     * @return the version
     *
     * @throws IllegalArgumentException
     *             if the version number is negative
     */
    private static int check(int version, String message) {
        if (0 <= version) {
            return version;
        }

        throw new IllegalArgumentException(message);
    }

    /**
     * Converts a string representation of a version number.
     *
     * @param version
     *            the version to convert. If {@code null}, zero is returned.
     *
     * @return the version number
     */
    private static int convert(String version) {
        return (version != null) ? Integer.parseInt(version) : 0;
    }
}
