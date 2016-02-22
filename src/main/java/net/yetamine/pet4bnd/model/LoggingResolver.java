package net.yetamine.pet4bnd.model;

import java.util.Objects;
import java.util.function.Consumer;

import net.yetamine.pet4bnd.version.Version;

/**
 * The default resolving strategy providing full feedback about the process.
 */
public final class LoggingResolver extends VersionResolver {

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
     * @see net.yetamine.pet4bnd.model.VersionResolver#fail(net.yetamine.pet4bnd.model.Bundle,
     *      net.yetamine.pet4bnd.version.Version)
     */
    @Override
    protected void fail(Bundle targetBundle, Version targetVersion) {
        final String f = "Target bundle version %s violates version restriction to %s.";
        feedback.accept(String.format(f, targetVersion, targetBundle.version().constraint().get()));
    }

    /**
     * @see net.yetamine.pet4bnd.model.VersionResolver#fail(net.yetamine.pet4bnd.model.PackageExport,
     *      net.yetamine.pet4bnd.version.Version)
     */
    @Override
    protected boolean fail(PackageExport export, Version exportVersion) {
        final String f = "Package '%s': target version %s violates version constraint %s.";
        final Version constraint = export.version().constraint().get();
        feedback.accept(String.format(f, export.packageName(), exportVersion, constraint));
        return false;
    }
}
