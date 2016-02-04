package net.yetamine.pet4bnd.demo.greeting;

/**
 * A greeting facility.
 */
public final class Greeting {

    /**
     * Returns a greeting from a list of arguments.
     *
     * @param args
     *     the arguments to use. It must not be {@code null}.
     *
     * @return the greeting
     */
    public static String from(String... args) {
        final StringBuilder result = new StringBuilder();
        for (int i = args.length; i-- > 0; ) {
            result.append(args[i]);
        }
        
        return result.toString();
    }
    
    private Greeting() {
        throw new AssertionError();
    }
}
