package net.yetamine.pet4bnd.model.format;

/**
 * Represents a formattable text fragment.
 */
interface Fragment {

    /**
     * Formats the content of this instance to a string value.
     *
     * @return the formatted content, or an empty string if no suitable content
     *         is available
     */
    String format();

    /**
     * Formats the content of the given value to a string value.
     *
     * @param value
     *            the value to format
     *
     * @return the result of the formatting, an empty string if the value is
     *         {@code null}
     */
    static String format(Fragment value) {
        return (value != null) ? value.format() : "";
    }
}
