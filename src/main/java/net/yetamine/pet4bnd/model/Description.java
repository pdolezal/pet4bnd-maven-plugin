package net.yetamine.pet4bnd.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * Encapsulates a bundle description.
 */
public final class Description extends Bundle implements Persistable<BufferedWriter> {

    /** Representation of the parsed content. */
    private final List<LineNode> representation;

    /**
     * Creates a new instance.
     *
     * @param parser
     *            the parser of the data. It must not be {@code null}.
     */
    Description(ParserImplementation parser) {
        super(parser.bundleOptions, Collections.unmodifiableMap(parser.bundleExports));
        representation = parser.representation;
    }

    /**
     * Creates a new parser.
     *
     * @return the new parser
     */
    public static LineParser<Description> newParser() {
        return new ParserImplementation();
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
    public static LineParser<Description> parse(Path path, Feedback feedback) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            final LineParser<Description> parser = Description.newParser().feedback(feedback);
            lines.forEach(parser); // Perform the actual processing
            return parser.finish();
        }
    }

    /**
     * @see net.yetamine.pet4bnd.model.Persistable#persist(java.lang.Object)
     */
    public void persist(BufferedWriter sink) throws IOException {
        for (LineNode line : representation) {
            sink.write(line.toString());
            sink.newLine();
        }
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
     * A parser for the {@link Description} class.
     */
    static final class ParserImplementation implements LineParser<Description> {

        /** Name of the option for bundle version. */
        private static final String BUNDLE_OPTION_VERSION = "$bundle-version";

        /** Full line representation to reconstruct the original. */
        final List<LineNode> representation = new ArrayList<>();
        /** Parsed package exports (except for the pending one). */
        final Map<String, PackageExport> bundleExports = new TreeMap<>();
        /** Parsed bundle options. */
        BundleOptions bundleOptions;

        /** Definition waiting for completion. */
        private VersionDefinition pendingDefinition;

        /** Feedback instance. */
        private Feedback feedback = Feedback.none();
        /** Number of warnings. */
        private int warningCount;
        /** Number of errors. */
        private int errorCount;
        /** Flag for finished. */
        private Optional<Description> result = Optional.empty();

        /**
         * Creates a new instance.
         */
        public ParserImplementation() {
            // Default constructor
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#feedback(net.yetamine.pet4bnd.feedback.Feedback)
         */
        public LineParser<Description> feedback(Feedback value) {
            feedback = Objects.requireNonNull(value);
            return this;
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#feedback()
         */
        public Feedback feedback() {
            return feedback;
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#finish()
         */
        public LineParser<Description> finish() {
            checkNotFinished();

            if (pendingDefinition != null) {
                closeDefinition(pendingDefinition);
                pendingDefinition = null;
            }

            if (bundleOptions == null) { // Deal with missing bundle options: provide hidden defaults
                bundleOptions = new BundleOptions(new ValueNode<Version>().set(Version.ZERO), new ValueNode<>());
                error("A bundle version option required, but missing.", null);
            }

            result = Optional.of(new Description(this));
            return this;
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#finished()
         */
        public boolean finished() {
            return result.isPresent();
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#result()
         */
        public Optional<Description> result() {
            return result;
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#warningCount()
         */
        public int warningCount() {
            return warningCount;
        }

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#errorCount()
         */
        public int errorCount() {
            return errorCount;
        }

        // Parsing core

        /**
         * Pattern for a non-significant line (comments and blank lines).
         */
        private static final Pattern PATTERN_IGNORABLE // @formatter:break
        = Pattern.compile("^\\s*(#.*)?$");

        /**
         * Pattern for an attribute specification. The line starts with <i>+</i>
         * and contains the all attributes as plain text. Whitespace at the ends
         * are ignored.
         */
        private static final Pattern PATTERN_ATTRIBUTES // @formatter:break
        = Pattern.compile("^\\s*\\+\\s*(?<value>.*?)\\s*$");

        /**
         * Pattern for finding the name of a definition.
         */
        private static final Pattern PATTERN_DEFINITION_NAME // @formatter:break
        = Pattern.compile("\\A\\s*(?<value>[^\\s:=<@#]+)\\s*:\\s*");

        /**
         * Pattern for finding the version baseline.
         */
        private static final Pattern PATTERN_DEFINITION_BASELINE // @formatter:break
        = Pattern.compile("(?<value>\\d+(\\.\\d+)?(\\.\\d+)?(\\.[^\\s:=<@#]+)?)");

        /**
         * Pattern for finding the version constraint.
         */
        private static final Pattern PATTERN_DEFINITION_CONSTRAINT // @formatter:break
        = Pattern.compile("(?<prefix>\\s*<\\s*)(?<value>\\d+(\\.\\d+)?(\\.\\d+)?(\\.[^\\s:=<@#]+)?)");

        /**
         * Pattern for finding the version variance.
         */
        private static final Pattern PATTERN_DEFINITION_VARIANCE // @formatter:break
        = Pattern.compile("(?<prefix>\\s*@\\s*)(?<value>[A-Za-z]+)");

        /**
         * Pattern for finding the trailing characters.
         */
        private static final Pattern PATTERN_DEFINITION_TRAILING // @formatter:break
        = Pattern.compile("\\s*(#.*)?");

        /**
         * @see net.yetamine.pet4bnd.model.LineParser#accept(java.lang.CharSequence)
         */
        public void accept(CharSequence line) {
            checkNotFinished();

            // Try to parse comments and blank lines
            if (PATTERN_IGNORABLE.matcher(line).matches()) {
                recordLine(line);
                return;
            }

            // Try to parse attributes
            final Matcher attributes = PATTERN_ATTRIBUTES.matcher(line);

            if (attributes.matches()) { // Attribute value exists
                if (pendingDefinition == null) {
                    recordLine(line);
                    error("Export attribute definition missing preceding package export.", null);
                    return;
                }

                pendingDefinition.attributes(attributes.group("value"));
                closeDefinition(pendingDefinition);
                pendingDefinition = null;
                recordLine(line);
                return;
            }

            // Nothing like attributes
            if (pendingDefinition != null) {
                closeDefinition(pendingDefinition);
                pendingDefinition = null;
            }

            // Open a new definition, which shall become the pending one
            final VersionDefinition definition = new VersionDefinition();

            try {
                // Match the definition name
                final Matcher name = PATTERN_DEFINITION_NAME.matcher(line);

                if (!name.find()) { // Name malformed or not a valid definition line at all
                    throw new IllegalArgumentException("Unacceptable definition name.");
                }

                final LineNode lineNode = new LineNode();
                definition.name(name.group("value"));
                lineNode.append(name.group());
                int position = name.end();

                // Match the version baseline
                if (line.length() <= position) {
                    throw new IllegalArgumentException("Missing version baseline.");
                }

                final Matcher baseline = PATTERN_DEFINITION_BASELINE.matcher(line);

                if (!baseline.find(position)) {
                    throw new IllegalArgumentException("Version baseline invalid.");
                }

                definition.versionBaseline().set(Version.valueOf(baseline.group("value")));
                lineNode.append(definition.versionBaseline());
                position = baseline.end();

                { // Match the version constraint
                    final ValueNode<Version> node = definition.versionConstraint();
                    String prefix = " < "; // Default formatting prefix

                    if (position < line.length()) {
                        final Matcher matcher = PATTERN_DEFINITION_CONSTRAINT.matcher(line);

                        if (matcher.find(position)) {
                            node.set(Version.valueOf(matcher.group("value")));
                            prefix = matcher.group("prefix");
                            position = matcher.end();
                        }
                    }

                    final String formattingPrefix = prefix; // Save for the lambda
                    node.formatter(o -> formattingPrefix + o.toString());
                    lineNode.append(node);
                }

                { // Match the version variance
                    final ValueNode<VersionVariance> node = definition.versionVariance();
                    String prefix = " @ "; // Default formatting prefix

                    if (position < line.length()) {
                        final Matcher matcher = PATTERN_DEFINITION_VARIANCE.matcher(line);

                        if (matcher.find(position)) {
                            node.set(VersionVariance.valueOf(matcher.group("value").toUpperCase()));
                            prefix = matcher.group("prefix");
                            position = matcher.end();
                        }
                    }

                    final String formattingPrefix = prefix; // Save for the lambda
                    node.formatter(o -> formattingPrefix + o.toString().toLowerCase());
                    lineNode.append(node);
                }

                // Check the trailing string (but do not store useless whitespace)
                final String trailing = line.subSequence(position, line.length()).toString();
                if (!PATTERN_DEFINITION_TRAILING.matcher(trailing).matches()) {
                    warn("Unknown construct found at the end of the line.", null);
                    lineNode.append(trailing); // Store trailing then
                }

                representation.add(lineNode);
            } catch (IllegalArgumentException e) {
                recordLine(line); // Store the whole line
                error("Could not parse the definition.", e);
                return;
            }

            // Solve the special case: the bundle option for version
            if (BUNDLE_OPTION_VERSION.equals(definition.name())) {
                assert (definition.attributes() == null);

                if (bundleOptions != null) { // Must be unique!
                    warn("Bundle version duplicated. Using only the first occurrence.", null);
                    return;
                }

                if (definition.versionVariance().get() != null) {
                    warn("Bundle version shall have no variance.", null);
                }

                bundleOptions = new BundleOptions(definition);
                return;
            }

            pendingDefinition = definition; // Remember for completion
        }

        /**
         * Checks if this parser has not finished yet.
         */
        private void checkNotFinished() {
            if (finished()) {
                throw new IllegalStateException();
            }
        }

        /**
         * Records the line as it is.
         *
         * @param line
         *            the line to record. It must not be {@code null}.
         */
        private void recordLine(CharSequence line) {
            representation.add(new LineNode().append(line.toString()));
        }

        /**
         * Deal with a pending definition.
         *
         * @param definition
         *            the definition to close. It must not be {@code null}.
         */
        private void closeDefinition(VersionDefinition definition) {
            final String name = definition.name();

            if (bundleExports.containsKey(name)) {
                final String f = "Duplicated definition for '%s'. Using only the first occurrence.";
                warn(String.format(f, name), null);
                return;
            }

            final PackageExport export = new PackageExport(definition);
            final PackageExport last = bundleExports.put(export.packageName(), export);
            assert (last == null);
        }

        /**
         * Reports an error.
         *
         * @param message
         *            the message
         * @param t
         *            the related exception if available
         */
        private void error(String message, Throwable t) {
            ++errorCount;
            feedback.fail(message, t);
        }

        /**
         * Reports an error.
         *
         * @param message
         *            the message
         * @param t
         *            the related exception if available
         */
        private void warn(String message, Throwable t) {
            ++warningCount;
            feedback.warn(message, t);
        }
    }
}
