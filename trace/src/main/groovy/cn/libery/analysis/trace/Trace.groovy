package cn.libery.analysis.trace

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class Trace implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)

        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        final def log = project.logger
        variants.all { variant ->
            if (!variant.buildType.isDebuggable()) {
                log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
                return
            } else if (!project.Trace.enabled) {
                log.debug("Trace is not disabled.")
                return
            }
        }

        project.extensions.create('Trace', TraceExtension)

        println("=== Trace Plugin ===")
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new TraceTransform(project))
    }

}