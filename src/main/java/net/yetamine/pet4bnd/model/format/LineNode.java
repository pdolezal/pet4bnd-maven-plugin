package net.yetamine.pet4bnd.model.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides a line representation which consists of multiple fragments.
 */
final class LineNode implements Fragment {

    /** Fragments of this line. */
    private final List<Fragment> fragments = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public LineNode() {
        // Default constructor
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        switch (fragments.size()) {
            // @formatter:off
            case 0 : return "";
            case 1 : return Fragment.format(fragments.get(0));
            // @formatter:on
        }

        return fragments.stream().map(f -> Fragment.format(f)).collect(Collectors.joining());
    }

    /**
     * @see net.yetamine.pet4bnd.model.format.Fragment#format()
     */
    public String format() {
        return toString();
    }

    /**
     * Appends a fragment.
     *
     * @param fragment
     *            the fragment to append. It must not be {@code null}.
     *
     * @return this instance
     */
    public LineNode append(Fragment fragment) {
        fragments.add(fragment);
        return this;
    }

    /**
     * Appends a fragment.
     *
     * @param fragment
     *            the fragment to append. It must not be {@code null}.
     *
     * @return this instance
     */
    public LineNode append(String fragment) {
        Objects.requireNonNull(fragment);
        return append(() -> fragment);
    }

    /**
     * Provides the list of fragments of this line.
     *
     * <p>
     * The provided list be modified to change the line representation, however,
     * {@code null} values are not welcome and might be rejected.
     *
     * @return the list of fragments of this line
     */
    public List<Fragment> fragments() {
        return fragments;
    }
}
