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
	// Auto-provisions the JDKs required by the per-version Java toolchains (8/16/17/21)
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

stonecutter {
	create(rootProject) {
		versions(
			"1.14.4", "1.16.5", "1.17.1", "1.18.2",
			"1.19.2", "1.19.4", "1.20.1", "1.20.4",
			"1.20.6", "1.21.1", "1.21.4", "1.21.8"
		)
		// The version committed to VCS; also the active development version.
		vcsVersion = "1.20.1"
	}
}

// Should match your modid
rootProject.name = "lifedebt"
