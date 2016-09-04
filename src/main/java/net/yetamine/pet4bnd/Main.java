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

package net.yetamine.pet4bnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.yetamine.pet4bnd.feedback.Feedback;
import net.yetamine.pet4bnd.format.Format2Bnd;
import net.yetamine.pet4bnd.format.Format2Map;
import net.yetamine.pet4bnd.model.LoggingResolver;
import net.yetamine.pet4bnd.model.VersionResolver;
import net.yetamine.pet4bnd.model.format.PetFormat;
import net.yetamine.pet4bnd.model.format.PetParser;
import net.yetamine.pet4bnd.support.Resource;
import net.yetamine.pet4bnd.version.Version;

/**
 * Implementation of the command line interface of the tool.
 */
public final class Main {

    /** Exit with success. */
    private static final int EXIT_SUCCESS = 0;
    /** Fail due to syntactic error of an argument. */
    private static final int EXIT_SYNTAX = 1;
    /** Fail due to an input processing. */
    private static final int EXIT_INPUT = 2;
    /** Fail due to an output error. */
    private static final int EXIT_OUTPUT = 3;

    /** Resource with the HELP content. */
    private static final Resource RESOURCE_HELP = new Resource("/module-resources/pet4bnd-help.txt");
    /** Resource with the MANIFEST of this archive. */
    private static final Resource RESOURCE_MANIFEST = new Resource("/META-INF/MANIFEST.MF");

    /** Default source path. */
    private static final Path DEFAULT_SOURCE = Paths.get("exports.pet");

    /**
     * Launches the command line version of the tool.
     *
     * @param args
     *            the command line arguments. It must not be {@code null}.
     *
     * @throws Exception
     *             if something goes wrong
     */
    public static void main(String... args) throws Exception {
        System.exit(main(Arrays.asList(args)));
    }

    /**
     * Launches the command line version of the tool.
     *
     * @param args
     *            the command line arguments. It must not be {@code null}.
     *
     * @return the return value
     *
     * @throws Exception
     *             if something goes wrong
     */
    public static int main(List<String> args) throws Exception {
        if (args.isEmpty()) {
            System.err.println("Missing arguments. Use --help to display details.");
            return EXIT_SYNTAX;
        }

        if ((args.size() == 1) && isHelpOption(args.get(0))) {
            printHelpContent();
            return EXIT_SUCCESS;
        }

        // Option properties
        boolean bundleVersion = false;
        boolean restore = false;
        boolean verbose = false;
        boolean report = false;
        boolean debug = false;

        Path petFile = null;
        Path bndFile = null;
        Path propertiesFile = null;

        // Parse arguments for the tool instance
        for (Iterator<String> it = args.iterator(); it.hasNext();) {
            final String option = it.next();

            try {
                if (isHelpOption(option)) {
                    throw new IllegalArgumentException("HELP options can't be used with other options.");
                }

                switch (option) {
                    case "-bundle-version":
                        bundleVersion = true;
                        break;

                    case "-debug":
                        debug = true;
                        break;

                    case "-pet":
                        petFile = Paths.get(it.next());
                        break;

                    case "-bnd":
                        bndFile = Paths.get(it.next());
                        break;

                    case "-properties":
                        propertiesFile = Paths.get(it.next());
                        break;

                    case "-report":
                        report = true;
                        break;

                    case "-restore":
                        restore = true;
                        break;

                    case "-verbose":
                        verbose = true;
                        break;

                    default:
                        throw new IllegalArgumentException(String.format("Unknown option '%s'.", option));
                }
            } catch (InvalidPathException e) {
                System.err.format("Unable to convert the argument to a file path.%n%s%n", e.getMessage());
                return EXIT_SYNTAX;
            } catch (NoSuchElementException e) {
                System.err.println(String.format("Missing argument for option '%s'.", option));
                return EXIT_SYNTAX;
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                return EXIT_SYNTAX;
            }
        }

        final Feedback feedback = newFeedback(debug);
        final PetFormat description;

        try { // Load the source
            if (petFile == null) {
                feedback.warn("No source file specified, trying to use the default.");
                petFile = DEFAULT_SOURCE;
            }

            feedback.info(String.format("Loading source file: %s", petFile));
            description = description(petFile, feedback);
        } catch (NoSuchFileException e) {
            feedback.fail("Missing source file.");
            return EXIT_INPUT;
        } catch (IOException e) {
            feedback.fail(e);
            return EXIT_INPUT;
        }

        final VersionResolver resolver = new LoggingResolver(description, feedback::fail);
        if (!resolver.resolve().test()) { // Resolve the versions and check they are valid
            feedback.fail("One or more version constraints were violated.");
            return EXIT_INPUT;
        }

        try { // Produce the output
            final Version version = description.version().resolution();

            if (bndFile != null) {
                feedback.info(String.format("Generating bnd file: %s", bndFile));
                final Format2Bnd format = new Format2Bnd(description, bundleVersion);
                format.store(bndFile);

                if (verbose) {
                    feedback.info("Package exports:");
                    format.exports().forEach(feedback::info);
                    feedback.info(""); // Empty line
                }
            }

            if (propertiesFile != null) {
                feedback.info(String.format("Generating properties file: %s", propertiesFile));
                final Format2Map format = new Format2Map(description);
                format.store(propertiesFile);

                if (verbose) { // Dump the result
                    feedback.info("Generated properties:");
                    format.content().forEach((n, v) -> feedback.info(String.format("%s = %s", n, v)));
                    feedback.info(""); // Empty line
                }
            }

            if (restore) {
                feedback.info("Restoring baselines and updating the source file.");
                description.restore();
                description.store(petFile);
            }

            if (report) {
                feedback.info("Dumping the target bundle version.");
                System.out.println(version);
            }
        } catch (IOException e) {
            feedback.fail(e);
            return EXIT_OUTPUT;
        }

        feedback.info("Done.");
        return EXIT_SUCCESS;
    }

    /**
     * Creates a new instance.
     *
     * @param source
     *            the source path. It must not be {@code null}.
     * @param feedback
     *            the feedback reporting interface. It must not be {@code null}.
     *
     * @throws IOException
     *             if the input processing failed
     */
    private static PetFormat description(Path source, Feedback feedback) throws IOException {
        final PetParser parser = PetFormat.parse(source, feedback);

        if (parser.errorCount() > 0) {
            throw new IOException("Errors encountered when parsing the definition file.");
        }

        if (parser.warningCount() > 0) {
            feedback.warn("Warnings encountered when parsing the definition file. Build might not be stable.");
        }

        return parser.result().orElseThrow(() -> {
            return new IOException("Failed to parse the definition file.");
        });
    }

    /**
     * Provides a feedback interface.
     *
     * @param debug
     *            {@code true} if exceptions should be dumped
     *
     * @return the feedback interface
     */
    private static Feedback newFeedback(boolean debug) {
        return new Feedback() {

            /**
             * @see net.yetamine.pet4bnd.feedback.Feedback#fail(java.lang.String,
             *      java.lang.Throwable)
             */
            public void fail(String message, Throwable t) {
                System.err.format("[ERROR] %s%n", Objects.requireNonNull(message));

                if (debug && (t != null)) {
                    System.err.println("[DEBUG] Error details:");
                    t.printStackTrace(System.err);
                }
            }

            /**
             * @see net.yetamine.pet4bnd.feedback.Feedback#warn(java.lang.String,
             *      java.lang.Throwable)
             */
            public void warn(String message, Throwable t) {
                System.err.format("[WARNING] %s%n", Objects.requireNonNull(message));

                if (debug && (t != null)) {
                    System.err.println("[DEBUG] Warning details:");
                    t.printStackTrace(System.err);
                }
            }

            /**
             * @see net.yetamine.pet4bnd.feedback.Feedback#info(java.lang.String)
             */
            public void info(String message) {
                System.err.println(Objects.requireNonNull(message));
            }
        };
    }

    // Help support

    /**
     * Indicates if the argument is a HELP option.
     *
     * @param argument
     *            the argument to check. It must not be {@code null}.
     *
     * @return {@code true} if the option is a HELP option
     */
    private static boolean isHelpOption(String argument) {
        switch (argument.toLowerCase()) {
            case "--help":
            case "-help":
            case "/help":
            case "help":
            case "-h":
            case "-?":
            case "/?":
            case "/h":
                return true;

            default:
                return false;
        }
    }

    /**
     * Print the help.
     */
    private static void printHelpContent() {
        final PrintStream out = System.out;

        try (InputStream is = RESOURCE_MANIFEST.inputStream()) {
            out.println("Package exports tracker for bnd");
            out.println("-------------------------------");
            out.println("A tool for generating bnd files");
            final Attributes attributes = new Manifest(is).getMainAttributes();
            final String product = attributes.getValue("Implementation-Title");
            final String version = attributes.getValue("Implementation-Version");
            final String vendor = attributes.getValue("Implementation-Vendor-Id");
            out.format("%s:%s:%s%n%n", vendor, product, version);
        } catch (NullPointerException | IOException e) {
            assert false; // Ignore missing manifest
        }

        try (BufferedReader reader = RESOURCE_HELP.bufferedReader(StandardCharsets.UTF_8)) {
            for (String line; (line = reader.readLine()) != null;) {
                out.println(line);
            }
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Missing HELP resource.", e);
        }
    }

    private Main() {
        throw new AssertionError();
    }
}
