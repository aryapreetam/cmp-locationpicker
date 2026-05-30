rootProject.name = "cmp-locationpicker-root"

// Composite build: consume local `cmp-webview` during development.
// We add explicit dependency substitution so `io.github.aryapreetam:cmp-webview:<version>`
// resolves to the included build's `:cmp-locationpicker` project without requiring publishing.
// includeBuild("../cmp-webview") {
//   dependencySubstitution {
//     substitute(module("io.github.aryapreetam:cmp-webview")).using(project(":cmp-webview"))
//   }
// }



pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
        includeGroupByRegex("android.*")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
        includeGroupByRegex("android.*")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenCentral()
    mavenLocal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":cmp-locationpicker")
include(":sample:composeApp")
include(":sample:androidApp")

