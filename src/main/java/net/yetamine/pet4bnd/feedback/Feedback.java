/*
 * Copyright 2016 Yetamine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
