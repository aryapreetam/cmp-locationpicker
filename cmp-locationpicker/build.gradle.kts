@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.dokka)
}

kotlin {
  jvmToolchain(17)

  androidLibrary {
    namespace = "io.github.aryapreetam.cmplocationpicker"
    compileSdk = 35
    minSdk = 23
    withHostTest {  }
    androidResources {
      enable = true
    }
  }
  jvm()
  wasmJs {
    browser()
  }
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui.multiplatform)
      implementation(libs.compose.foundation)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.core)

      // WebView (Android/iOS/JVM/WASM) for rendering controlled `htmlContent`.
      implementation("io.github.aryapreetam:cmp-webview:0.0.3")
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }

    androidMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }

    jvmMain.dependencies {
      implementation(libs.ktor.client.java)
    }

    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }

    wasmJsMain.dependencies {
      implementation(libs.ktor.client.wasm)
    }

  }

  //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
  targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
    compilations["main"].compileTaskProvider.configure {
      compilerOptions {
        freeCompilerArgs.add("-Xexport-kdoc")
      }
    }
  }

}

// NOTE: Host-specific dependency leakage guardrail:
// DO NOT import host-specific binary dependencies (e.g. `compose.desktop.currentOs`) under library targets.
// Any desktop UI implementation should target standard platform-agnostic `jvm()` targets.
// Platform-specific runtime locators must be restricted solely to the executable sample application (:sample).

dependencies {
  dokkaPlugin(libs.android.documentation.plugin)
}

// Configure Dokka V2 extension.
dokka {
  moduleName.set("cmp-locationpicker")

  dokkaPublications.html {
    suppressObviousFunctions.set(false)
    suppressInheritedMembers.set(false)
  }

  dokkaSourceSets.configureEach {
    // Entry docs.
    includes.from("src/commonMain/kotlin/Module.md")
    includes.from("src/commonMain/kotlin/io/github/aryapreetam/cmplocationpicker/package.md")
    includes.from("src/commonMain/kotlin/io/github/aryapreetam/cmplocationpicker/ui/package.md")
    includes.from("src/commonMain/kotlin/io/github/aryapreetam/cmplocationpicker/provider/package.md")

    // Source links.
    sourceLink {
      localDirectory.set(file("src"))
      remoteUrl("https://github.com/aryapreetam/cmp-locationpicker/tree/main/lib/src")
      remoteLineSuffix.set("#L")
    }

    // Suppress internal packages.
    perPackageOption {
      matchingRegex.set(".*\\.internal.*")
      suppress.set(true)
    }

    perPackageOption {
      matchingRegex.set("io.github.aryapreetam.cmplocationpicker")
      reportUndocumented.set(true)
      skipDeprecated.set(false)
    }
  }
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
  publishToMavenCentral()
  coordinates(
      project.group.toString(),
      findProperty("libArtifactId")?.toString() ?: "cmp-locationpicker",
      project.version.toString()
  )

  pom {
    name = "cmp-locationpicker"
    description = "Location picker dialog for Compose Multiplatform (Leaflet + OpenStreetMap + Nominatim in v0)"
    url = "https://github.com/aryapreetam/cmp-locationpicker"

    licenses {
      license {
        name = "MIT"
        url = "https://opensource.org/licenses/MIT"
      }
    }

    developers {
      developer {
        id = "aryapreetam"
        name = "Preetam Bhosle"
      }
    }

    scm {
      url.set("https://github.com/aryapreetam/cmp-locationpicker")
      // Maven Central validation commonly expects SCM connection fields.
      connection.set("scm:git:https://github.com/aryapreetam/cmp-locationpicker.git")
      developerConnection.set("scm:git:ssh://git@github.com/aryapreetam/cmp-locationpicker.git")
      tag.set("HEAD")
    }
  }
  // Sign publications if either local keyId or CI signingInMemoryKey is available
  if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")) {
    signAllPublications()
  }
}
