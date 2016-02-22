package net.yetamine.pet4bnd.model.format;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides a mutable value representation.
 */
final class ValueNode<T> implements Fragment, Supplier<T> {

    /** Formatter of the contained value. */
    private Function<? super T, String> formatter;
    /** Content of the node. */
    private T content;

    /**
     * Creates a new instance.
     */
    public ValueNode() {
        formatter = Object::toString;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return use().map(formatter).orElse("");
    }

    /**
     * Returns the value of the node.
     *
     * @see java.util.function.Supplier#get()
     */
    public T get() {
        return content;
    }

    /**
     * Returns the value of the node.
     *
     * @return the value of the node, or an empty container
     */
    public Optional<T> use() {
        return Optional.ofNullable(get());
    }

    /**
     * Sets the value of the node.
     *
     * @param value
     *            the value of the node
     *
     * @return this instance
     */
    public ValueNode<T> set(T value) {
        content = value;
        return this;
    }

    /**
     * Sets the formatter of the value.
     *
     * @param value
     *            the value to format. It must not be {@code null}.
     *
     * @return this instance
     */
    public ValueNode<T> formatter(Function<? super T, String> value) {
        formatter = Objects.requireNonNull(value);
        return this;
    }
}
