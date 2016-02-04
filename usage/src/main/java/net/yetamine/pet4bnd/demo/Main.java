package net.yetamine.pet4bnd.demo;

import net.yetamine.pet4bnd.demo.greeting.Greeting;
import net.yetamine.pet4bnd.demo.messages.Messages;

/**
 * A main class.
 */
public final class Main {

    /**
     * Launches the application.
     *
     * @param args
     *            the command line arguments. It must not be {@code null}.
     */
    public static void main(String... args) {
        Messages.display(Greeting.from(args));
    }
    
    private Main() {
        throw new AssertionError();
    }
}
