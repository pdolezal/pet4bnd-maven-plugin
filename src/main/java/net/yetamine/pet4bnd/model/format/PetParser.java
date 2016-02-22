package net.yetamine.pet4bnd.model.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.model.PackageExport;
import net.yetamine.pet4bnd.model.VersionStatement;
import net.yetamine.pet4bnd.version.Version;
import net.yetamine.pet4bnd.version.VersionVariance;

/**
 * A parser for the {@link PetFormat} class.
 */
public final class PetParser implements Consumer<CharSequence> {

    /** Name of the option for bundle version. */
    private static final String BUNDLE_OPTION_VERSION = "$bundle-version";

    /** Full line representation to reconstruct the original. */
    private final List<LineNode> representation = new ArrayList<>();
    /** Parsed package exports (except for the pending one). */
    private final Map<String, PackageExport> bundleExports = new TreeMap<>();
    /** Parsed bundle version statement. */
    private VersionDefinition bundleVersion;

    /** Export definition waiting for completion. */
    private VersionDefinition pendingExport;

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

        if (bundleVersion == null) {
            bundleVersion = new VersionDefinition().baseline(Version.ZERO);
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
    VersionStatement bundleVersion() {
        return bundleVersion;
    }

    // Parsing core

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

        // Open a new definition, which shall become the pending one
        final VersionDefinition definition = new VersionDefinition();

        try {
            // Match the definition name
            final Matcher name = PATTERN_DEFINITION_NAME.matcher(line);

            if (!name.find()) { // Name malformed or not a valid definition line at all
                throw new IllegalArgumentException("Unacceptable definition name.");
            }

            final LineNode lineNode = new LineNode();
            definition.identifier(name.group("value"));
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

            definition.baseline(Version.valueOf(baseline.group("value")));
            lineNode.append(definition.baselineNode());
            position = baseline.end();

            { // Match the version constraint
                final ValueNode<Version> node = definition.constraintNode();
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
                final ValueNode<VersionVariance> node = definition.varianceNode();
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
        if (BUNDLE_OPTION_VERSION.equals(definition.identifier())) {
            if (bundleVersion != null) { // Must be unique!
                warn("Bundle version duplicated. Using only the first occurrence.", null);
                return;
            }

            bundleVersion = definition;
            return;
        }

        createPendingExport(definition);
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
     * Records the value as a pending export.
     *
     * @param value
     *            the value to record. It must not be {@code null}.
     */
    private void createPendingExport(VersionDefinition value) {
        assert (pendingExport == null);
        pendingExport = value;
        assert (pendingExport != null);
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
        if (pendingExport == null) {
            return false;
        }

        final VersionDefinition export = pendingExport;
        pendingExport = null; // Clean before further processing

        // Check the name availability
        final String name = export.identifier();
        if (bundleExports.containsKey(name)) {
            final String f = "Duplicated definition for '%s'. Using only the first occurrence.";
            warn(String.format(f, name), null);
            return true;
        }

        // Record the export
        final PackageExport packageExport = new PetExport(export, attributes);
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
}
