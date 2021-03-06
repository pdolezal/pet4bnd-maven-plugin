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

package net.yetamine.pet4bnd.mojo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.model.Bundle;
import net.yetamine.pet4bnd.model.LoggingResolver;
import net.yetamine.pet4bnd.model.format.PetFormat;
import net.yetamine.pet4bnd.model.format.PetParser;

/**
 * A base for Mojo implementations with common utilities.
 */
public abstract class AbstractPet4BndMojo extends AbstractMojo {

    /** Project for this instance. */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /** Parser feedback instance. */
    private Feedback feedback;

    /**
     * Prepares a new instance.
     */
    protected AbstractPet4BndMojo() {
        // Default constructor
    }

    // Mojo interface

    /**
     * Provides the project.
     *
     * @return the project
     */
    public final MavenProject getProject() {
        return project;
    }

    /**
     * Returns the default source path.
     *
     * @return the default source path
     */
    public final Path getDefaultSourcePath() {
        return project.getBasedir().toPath().resolve("exports.pet");
    }

    // General utilities

    /**
     * Makes a {@link Path} instance from the given path string.
     *
     * @param path
     *            the path to convert. It must not be {@code null}.
     *
     * @return the {@link Path} instance
     *
     * @throws MojoExecutionException
     *             if the path is invalid
     */
    protected static Optional<Path> resolvePath(String path) throws MojoExecutionException {
        try {
            return Optional.ofNullable(path).map(Paths::get);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(String.format("Invalid path specified: %s", path), e);
        }
    }

    /**
     * Makes a {@link Path} instance from the given path string.
     *
     * @param path
     *            the path to convert. It must not be {@code null}.
     *
     * @return the {@link Path} instance
     *
     * @throws MojoExecutionException
     *             if the path is invalid or missing
     */
    protected static Path requirePath(String path) throws MojoExecutionException {
        try {
            return Optional.ofNullable(path).map(Paths::get).orElseThrow(() -> {
                return new MojoExecutionException("Path missing.");
            });
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(String.format("Invalid path specified: %s", path), e);
        }
    }

    /**
     * Provides the feedback callback that uses the Mojo's log.
     *
     * @return the feedback callback
     */
    protected final Feedback getFeedback() {
        if (feedback == null) {
            feedback = new Feedback() {

                /**
                 * @see net.yetamine.pet4bnd.feedback.Feedback#fail(java.lang.String,
                 *      java.lang.Throwable)
                 */
                public void fail(String message, Throwable t) {
                    getLog().error(Objects.requireNonNull(message), t);
                }

                /**
                 * @see net.yetamine.pet4bnd.feedback.Feedback#warn(java.lang.String,
                 *      java.lang.Throwable)
                 */
                public void warn(String message, Throwable t) {
                    getLog().warn(Objects.requireNonNull(message), t);
                }

                /**
                 * @see net.yetamine.pet4bnd.feedback.Feedback#info(java.lang.String)
                 */
                public void info(String message) {
                    getLog().info(Objects.requireNonNull(message));
                }
            };
        }

        return feedback;
    }

    /**
     * Parses the specified definition file.
     *
     * @param path
     *            the path to the file. It must not be {@code null}.
     *
     * @return the definition
     *
     * @throws MojoExecutionException
     *             if there is any error
     */
    protected final PetFormat parseSource(Path path) throws MojoExecutionException {
        try {
            final Feedback report = getFeedback();
            final PetParser parser = PetFormat.parse(path, report);

            if (parser.errorCount() > 0) {
                throw new MojoExecutionException("Errors encountered when parsing the definition file.");
            }

            if (parser.warningCount() > 0) {
                report.warn("Warnings encountered when parsing the definition file. Build might not be stable.");
            }

            return parser.result().orElseThrow(() -> {
                return new MojoExecutionException("Failed to parse the definition file.");
            });
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Unable to read the definition file: %s", path), e);
        }
    }

    /**
     * Performs the complete resolution.
     *
     * @param <T>
     *            the type of the result
     * @param definition
     *            the definition to resolve. It must not be {@code null}.
     *
     * @return the definition
     *
     * @throws MojoExecutionException
     *             if a fatal error occurs and the resolution is not valid
     */
    protected final <T extends Bundle> T resolveDefinition(T definition) throws MojoExecutionException {
        if (new LoggingResolver(definition, getFeedback()::fail).resolve().test()) {
            return definition;
        }

        throw new MojoExecutionException("One or more version constraints were violated.");
    }
}
