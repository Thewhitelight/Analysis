package cn.libery.analysis.trace

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class Trace implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("=== Trace Plugin ===")
        project.extensions.create('Trace', TraceExtension)

        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new TraceTransform(project))
    }

}