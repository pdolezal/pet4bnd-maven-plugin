package net.yetamine.pet4bnd.model.format;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.Persistable;
import net.yetamine.pet4bnd.model.VersionStatement;

/**
 * Encapsulates a bundle description.
 */
public final class PetFormat implements Bundle, Persistable {

    /** Packages that the bundle exports. */
    private final Map<String, PackageExport> exports;
    /** Bundle version description. */
    private final VersionStatement version;
    /** Representation of the parsed content. */
    private final List<LineNode> representation;

    /**
     * Creates a new instance.
     *
     * @param parser
     *            the parser of the data. It must not be {@code null}.
     */
    PetFormat(PetParser parser) {
        representation = parser.representation();
        exports = Collections.unmodifiableMap(parser.bundleExports());
        version = parser.bundleVersion();
    }

    /**
     * @see net.yetamine.pet4bnd.model.Bundle#version()
     */
    public VersionStatement version() {
        return version;
    }

    /**
     * @see net.yetamine.pet4bnd.model.Bundle#exports()
     */
    public Map<String, PackageExport> exports() {
        return exports;
    }

    /**
     * Parses the specified definition file.
     *
     * @param path
     *            the path to the file. It must not be {@code null}.
     * @param feedback
     *            the parser feedback to use. It must not be {@code null}.
     *
     * @return the parser containing the result of parsing and error statistics
     *
     * @throws IOException
     *             if an I/O operation failed
     */
    public static PetParser parse(Path path, Feedback feedback) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            final PetParser parser = new PetParser().feedback(feedback);
            lines.forEach(parser); // Perform the actual processing
            return parser.finish();
        }
    }

    /**
     * Formats the bundle description.
     *
     * @param sink
     *            the sink to accept lines of the representation. It must not be
     *            {@code null}.
     */
    public void format(Consumer<? super String> sink) {
        Objects.requireNonNull(sink);
        representation.forEach(line -> sink.accept(line.toString()));
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
        for (LineNode line : representation) {
            sink.write(line.toString());
            sink.newLine();
        }
    }

    /**
     * @see net.yetamine.pet4bnd.model.Persistable#persist(java.io.OutputStream)
     */
    public void persist(OutputStream sink) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sink, StandardCharsets.UTF_8))) {
            persist(writer);
        }
    }
}
