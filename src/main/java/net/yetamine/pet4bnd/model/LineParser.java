package net.yetamine.pet4bnd.model;

import java.util.Optional;
import java.util.function.Consumer;

import net.yetamine.pet4bnd.feedback.Feedback;

/**
 * A parser interface.
 *
 * @param <T>
 *            the type of the parsing result
 */
public interface LineParser<T> extends Consumer<CharSequence> {

    /**
     * Sets the feedback interface.
     *
     * @param value
     *            the instance to set. It must not be {@code null}.
     *
     * @return this instance
     */
    LineParser<T> feedback(Feedback value);

    /**
     * Returns the current feedback interface.
     *
     * @return the current feedback interface
     */
    Feedback feedback();

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
    void accept(CharSequence line);

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
    LineParser<T> finish();

    /**
     * Indicates whether the parser finished parsing (i.e., {@link #finish()}
     * has been called).
     *
     * @return {@code true} if the parser is finished parsing
     */
    boolean finished();

    /**
     * Provides the result of the parsing.
     *
     * @return the result of the parsing, or an empty container if no result is
     *         available (perhaps when still parsing)
     */
    Optional<T> result();

    /**
     * Returns the number of warnings so far.
     *
     * @return the number of warnings so far
     */
    int warningCount();

    /**
     * Returns the number of errors so far.
     *
     * @return the number of errors so far
     */
    int errorCount();
}
