package net.yetamine.pet4bnd.feedback;

/**
 * A feedback reporting interface.
 */
public interface Feedback {

    /**
     * Reports an error.
     *
     * @param message
     *            the message. It must not be {@code null}.
     * @param t
     *            the related exception if available
     */
    void fail(String message, Throwable t);

    /**
     * Reports an error.
     *
     * @param message
     *            the message. It must not be {@code null}.
     */
    default void fail(String message) {
        fail(message, null);
    }

    /**
     * Reports an error.
     *
     * @param t
     *            the related exception. It must not be {@code null}.
     */
    default void fail(Throwable t) {
        fail(t.getMessage(), t);
    }

    /**
     * Reports a warning.
     *
     * @param message
     *            the message. It must not be {@code null}.
     * @param t
     *            the related exception if available
     */
    void warn(String message, Throwable t);

    /**
     * Reports a warning.
     *
     * @param t
     *            the related exception. It must not be {@code null}.
     */
    default void warn(Throwable t) {
        warn(t.getMessage(), t);
    }

    /**
     * Reports a warning.
     *
     * @param message
     *            the message. It must not be {@code null}.
     */
    default void warn(String message) {
        warn(message, null);
    }

    /**
     * Reports an informational message.
     *
     * @param message
     *            the message. It must not be {@code null}.
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
