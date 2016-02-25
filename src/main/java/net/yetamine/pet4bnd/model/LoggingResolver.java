package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The default resolving strategy providing full feedback about the process.
 */
public final class LoggingResolver extends VersionResolver {

    /** Feedback for logging problems. */
    private final Consumer<? super String> feedback;

    /**
     * Creates a new instance.
     *
     * @param resolutionTarget
     *            the bundle to resolve. It must not be {@code null}.
     * @param resolutionFeedback
     *            the feedback interface to use. It must not be {@code null}.
     */
    public LoggingResolver(Bundle resolutionTarget, Consumer<? super String> resolutionFeedback) {
        super(resolutionTarget);
        feedback = Objects.requireNonNull(resolutionFeedback);
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionResolver#constraintViolated(net.yetamine.pet4bnd.model.PackageExport)
     */
    @Override
    protected boolean constraintViolated(PackageExport export) {
        final PackageVersion version = export.version();
        final String f = "Package '%s': target version %s violates version constraint %s.";
        feedback.accept(String.format(f, export.packageName(), version.resolution(), version.constraint()));
        return false;
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionResolver#constraintViolated()
     */
    @Override
    protected void constraintViolated() {
        final BundleVersion version = bundle().version();
        final String f = "Target bundle version %s violates version restriction to %s.";
        feedback.accept(String.format(f, version.resolution(), version.constraint()));
    }

}
