package net.yetamine.pet4bnd.format;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.Persistable;

/**
 * Formats a definition to the <i>bnd</i> format.
 */
public final class Format2Bnd implements Persistable<BufferedWriter> {

    /** Export header literal. */
    private static final String EXPORT_HEADER = "Export-Package:";

    /** Indentation for generated file. */
    private static final String INDENTATION_TEXT = "    ";
    /** Size of the indentation for generated files. */
    private static final int INDENTATION_SIZE = INDENTATION_TEXT.length();

    /** Content to format on demand. */
    private final List<String> exports;
    /** Line length for formatting. */
    private final int lineLength;

    /**
     * Creates a new instance.
     *
     * @param definition
     *            the definition to format. It must not be {@code null}.
     */
    public Format2Bnd(Bundle definition) {
        final Collection<PackageExport> packageExports = definition.exports().values();
        final List<String> lines = new ArrayList<>(packageExports.size());
        lineLength = formatExports(packageExports, lines::add);
        exports = Collections.unmodifiableList(lines);
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
     * @see net.yetamine.pet4bnd.model.Persistable#persist(java.lang.Object)
     */
    public void persist(BufferedWriter sink) throws IOException {
        Objects.requireNonNull(sink);

        // Generate the bnd file
        sink.write(EXPORT_HEADER);
        // Compute the line end position: include indentation + trailing space to the backslash
        final int lineJoinOffset = lineLength + INDENTATION_SIZE - (lineLength % INDENTATION_SIZE);
        for (int i = lineJoinOffset - EXPORT_HEADER.length() + INDENTATION_SIZE; i > 0; i--) {
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
     * @see net.yetamine.pet4bnd.model.Persistable#store(java.nio.file.Path)
     */
    public void store(Path path) throws IOException {
        try (BufferedWriter sink = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            persist(sink);
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
        int result = EXPORT_HEADER.length();

        for (PackageExport packageExport : packageExports) {
            // Build the line with this package's export
            final StringBuilder builder = new StringBuilder(packageExport.packageName()).append(';');
            builder.append("version=\"").append(packageExport.versionBaseline()).append('"');
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
