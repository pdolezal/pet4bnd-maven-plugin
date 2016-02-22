package net.yetamine.pet4bnd.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A persistable object.
 */
public interface Persistable {

    /**
     * Stores the encapsulated object in the given sink.
     *
     * @param sink
     *            the sink to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    void persist(OutputStream sink) throws IOException;

    /**
     * Stores the encapsulated object in the given file.
     *
     * @param path
     *            the path to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    default void store(Path path) throws IOException {
        try (OutputStream sink = Files.newOutputStream(path)) {
            persist(sink);
        }
    }
}
