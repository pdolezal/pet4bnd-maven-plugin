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
    String toString();
}
