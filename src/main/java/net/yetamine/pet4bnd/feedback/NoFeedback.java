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