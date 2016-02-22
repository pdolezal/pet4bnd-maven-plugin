package net.yetamine.pet4bnd.model.format;

import java.util.Objects;

/**
 * Provides a constant string representation.
 */
final class StringNode implements Fragment {

    /** Content of the node. */
    private final String content;

    /**
     * Creates a new instance.
     *
     * @param value
     *            the content of the node. It must not be {@code null}.
     */
    public StringNode(String value) {
        content = Objects.requireNonNull(value);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return content;
    }
}
