package net.yetamine.pet4bnd.feedback;

/**
 * A nothing-doing implementation of the parser feedback interface.
 */
enum NoFeedback implements Feedback {

    /** Sole instance of this feedback. */
    INSTANCE;

    /**
     * @see net.yetamine.pet4bnd.feedback.Feedback#fail(java.lang.String,
     *      java.lang.Throwable)
     */
    public void fail(String message, Throwable t) {
        // Do nothing
    }

    /**
     * @see net.yetamine.pet4bnd.feedback.Feedback#warn(java.lang.String,
     *      java.lang.Throwable)
     */
    public void warn(String message, Throwable t) {
        // Do nothing
    }

    /**
     * @see net.yetamine.pet4bnd.feedback.Feedback#info(java.lang.String)
     */
    public void info(String message) {
        // Do nothing
    }
}