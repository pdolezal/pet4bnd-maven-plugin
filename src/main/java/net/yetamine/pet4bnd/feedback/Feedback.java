package net.yetamine.pet4bnd.feedback;

/**
 * A feedback reporting interface.
 */
public interface Feedback {

    /**
     * Reports an error.
     *
     * @param message
     *            the message
     * @param t
     *            the related exception if available
     */
    void fail(String message, Throwable t);

    /**
     * Reports an error.
     *
     * @param message
     *            the message
     */
    default void fail(String message) {
        fail(message, null);
    }

    /**
     * Reports a warning.
     *
     * @param message
     *            the message
     * @param t
     *            the related exception if available
     */
    void warn(String message, Throwable t);

    /**
     * Reports a warning.
     *
     * @param message
     *            the message
     */
    default void warn(String message) {
        warn(message, null);
    }

    /**
     * Reports an informational message.
     *
     * @param message
     *            the message
     */
    void info(String message);

    /**
     * Provides a nothing-doing instance.
     *
     * @return a nothing-doing instance
     */
    static Feedback none() {
        return NoFeedback.INSTANCE;
    }
}
