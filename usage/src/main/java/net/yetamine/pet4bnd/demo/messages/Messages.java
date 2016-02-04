package net.yetamine.pet4bnd.demo.messages;

/**
 * Message utilities.
 */
public final class Messages {

    /**
     * Displays a message.
     *
     * @param message
     *     the message to display. It must not be {@code null}.
     */
    public static void display(String message) {
        System.out.println(message);
    }
    
    private Messages() {
        throw new AssertionError();
    }
}
