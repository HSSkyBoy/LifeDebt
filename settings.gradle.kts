pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}
}

plugins {
	// https://stonecutter.kikugie.dev/
	// NOTE: Stonecutter 0.8+ hard-requires Gradle 9; 0.7.x is the newest line that runs on Gradle 8.14.4.
	id("dev.kikugie.stonecutter") version "0.7.11"
	// Auto-provisions the single Java 21 toolchain used by every supported version.
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

stonecutter {
	create(rootProject) {
		versions("1.20.5", "1.20.6", "1.21.1", "1.21.4", "1.21.8", "1.21.11")
		// The current stable node is the development baseline. Older nodes only receive
		// compatibility fixes; new mechanics are authored against this modern API surface.
		vcsVersion = "1.21.8"
	}
}

// Should match your modid
rootProject.name = "lifedebt"
