buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        // This makes the plugin available to all modules manually
        classpath(libs.resources.generator)
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

// Kover only instruments JVM/Android host tests; Kotlin/Native (iOS) coverage comes from xccov in the iOS workflow.
dependencies {
    kover(project(":shared"))
    kover(project(":networking"))
    kover(project(":composeApp"))
}

kover {
    reports {
        filters {
            excludes {
                // Generated code
                classes(
                    "*_Impl",
                    "*_Impl\$*",
                    "*.ComposableSingletons\$*",
                    "*.MR",
                    "*.MR\$*",
                    "*.BuildConfig",
                    "*.R",
                    "*.R\$*",
                )
                // Declarative-only layers with no branching logic to assert on
                packages(
                    "com.dnfapps.arrmatey.di",
                    "com.dnfapps.arrmatey.ui",
                    "com.dnfapps.arrmatey.database.migrations",
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }

        total {
            html { onCheck = false }
            xml { onCheck = false }
        }
    }
}

val ktlintVersion = libs.versions.ktlint.get()

allprojects {
    apply(
        plugin =
            rootProject.libs.plugins.spotless
                .get()
                .pluginId,
    )

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**", "**/generated/**")
            ktlint(ktlintVersion)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion)
        }
    }
}
