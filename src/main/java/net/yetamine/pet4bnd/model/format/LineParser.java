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

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * A parser for a line processing.
 */
final class LineParser {

    /** Line being parsed. */
    private final CharSequence line;
    /** Line representation for reconstruction. */
    private final TextLine text;
    /** Current parsing position. */
    private int position;

    /**
     * Creates a new instance.
     *
     * @param source
     *            the source to parse. It must not be {@code null}.
     * @param startPosition
     *            the position to start with
     */
    public LineParser(CharSequence source, int startPosition) {
        position = Math.min(Math.max(0, startPosition), source.length());
        text = new TextLine();
        line = source;
    }

    /**
     * Creates a new instance.
     *
     * @param source
     *            the source to parse. It must not be {@code null}.
     */
    public LineParser(CharSequence source) {
        this(source, 0);
    }

    /**
     * Returns the source line.
     *
     * @return the source line
     */
    public CharSequence line() {
        return line;
    }

    /**
     * Returns the line text representation.
     *
     * @return the line text representation
     */
    public TextLine text() {
        return text;
    }

    // Failure handling

    /**
     * Returns a {@link ParseException} with the given message and cause at the
     * current parsing position.
     *
     * @param message
     *            the message to use
     * @param cause
     *            the cause exception if any
     *
     * @return the exception to throw
     */
    public ParseException failure(String message, Throwable cause) {
        final ParseException result = new ParseException(message, position);
        result.initCause(cause);
        return result;
    }

    /**
     * Returns a {@link ParseException} with the given message at the current
     * parsing position.
     *
     * @param message
     *            the message to use
     *
     * @return the exception to throw
     */
    public ParseException failure(String message) {
        return new ParseException(message, position);
    }

    // Parse attributes

    /** Pattern for an attribute specification. */
    private static final Pattern PATTERN_ATTRIBUTES // @formatter:break
            = Pattern.compile("\\s*\\+\\s*(?<value>.*?)\\s*$");

    /**
     * Parses an attribute specification.
     *
     * @return the attribute specification, or {@code null} if none
     */
    public String parseAttributes() {
        final Matcher matcher = PATTERN_ATTRIBUTES.matcher(line);

        if (parse(matcher)) {
            text.append(matcher.group());
            position = matcher.end();
            return matcher.group("value");
        }

        return null;
    }

    // Parse comments and blank lines

    /** Pattern for a non-significant line (comments and blank lines). */
    private static final Pattern PATTERN_IGNORABLE // @formatter:break
            = Pattern.compile("\\s*(#.*)?$");

    /**
     * Parses an ignorable fragment.
     *
     * @return {@code true} if parsing successful
     */
    public boolean parseIgnorable() {
        final Matcher matcher = PATTERN_IGNORABLE.matcher(line);

        if (parse(matcher)) {
            text.append(matcher.group());
            position = matcher.end();
            return true;
        }

        return false;
    }

    /**
     * Parses the trailing part of the input.
     *
     * @return {@code true} if the trailing part contains no unexpected
     *         characters
     */
    public boolean consumeTrailing() {
        // Check the trailing string (but do not store useless whitespace)
        final String trailing = line.subSequence(position, line.length()).toString();
        text.append(trailing); // Store trailing always
        return PATTERN_IGNORABLE.matcher(trailing).matches();
    }

    // Parse export declarations

    /** Pattern for finding the name of a definition. */
    private static final Pattern PATTERN_DECLARATION_EXPORT // @formatter:break
            = Pattern.compile("\\s*(?<value>[^\\s$:=<@#]+)\\s*:\\s*");

    /**
     * Parses an export declaration.
     *
     * @return the identifier of the package, or {@code null} if the parser does
     *         not stand at a valid declaration
     */
    public String parseExportDeclaration() {
        return parseConstant(PATTERN_DECLARATION_EXPORT, "value");
    }

    // Parse group declarations

    /** Pattern for finding the inheritance. */
    private static final Pattern PATTERN_DECLARATION_GROUP // @formatter:break
            = Pattern.compile("\\s*(?<value>\\$[^\\s$:=<@#]+)\\s*:\\s*");

    /**
     * Parses a group declaration.
     *
     * @return the identifier of the group, or {@code null} if the parser does
     *         not stand at a valid declaration
     */
    public String parseGroupDeclaration() {
        return parseConstant(PATTERN_DECLARATION_GROUP, "value");
    }

    // Parse group references

    /** Pattern for finding the group reference. */
    private static final Pattern PATTERN_DEFINITION_REFERENCE // @formatter:break
            = Pattern.compile("(?<value>\\$[^\\s$:=<@#]+)");

    /**
     * Parses a group reference.
     *
     * @param formatter
     *            the formatter of the representation placeholder. It must not
     *            be {@code null}.
     *
     * @return the identifier of the group, or {@code null} if the parser does
     *         not stand at a valid reference
     */
    public String parseGroupReference(TextFragment formatter) {
        Objects.requireNonNull(formatter);

        final Matcher matcher = PATTERN_DEFINITION_REFERENCE.matcher(line);

        if (parse(matcher)) {
            final String result = matcher.group("value");
            position = matcher.end();
            text.append(formatter);
            return result;
        }

        return null;
    }

    // Parse version baseline

    /** Pattern for finding the version baseline. */
    private static final Pattern PATTERN_DEFINITION_BASELINE = Pattern
            .compile("(?<value>\\d+(\\.\\d+)?(\\.\\d+)?(\\.[^\\s:=<@#]+)?)");

    /**
     * Parses the version baseline.
     *
     * @param formatter
     *            the formatter of the representation placeholder. It must not
     *            be {@code null}.
     *
     * @return the version baseline
     *
     * @throws ParseException
     *             if the parsing fails
     */
    public Version requireBaseline(TextFragment formatter) throws ParseException {
        Objects.requireNonNull(formatter);

        if (line.length() <= position) { // Match the version baseline
            throw failure("Missing version baseline.");
        }

        final Matcher matcher = PATTERN_DEFINITION_BASELINE.matcher(line);

        if (parse(matcher)) {
            final Version result = requireVersion(matcher.group("value"));
            position = matcher.end();
            text.append(formatter);
            return result;
        }

        throw failure("Version baseline invalid.");
    }

    // Parse constraints

    /** Pattern for finding the version constraint. */
    private static final Pattern PATTERN_DEFINITION_CONSTRAINT // @formatter:break
            = Pattern.compile("(?<prefix>\\s*<\\s*)(?<value>\\d+(\\.\\d+)?(\\.\\d+)?(\\.[^\\s:=<@#]+)?)");

    /**
     * Parses the version constraint.
     *
     * @param formatter
     *            the formatter of the representation placeholder. It must not
     *            be {@code null}.
     *
     * @return the version constraint, or {@code null} if none
     *
     * @throws ParseException
     *             if the parsing fails
     */
    public Version parseConstraint(TextFragment formatter) throws ParseException {
        Objects.requireNonNull(formatter);

        final Matcher matcher = PATTERN_DEFINITION_CONSTRAINT.matcher(line);

        if (parse(matcher)) {
            final Version result = requireVersion(matcher.group("value"));
            final String prefix = matcher.group("prefix");

            text.append(() -> {
                final String value = formatter.format();
                return (value != null) ? prefix + value : null;
            });

            position = matcher.end();
            return result;
        }

        text.append(() -> {
            final String value = formatter.format();
            return (value != null) ? " < " + value : null;
        });

        return null;
    }

    // Parse version variances

    /** Pattern for finding the version variance. */
    private static final Pattern PATTERN_DEFINITION_VARIANCE // @formatter:break
            = Pattern.compile("(?<prefix>\\s*@\\s*)(?<value>[A-Za-z]+)");

    /**
     * Parses the version variance.
     *
     * @param formatter
     *            the formatter of the representation placeholder. It must not
     *            be {@code null}.
     *
     * @return the version variance, or an empty container if none available
     *
     * @throws ParseException
     *             if the parsing fails
     */
    public VersionVariance parseVariance(TextFragment formatter) throws ParseException {
        Objects.requireNonNull(formatter);

        final Matcher matcher = PATTERN_DEFINITION_VARIANCE.matcher(line);

        if (parse(matcher)) {
            final VersionVariance result;
            try { // Parse the variance safely
                result = VersionVariance.valueOf(matcher.group("value").toUpperCase());
            } catch (IllegalArgumentException e) {
                throw failure(e.getMessage(), e);
            }

            final String prefix = matcher.group("prefix");

            text.append(() -> {
                final String value = formatter.format();
                return (value != null) ? prefix + value : null;
            });

            position = matcher.end();
            return result;
        }

        text.append(() -> {
            final String value = formatter.format();
            return (value != null) ? " @ " + value : null;
        });

        return null;
    }

    /**
     * Parses the input with the given matcher.
     *
     * @param matcher
     *            the matcher. It must not be {@code null}.
     *
     * @return {@code true} if the matcher finds the match at the current
     *         position
     */
    private boolean parse(Matcher matcher) {
        return matcher.find(position) && (matcher.start() == position);
    }

    /**
     * Parses a constant with the given pattern if possible.
     *
     * @param pattern
     *            the pattern to use. It must not be {@code null}.
     * @param value
     *            the name of the group to provide the value. It must not be
     *            {@code null}.
     *
     * @return the text provided by the given group of the pattern, or
     *         {@code null} if not matching
     */
    private String parseConstant(Pattern pattern, String value) {
        final Matcher matcher = pattern.matcher(line);

        if (matcher.find(position)) {
            final String result = matcher.group(value);
            text.append(matcher.group());
            position = matcher.end();
            return result;
        }

        return null;
    }

    /**
     * Parses a version string.
     *
     * @param value
     *            the value to parse. It must not be {@code null}.
     *
     * @return the version
     *
     * @throws ParseException
     *             if the parsing fails
     */
    private Version requireVersion(String value) throws ParseException {
        try {
            return Version.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw failure(e.getMessage(), e);
        }
    }
}
