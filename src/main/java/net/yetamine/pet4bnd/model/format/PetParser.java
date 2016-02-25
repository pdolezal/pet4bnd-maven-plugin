package net.yetamine.pet4bnd.model.format;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.model.BundleVersion;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.PackageVersion;
import net.yetamine.pet4bnd.model.VersionDefinition;
import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * A parser for the {@link PetFormat} class.
 */
public final class PetParser implements Consumer<CharSequence> {

    /** Name of the option for bundle version. */
    private static final String BUNDLE_OPTION_VERSION = "$bundle-version";

    /**
     * Pattern for a non-significant line (comments and blank lines).
     */
    private static final Pattern PATTERN_IGNORABLE // @formatter:break
    = Pattern.compile("^\\s*(#.*)?$");

    /**
     * Pattern for an attribute specification. The line starts with <i>+</i> and
     * contains the all attributes as plain text. Whitespace at the ends are
     * ignored.
     */
    private static final Pattern PATTERN_ATTRIBUTES // @formatter:break
    = Pattern.compile("^\\s*\\+\\s*(?<value>.*?)\\s*$");

    /** Parsed bundle version statement. */
    private final BundleVersion bundleVersion = new BundleVersionDefinition();
    /** Parsed package exports (except for the pending one). */
    private final Map<String, PackageExport> bundleExports = new TreeMap<>();
    /** Full line representation to reconstruct the original. */
    private final List<LineNode> representation = new ArrayList<>();

    /** Version inheritance supplier for packages. */
    private final Supplier<Version> versionInheriting = bundleVersion::resolution;

    /** Export version for {@link #pendingExportIdentifier}. */
    private PackageVersion pendingExportVersion;
    /** Pending package export identifier. */
    private String pendingExportIdentifier;
    /** Flag for marking the bundle version. */
    private boolean bundleVersionPresent;

    /** Feedback instance. */
    private Feedback feedback = Feedback.none();
    /** Number of warnings. */
    private int warningCount;
    /** Number of errors. */
    private int errorCount;
    /** Complete result. */
    private Optional<PetFormat> result = Optional.empty();

    /**
     * Creates a new instance.
     */
    public PetParser() {
        // Default constructor
    }

    /**
     * Sets the feedback interface.
     *
     * @param value
     *            the instance to set. It must not be {@code null}.
     *
     * @return this instance
     */
    public PetParser feedback(Feedback value) {
        feedback = Objects.requireNonNull(value);
        return this;
    }

    /**
     * Returns the current feedback interface.
     *
     * @return the current feedback interface
     */
    public Feedback feedback() {
        return feedback;
    }

    /**
     * Accepts next line of the data to parse.
     *
     * <p>
     * The parser works in line-oriented mode, leaving the line splitting on the
     * data source. It allows adapting on platform-dependent line endings and on
     * embedding the lines into different file formats.
     *
     * @param line
     *            the line to parse. It must not be {@code null} and it is
     *            supposed not to contain the line ending.
     *
     * @see java.util.function.Consumer#accept(java.lang.Object)
     *
     * @throws IllegalStateException
     *             if the parser is not parsing
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
            if (closePendingExport(attributes.group("value"))) {
                recordLine(line);
                return;
            }

            recordLine(line);
            error("Export attribute definition missing preceding package export.", null);
            return;
        }

        closePendingExport(null); // Nothing like attributes, close the pending export if any

        try {
            final VersionStatementParser parser = new VersionStatementParser(line);

            final VersionStatement version;
            final String identifier = parser.parseIdentifier();
            if (BUNDLE_OPTION_VERSION.equals(identifier)) {
                if (bundleVersionPresent) {
                    recordLine(line);
                    warn("Bundle version duplicated. Using the first occurrence.", null);
                    return;
                }

                bundleVersionPresent = true;
                version = bundleVersion;
            } else {
                version = new PackageVersionDefinition(versionInheriting);
            }

            version.baseline(parser.parseBaseline(o -> version.baseline()));
            version.constraint(parser.parseConstraint(o -> version.constraint()));
            version.variance(parser.parseVariance(o -> {
                final VersionVariance variance = version.variance();
                return (variance != null) ? variance.toString().toLowerCase() : null;
            }));

            if (!parser.finish()) { // Record the trailing part, hence it may just warn
                warn("Unknown construct found at the end of the line.", null);
            }

            // Record a pending package export
            if (version instanceof PackageVersion) {
                createPendingExport(identifier, (PackageVersion) version);
            }

            representation.add(parser.representation());
        } catch (ParseException e) {
            recordLine(line);
            error(null, e);
        }
    }

    /**
     * Finishes the parsing.
     *
     * <p>
     * The parser may provide a result via {@link #result()} even if errors or
     * warnings were encountered. However, such a result was achieved using some
     * error recovery heuristics and might provide wrong or unexpected input for
     * further processing.
     *
     * @return this instance
     *
     * @throws IllegalStateException
     *             if the parser is not in the parsing state
     */
    public PetParser finish() {
        checkNotFinished();

        closePendingExport(null);

        if (!bundleVersionPresent) { // This is an error indeed as it prevents safe formatting
            error("A bundle version option required, but missing.", null);
        }

        result = Optional.of(new PetFormat(this));
        return this;
    }

    /**
     * Indicates whether the parser finished parsing (i.e., {@link #finish()}
     * has been called).
     *
     * @return {@code true} if the parser is finished parsing
     */
    public boolean finished() {
        return result.isPresent();
    }

    /**
     * Provides the result of the parsing.
     *
     * @return the result of the parsing, or an empty container if no result is
     *         available (perhaps when still parsing)
     */
    public Optional<PetFormat> result() {
        return result;
    }

    /**
     * Returns the number of warnings so far.
     *
     * @return the number of warnings so far
     */
    public int warningCount() {
        return warningCount;
    }

    /**
     * Returns the number of errors so far.
     *
     * @return the number of errors so far
     */
    public int errorCount() {
        return errorCount;
    }

    // Interface for clients

    /**
     * Provides the representation details.
     *
     * @return the representation details
     */
    List<LineNode> representation() {
        return representation;
    }

    /**
     * Provides the live map of exports.
     *
     * @return the live map of exports
     */
    Map<String, PackageExport> bundleExports() {
        return bundleExports;
    }

    /**
     * Provides the current bundle version.
     *
     * @return the current bundle version
     */
    BundleVersion bundleVersion() {
        return bundleVersion;
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
     * Records arguments for a pending export.
     *
     * @param identifier
     *            the identifier to export. It must not be {@code null}.
     * @param version
     *            the version to export. It must not be {@code null}.
     */
    private void createPendingExport(String identifier, PackageVersion version) {
        assert (pendingExportIdentifier == null);
        assert (pendingExportVersion == null);
        pendingExportIdentifier = identifier;
        pendingExportVersion = version;
        assert (pendingExportVersion != null);
        assert (pendingExportIdentifier != null);
    }

    /**
     * Deal with a pending export definition.
     *
     * @param attributes
     *            the attributes for the definition
     *
     * @return {@code true} if a pending export has been closed
     */
    private boolean closePendingExport(String attributes) {
        if (pendingExportVersion == null) { // Nothing pending
            assert (pendingExportIdentifier == null);
            return false;
        }

        final PackageVersion exportVersion = pendingExportVersion;
        final String exportIdentifier = pendingExportIdentifier;
        // Clean before further processing
        pendingExportIdentifier = null;
        pendingExportVersion = null;

        // Check the name availability
        if (bundleExports.containsKey(exportIdentifier)) {
            final String f = "Duplicated definition for '%s'. Using only the first occurrence.";
            warn(String.format(f, exportIdentifier), null);
            return true;
        }

        // Record the export
        final PackageExport packageExport = new PetExport(exportIdentifier, exportVersion, attributes);
        final PackageExport last = bundleExports.put(packageExport.packageName(), packageExport);
        assert (last == null);
        return true;
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

    private static final class VersionStatementParser {

        /** Name of the version inheritance directive. */
        private static final String VERSION_DIRECTIVE_INHERIT = "inherit";

        /**
         * Pattern for finding the name of a definition.
         */
        private static final Pattern PATTERN_DEFINITION_NAME // @formatter:break
        = Pattern.compile("\\A\\s*(?<value>[^\\s:=<@#]+)\\s*:\\s*");

        /**
         * Pattern for finding the version baseline.
         */
        private static final Pattern PATTERN_DEFINITION_BASELINE // @formatter:break
        = Pattern.compile("(?<directive>[A-Z-a-z]+)|(?<value>\\d+(\\.\\d+)?(\\.\\d+)?(\\.[^\\s:=<@#]+)?)");

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

        private final CharSequence line;
        private final LineNode representation;
        private int position;

        /**
         * Creates a new instance.
         *
         * @param source
         *            the source to parse. It must not be {@code null}.
         * @param startPosition
         *            the position to start with
         */
        public VersionStatementParser(CharSequence source, int startPosition) {
            position = Math.min(Math.max(0, startPosition), source.length());
            representation = new LineNode();
            line = source;
        }

        /**
         * Creates a new instance.
         *
         * @param source
         *            the source to parse. It must not be {@code null}.
         */
        public VersionStatementParser(CharSequence source) {
            this(source, 0);
        }

        /**
         * Returns the line representation.
         *
         * @return the line representation
         */
        public LineNode representation() {
            return representation;
        }

        /**
         * Parses the identifier.
         *
         * @return the identifier
         *
         * @throws ParseException
         *             if the parsing fails
         */
        public String parseIdentifier() throws ParseException {
            final Matcher name = PATTERN_DEFINITION_NAME.matcher(line);

            if (!name.find()) { // Name malformed or not a valid definition line at all
                throw new ParseException("Unacceptable definition name.", position);
            }

            final String result = name.group("value");
            representation.append(name.group());
            position = name.end();
            return result;
        }

        /**
         * Parses the version baseline.
         *
         * @param formatter
         *            the formatter of the representation placeholder. It must
         *            not be {@code null}.
         *
         * @return the version baseline
         *
         * @throws ParseException
         *             if the parsing fails
         */
        public Version parseBaseline(Function<? super Version, ?> formatter) throws ParseException {
            assert (formatter != null);

            if (line.length() <= position) { // Match the version baseline
                throw new ParseException("Missing version baseline.", position);
            }

            final Matcher matcher = PATTERN_DEFINITION_BASELINE.matcher(line);

            if (!matcher.find(position)) {
                throw new ParseException("Version baseline invalid.", position);
            }

            final String directive = matcher.group("directive");
            if (VERSION_DIRECTIVE_INHERIT.equals(directive)) {
                representation.append(directive);
                position = matcher.end();
                return null;
            }

            if (directive != null) { // Value would be null otherwise
                throw new ParseException(String.format("Unknown directive '%s'.", directive), position);
            }

            final Version result = parseVersion(matcher.group("value"), position);
            representation.append(() -> Objects.toString(formatter.apply(result), ""));
            position = matcher.end();
            return result;
        }

        /**
         * Parses the version constraint.
         *
         * @param formatter
         *            the formatter of the representation placeholder. It must
         *            not be {@code null}.
         *
         * @return the version constraint, or {@code null} if none
         *
         * @throws ParseException
         *             if the parsing fails
         */
        public Version parseConstraint(Function<? super Version, ?> formatter) throws ParseException {
            assert (formatter != null);

            if (position < line.length()) {
                final Matcher matcher = PATTERN_DEFINITION_CONSTRAINT.matcher(line);

                if (matcher.find(position)) {
                    final Version result = parseVersion(matcher.group("value"), position);
                    final String prefix = matcher.group("prefix");

                    representation.append(() -> {
                        final Object value = formatter.apply(result);
                        return (value != null) ? prefix + value : "";
                    });

                    position = matcher.end();
                    return result;
                }
            }

            representation.append(() -> {
                final Object value = formatter.apply(null);
                return (value != null) ? " < " + value : "";
            });

            return null;
        }

        /**
         * Parses the version variance.
         *
         * @param formatter
         *            the formatter of the representation placeholder. It must
         *            not be {@code null}.
         *
         * @return the version variance, or {@code null} if none
         *
         * @throws ParseException
         *             if the parsing fails
         */
        public VersionVariance parseVariance(Function<? super VersionVariance, ?> formatter) throws ParseException {
            assert (formatter != null);

            if (position < line.length()) {
                final Matcher matcher = PATTERN_DEFINITION_VARIANCE.matcher(line);

                if (matcher.find(position)) {
                    final VersionVariance result;
                    try { // Parse the variance safely
                        result = VersionVariance.valueOf(matcher.group("value").toUpperCase());
                    } catch (IllegalArgumentException e) {
                        final ParseException t = new ParseException(e.getMessage(), position);
                        t.initCause(e);
                        throw t;
                    }

                    final String prefix = matcher.group("prefix");

                    representation.append(() -> {
                        final Object value = formatter.apply(result);
                        return (value != null) ? prefix + value : "";
                    });

                    position = matcher.end();
                    return result;
                }
            }

            representation.append(() -> {
                final Object value = formatter.apply(null);
                return (value != null) ? " @ " + value : "";
            });

            return null;
        }

        /**
         * Parses the trailing part of the input line and stores it.
         *
         * @return {@code true} if the trailing part contains no unexpected
         *         characters
         */
        public boolean finish() {
            // Check the trailing string (but do not store useless whitespace)
            final String trailing = line.subSequence(position, line.length()).toString();
            representation.append(trailing); // Store trailing always
            return PATTERN_DEFINITION_TRAILING.matcher(trailing).matches();
        }

        /**
         * Parses a version string.
         *
         * @param value
         *            the value to parse. It must not be {@code null}.
         * @param position
         *            the position to report as an error if the parsing fails
         *
         * @return the version
         *
         * @throws ParseException
         *             if the parsing fails
         */
        private static Version parseVersion(String value, int position) throws ParseException {
            try {
                return Version.valueOf(value);
            } catch (IllegalArgumentException e) {
                final ParseException t = new ParseException(e.getMessage(), position);
                t.initCause(e);
                throw t;
            }
        }
    }
}

/**
 * Represents a definition statement for a bundle.
 */
final class BundleVersionDefinition extends VersionDefinition implements BundleVersion {

    /**
     * Creates a new instance.
     */
    public BundleVersionDefinition() {
        super.baseline(Version.ZERO);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionStatement#resolution()
     */
    @Override
    public Version resolution() {
        final Version resolution = super.resolution();
        if (resolution != null) {
            return resolution;
        }

        final Version baseline = baseline();
        final VersionVariance variance = variance();
        return (variance != null) ? variance.apply(baseline) : baseline;
    }
}

/**
 * Represents a definition statement for a package.
 */
final class PackageVersionDefinition extends VersionDefinition implements PackageVersion {

    /** Dynamic resolver. */
    private final Supplier<Version> resolver;

    /**
     * Creates a new instance.
     *
     * @param resolutionSupplier
     *            the supplier of the baseline version for the cases when the
     *            baseline version shall be inherited. It must not be
     *            {@code null}.
     */
    public PackageVersionDefinition(Supplier<Version> resolutionSupplier) {
        resolver = Objects.requireNonNull(resolutionSupplier);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionDefinition#resolution()
     */
    @Override
    public Version resolution() {
        final Version resolution = super.resolution();
        if (resolution != null) {
            return resolution;
        }

        final Version baseline = baseline();
        if (baseline == null) {
            return resolver.get();
        }

        final VersionVariance variance = variance();
        return (variance != null) ? variance.apply(baseline) : baseline;
    }
}
