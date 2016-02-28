package net.yetamine.pet4bnd.model.format;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.model.BundleVersion;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.PackageVersion;
import net.yetamine.pet4bnd.model.VersionGroup;
import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.model.support.BundleVersionDefinition;
import net.yetamine.pet4bnd.model.support.PackageExportDefinition;
import net.yetamine.pet4bnd.model.support.PackageGroupDefinition;
import net.yetamine.pet4bnd.model.support.PackageVersionDefinition;

/**
 * A parser for the {@link PetFormat} class.
 */
public final class PetParser implements Consumer<CharSequence> {

    /** Name of the group representing the bundle version statement. */
    private static final String BUNDLE_VERSION_STATEMENT = "$bundle";

    /** Parsed bundle version statement. */
    private final BundleVersionDefinition bundleVersion = new BundleVersionDefinition();
    /** Known version groups (including {@link #bundleVersion()} when found). */
    private final Map<String, VersionStatement> versionGroups = new HashMap<>();
    /** Parsed package exports (except for the pending one). */
    private final Map<String, PackageExport> bundleExports = new TreeMap<>();
    /** Full line representation to reconstruct the original. */
    private final List<TextLine> representation = new ArrayList<>();

    /** Export version for {@link #pendingExportIdentifier}. */
    private PackageVersion pendingExportVersion;
    /** Pending package export identifier. */
    private String pendingExportIdentifier;

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

        try {
            accept(new LineParser(line));
        } catch (ParseException e) {
            saveLine(line);
            error(e);
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

        if (!versionGroups.containsKey(BUNDLE_VERSION_STATEMENT)) {
            final String f = "The %s declaration required, but missing.";
            error(String.format(f, BUNDLE_VERSION_STATEMENT), null);
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
    List<TextLine> representation() {
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
            warn(String.format(f, exportIdentifier));
            return true;
        }

        // Record the export
        final PackageExport packageExport = new PackageExportDefinition(exportIdentifier, exportVersion, attributes);
        final PackageExport last = bundleExports.put(packageExport.packageName(), packageExport);
        assert (last == null);
        return true;
    }

    /**
     * Reports an error.
     *
     * @param message
     *            the message. It must not be {@code null}.
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
     * @param t
     *            the related exception. It must not be {@code null}.
     */
    private void error(Throwable t) {
        ++errorCount;
        feedback.fail(t);
    }

    /**
     * Reports an error.
     *
     * @param message
     *            the message
     */
    private void warn(String message) {
        ++warningCount;
        feedback.warn(message);
    }

    /**
     * Saves the line as it is in the {@link #representation()}.
     *
     * @param line
     *            the line to record. It must not be {@code null}.
     */
    private void saveLine(CharSequence line) {
        representation.add(new TextLine().append(line.toString()));
    }

    /**
     * Accepts the source fragment with the given parser.
     *
     * @param parser
     *            the parser to use for processing the source. It must not be
     *            {@code null}.
     *
     * @throws ParseException
     *             if parsing the source fails
     */
    private void accept(LineParser parser) throws ParseException {
        // Parse comments and blank lines
        if (parser.parseIgnorable()) {
            representation.add(parser.text());
            return;
        }

        // Parse attributes for a pending export
        final String attributes = parser.parseAttributes();

        if (attributes != null) {
            if (closePendingExport(attributes)) {
                representation.add(parser.text());
                return;
            }

            // Valid source, but not semantically (must follow an export)
            throw parser.failure("Export attribute definition missing preceding package export.");
        }

        closePendingExport(null); // Nothing like attributes, close the pending export if any

        // Parse a group declaration
        final String group = parser.parseGroupDeclaration();

        if (group != null) { // It is a group
            if (versionGroups.containsKey(group)) {
                saveLine(parser.line()); // Save before warning (might throw)
                final String f = "Declaration of '%s' duplicated. Using the first occurrence.";
                warn(String.format(f, group));
                return;
            }

            final VersionStatement statement;
            if (BUNDLE_VERSION_STATEMENT.equals(group)) {
                statement = bundleVersion;
            } else {
                // If not a reserved name, make a new definition
                statement = new PackageGroupDefinition(group);
            }

            statement.baseline(parser.requireBaseline(() -> statement.baseline().toString()));
            parseVersionDetails(parser, statement);
            versionGroups.put(group, statement);
            representation.add(parser.text());
            return;
        }

        // Parse an export declaration
        final String export = parser.parseExportDeclaration();
        if (export == null) { // Which is mandatory as the last option left
            throw parser.failure("Unknown construct found.");
        }

        final PackageVersion version = new PackageVersionDefinition();
        requireVersionBaseline(parser, version);
        parseVersionDetails(parser, version);
        createPendingExport(export, version);
        representation.add(parser.text());
    }

    /**
     * Parses a version baseline.
     *
     * @param parser
     *            the parser to use. It must not be {@code null}.
     * @param version
     *            the version object to fill with the data. It must not be
     *            {@code null}.
     *
     * @throws ParseException
     *             if the parsing fails
     */
    private void requireVersionBaseline(LineParser parser, PackageVersion version) throws ParseException {
        // Make the formater for the version baseline
        final TextFragment baselineFormatter = () -> {
            return version.inheritance().map(s -> {
                if (s instanceof BundleVersion) {
                    return BUNDLE_VERSION_STATEMENT;
                }

                if (s instanceof VersionGroup) {
                    return ((VersionGroup) s).identifier();
                }

                final String f = "Unable to format reference of class '%s'.";
                throw new IllegalArgumentException(String.format(f, s.getClass()));
            }).orElseGet(() -> version.baseline().toString());
        };

        // Parse the baseline as a group reference
        final String reference = parser.parseGroupReference(baselineFormatter);
        if (reference == null) { // If no reference, the version baseline must be here
            version.baseline(parser.requireBaseline(baselineFormatter));
            return;
        }

        final VersionStatement statement = versionGroups.get(reference);

        if (statement == null) {
            final String f = "Reference to undefined group '%s'.";
            throw parser.failure(String.format(f, reference));
        }

        version.inherit(statement);
    }

    /**
     * Parses the version details (common for group and export definitions).
     *
     * @param parser
     *            the parser to use. It must not be {@code null}.
     * @param statement
     *            the statement to fill with the data. It must not be
     *            {@code null}.
     *
     * @throws ParseException
     *             if the parsing fails
     */
    private void parseVersionDetails(LineParser parser, VersionStatement statement) throws ParseException {
        statement.constraint(parser.parseConstraint(() -> {
            return statement.constraint().map(Object::toString).orElse(null);
        }));

        statement.variance(parser.parseVariance(() -> {
            return statement.variance().map(Object::toString).map(String::toLowerCase).orElse(null);
        }));

        if (!parser.consumeTrailing()) { // Record the trailing part, hence it may just warn
            warn("Unknown construct found at the end of the line.");
        }
    }
}
