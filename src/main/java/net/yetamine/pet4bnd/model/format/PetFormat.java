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
import net.yetamine.pet4bnd.model.BundleVersion;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.Persistable;

/**
 * Encapsulates a bundle description.
 */
public final class PetFormat implements Bundle, Persistable {

    /** Packages that the bundle exports. */
    private final Map<String, PackageExport> exports;
    /** Bundle version description. */
    private final BundleVersion version;
    /** Representation of the parsed content. */
    private final List<TextLine> representation;

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
    public BundleVersion version() {
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
     * <p>
     * The implementation uses the given feedback to report the lines where an
     * error or warning occurs.
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
            final ParsingFeedback logger = new ParsingFeedback(feedback);
            final PetParser parser = new PetParser().feedback(logger);

            lines.forEach(line -> {
                logger.record(line);
                parser.accept(line);
            });

            // No line now
            logger.record(null, 0);
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
        for (TextLine line : representation) {
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

    /**
     * Input tracking feedback.
     */
    private static final class ParsingFeedback implements Feedback {

        /** Underlying feedback instance. */
        private final Feedback feedback;
        /** Current input. */
        private String input;
        /** Current line. */
        private int position;

        /**
         * Creates a new instance.
         *
         * @param backing
         *            the underlying feedback instance. It must not be
         *            {@code null}.
         */
        public ParsingFeedback(Feedback backing) {
            feedback = Objects.requireNonNull(backing);
        }

        /**
         * Stores the line for the next messages.
         *
         * @param line
         *            the line to store
         * @param number
         *            the line number
         */
        public void record(String line, int number) {
            position = number;
            input = line;
        }

        /**
         * Stores the next line for the next messages, i.e., increments the line
         * number.
         *
         * @param line
         *            the line to store
         */
        public void record(String line) {
            input = line;

            if (input != null) {
                ++position;
            }
        }

        /**
         * @see net.yetamine.pet4bnd.feedback.Feedback#fail(java.lang.String,
         *      java.lang.Throwable)
         */
        public void fail(String message, Throwable t) {
            if (input == null) {
                feedback.fail(message, t);
                return;
            }

            feedback.fail(message);
            feedback.fail(reference(), t);
        }

        /**
         * @see net.yetamine.pet4bnd.feedback.Feedback#warn(java.lang.String,
         *      java.lang.Throwable)
         */
        public void warn(String message, Throwable t) {
            if (input == null) {
                feedback.warn(message, t);
                return;
            }

            feedback.warn(message);
            feedback.warn(reference(), t);
        }

        /**
         * @see net.yetamine.pet4bnd.feedback.Feedback#info(java.lang.String)
         */
        public void info(String message) {
            feedback.info(message);
        }

        /**
         * Renders the reference for the current input, which must not be
         * {@code null}.
         *
         * @return the reference for the current input
         */
        private String reference() {
            assert (input != null);
            return (position > 0) ? String.format("See line %d: %s", position, input) : "See: " + input;
        }
    }
}
