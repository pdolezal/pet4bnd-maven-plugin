package net.yetamine.pet4bnd.model;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A persistable object.
 *
 * @param <T>
 *            the type of the persisting interface
 */
public interface Persistable<T> {

    /**
     * Stores the encapsulated object in the given sink.
     *
     * @param sink
     *            the sink to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    void persist(T sink) throws IOException;

    /**
     * Stores the encapsulated object in the given file.
     *
     * @param path
     *            the path to store the data to. It must not be {@code null}.
     *
     * @throws IOException
     *             if storing the object fails
     */
    void store(Path path) throws IOException;
}
