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

package net.yetamine.pet4bnd.format;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.Persistable;
import net.yetamine.pet4bnd.version.Version;

/**
 * Formats a definition to the <i>bnd</i> format.
 */
public final class Format2Bnd implements Persistable {

    /** Bundle version header literal. */
    private static final String BUNDLE_VERSION_HEADER = "Bundle-Version:";
    /** Export header literal. */
    private static final String EXPORT_PACKAGE_HEADER = "Export-Package:";

    /** Comment to include in the generated files. */
    private static final String COMMENT_GENERATOR = "# Generated by the pet4bnd tool";
    /** Comment header for generation timestamp. */
    private static final String COMMENT_TIMESTAMP = "# ";

    /** Indentation for generated file. */
    private static final String INDENTATION_TEXT = "    ";
    /** Size of the indentation for generated files. */
    private static final int INDENTATION_SIZE = INDENTATION_TEXT.length();

    /** Export bundle version. */
    private final Version bundleVersion;
    /** Content to format on demand. */
    private final List<String> exports;
    /** Line length for formatting. */
    private final int lineLength;
    /** Timestamp source. */
    private final Clock timestamp;

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to format. It must not be {@code null}.
     * @param bundleVersionOverride
     *            the bundle version to export or {@code null} if no bundle
     *            version shall be exported
     */
    public Format2Bnd(Bundle definition, Version bundleVersionOverride) {
        final Collection<PackageExport> packageExports = definition.exports().values();
        final List<String> lines = new ArrayList<>(packageExports.size());
        lineLength = formatExports(packageExports, lines::add);
        exports = Collections.unmodifiableList(lines);
        bundleVersion = bundleVersionOverride;
        timestamp = Clock.systemUTC();
    }

    /**
     * Creates a new instance from the given instance.
     *
     * @param source
     *            the source instance. It must not be {@code null}.
     * @param clock
     *            the clock for the timestamp. It may be {@code null} for no
     *            timestamp
     */
    private Format2Bnd(Format2Bnd source, Clock clock) {
        bundleVersion = source.bundleVersion;
        lineLength = source.lineLength;
        exports = source.exports;
        timestamp = clock;
    }

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to format. It must not be {@code null}.
     * @param renderBundleVersion
     *            {@code true} if the bundle version shall be exported
     */
    public Format2Bnd(Bundle definition, boolean renderBundleVersion) {
        this(definition, renderBundleVersion ? definition.version().resolution() : null);
    }

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to format. It must not be {@code null}.
     */
    public Format2Bnd(Bundle definition) {
        this(definition, definition.version().resolution());
    }

    /**
     * Returns a new instance based on this instance that uses the given clock
     * for the timestamp.
     *
     * @param clock
     *            the clock for the timestamp. It may be {@code null} for no
     *            timestamp
     *
     * @return the new instance
     */
    public Format2Bnd timestamp(Clock clock) {
        return new Format2Bnd(this, clock);
    }

    /**
     * Provides a read-only view on the lines with the exports.
     *
     * @return the lines with the exports
     */
    public List<String> exports() {
        return exports;
    }

    /**
     * Stores the encapsulated object in the given sink.
     *
     * @param sink
     *            the sink to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    public void persist(BufferedWriter sink) throws IOException {
        Objects.requireNonNull(sink);

        // Generate the bnd file
        sink.write(COMMENT_GENERATOR);
        sink.newLine();

        if (timestamp != null) {
            sink.write(COMMENT_TIMESTAMP);
            sink.write(Instant.now(timestamp).toString());
            sink.newLine();
        }

        sink.newLine();

        if (bundleVersion != null) {
            sink.write(BUNDLE_VERSION_HEADER);
            sink.write(' ');
            sink.write(bundleVersion.toString());
            sink.newLine();
            sink.newLine();
        }

        if (exports.isEmpty()) {
            return;
        }

        sink.write(EXPORT_PACKAGE_HEADER);
        // Compute the line end position: include indentation + trailing space to the backslash
        final int lineJoinOffset = lineLength + INDENTATION_SIZE - (lineLength % INDENTATION_SIZE);
        for (int i = lineJoinOffset - EXPORT_PACKAGE_HEADER.length() + INDENTATION_SIZE; i > 0; i--) {
            sink.append(' ');
        }

        sink.write('\\');
        sink.newLine();

        // Having the header, continue with the exports
        final int last = exports.size() - 1;
        for (int i = 0; i < last; i++) {
            sink.write(INDENTATION_TEXT);
            final String export = exports.get(i);
            sink.write(export);
            sink.write(',');

            // Fill the remaining part to the joining (subtract one more for the comma!)
            for (int remaining = lineJoinOffset - export.length(); --remaining > 0;) {
                sink.append(' ');
            }

            // End the line
            sink.write('\\');
            sink.newLine();
        }

        // The last export is solved separately to avoid a useless comma
        sink.write(INDENTATION_TEXT);
        sink.write(exports.get(last));
        sink.newLine();
        sink.newLine(); // Make a blank line for the case of any continuation
    }

    /**
     * @see net.yetamine.pet4bnd.model.Persistable#persist(java.io.OutputStream)
     */
    public void persist(OutputStream sink) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sink, StandardCharsets.UTF_8))) {
            persist(writer);
        }
    }

    /**
     * Formats the exports into a line sequence.
     *
     * @param packageExports
     *            the exports to process. It must not be {@code null}.
     * @param lines
     *            the consumer of the lines. It must not be {@code null}.
     *
     * @return the maximal length of the lines
     */
    private static int formatExports(Iterable<PackageExport> packageExports, Consumer<? super String> lines) {
        // Format the packages into lines to capture their length for pretty formatting
        int result = EXPORT_PACKAGE_HEADER.length();

        for (PackageExport packageExport : packageExports) {
            // Build the line with this package's export
            final StringBuilder builder = new StringBuilder(packageExport.packageName()).append(';');
            builder.append("version=\"").append(packageExport.version().resolution()).append('"');
            packageExport.attributes().filter(a -> !a.isEmpty()).ifPresent(a -> {
                builder.append(';').append(a);
            });

            // Record the line and its length
            final String export = builder.toString();
            result = Math.max(result, export.length());
            lines.accept(export);
        }

        return result;
    }
}
