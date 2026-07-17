plugins {
	id("dev.kikugie.stonecutter")
	// Declared here with `apply false` so every version node resolves the same Loom version.
	// NOTE: Loom 1.14+ requires Gradle 9 (plugin api-version 9.2+); 1.13.6 is the newest release built for Gradle 8.14.
	id("fabric-loom") version "1.13.6" apply false
}

stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// Builds every registered version and collects the jars into `build/libs/{mod_version}/`.
tasks.register("chiseledBuild") {
	group = "project"
	description = "Builds all Minecraft versions and collects the jars into build/libs/{mod_version}/"
	dependsOn(stonecutter.tasks.named("buildAndCollect"))
}
