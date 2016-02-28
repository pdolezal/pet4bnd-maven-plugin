package net.yetamine.pet4bnd.model.format;

/**
 * Represents a formattable text fragment.
 */
interface TextFragment {

    /**
     * Formats the content of this instance to a string value.
     *
     * @return the formatted content, or {@code null} if no formatting possible
     *         or the result shall be omitted
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
    static String toString(TextFragment value) {
        if (value != null) {
            final String result = value.format();
            if (result != null) {
                return result;
            }
        }

        return "";
    }
}
