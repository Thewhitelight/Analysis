package cn.libery.analysis.track

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class Track implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("=== Track Plugin ===")

        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        final def log = project.logger
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        project.extensions.create('track', TrackExtension)

        project.dependencies {
            debugImplementation 'cn.libery.analysis:runtime:1.0.2'
            debugImplementation 'org.aspectj:aspectjrt:1.8.9'
        }

        // add the versionName & versionCode to the apk file name
        variants.all { variant ->

            if (!variant.buildType.isDebuggable()) {
                log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
                return
            } else if (!project.track.enabled) {
                log.debug("Hugo is not disabled.")
                return
            }

            def fullName = ""
            variant.name.tokenize('-').eachWithIndex { token, index ->
                fullName = fullName + (index == 0 ? token : token.capitalize())
            }

            JavaCompile javaCompile = variant.javaCompiler
            MessageHandler handler = new MessageHandler(true)
            javaCompile.doLast {
                String[] javaArgs = ["-showWeaveInfo",
                                     "-1.8",
                                     "-inpath", javaCompile.destinationDir.toString(),
                                     "-aspectpath", javaCompile.classpath.asPath,
                                     "-d", javaCompile.destinationDir.toString(),
                                     "-classpath", javaCompile.classpath.asPath,
                                     "-bootclasspath", project.android.bootClasspath.join(
                        File.pathSeparator)]

                String[] kotlinArgs = ["-showWeaveInfo",
                                       "-1.8",
                                       "-inpath", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                       "-aspectpath", javaCompile.classpath.asPath,
                                       "-d", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                       "-classpath", javaCompile.classpath.asPath,
                                       "-bootclasspath", project.android.bootClasspath.join(
                        File.pathSeparator)]

                new Main().run(javaArgs, handler)
                new Main().run(kotlinArgs, handler)

                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break
                        case IMessage.WARNING:
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break
                    }
                }
            }
        }
    }

}