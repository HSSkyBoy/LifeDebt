pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
	}
	plugins {
		id("fabric-loom") version providers.gradleProperty("loom_version").get()
		kotlin("jvm") version "2.4.10-RC"
	}
}

plugins {
	// Auto-provisions the Java 21 toolchain.
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

// Should match your modid
rootProject.name = "lifedebt"
