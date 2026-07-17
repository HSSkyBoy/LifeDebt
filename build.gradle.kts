plugins {
	id("fabric-loom")
	`maven-publish`
}

// Minecraft version of the node this build instance is evaluated for (e.g. "1.20.1").
val mcVersion: String = stonecutter.current.version

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String
base.archivesName = "lifedebt"

// Java version required by each Minecraft version range.
val requiredJava: JavaVersion = when {
	stonecutter.eval(mcVersion, ">=1.20.6") -> JavaVersion.VERSION_21
	stonecutter.eval(mcVersion, ">=1.18") -> JavaVersion.VERSION_17
	stonecutter.eval(mcVersion, ">=1.17") -> JavaVersion.VERSION_16
	else -> JavaVersion.VERSION_1_8
}

loom {
	// No splitEnvironmentSourceSets(): it requires a bundled server jar (MC 1.18+) and
	// breaks configuration on 1.16.5-1.17. The mod has no client-only code, so a single
	// merged `main` source set works across the whole 1.16.5-1.21.8 range.
	mods {
		create("lifedebt") {
			sourceSet(sourceSets["main"])
		}
	}
}

dependencies {
	// Versioned values come from `versions/{mcVersion}/gradle.properties`.
	minecraft("com.mojang:minecraft:$mcVersion")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	withSourcesJar()

	sourceCompatibility = requiredJava
	targetCompatibility = requiredJava

	toolchain {
		languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

val modVersion = version.toString()
// Resolved at project scope: inside the buildAndCollect Copy lambda the receiver is the task,
// so a bare property("mod_version") there resolves against the task and fails.
val modVersionRaw = property("mod_version") as String
val mixinJavaLevel = "JAVA_${requiredJava.majorVersion}"
tasks.processResources {
	inputs.property("version", modVersion)
	inputs.property("minecraft_version", mcVersion)

	// Fabric API's aggregate mod id is `fabric` on <1.18 and `fabric-api` from 1.18 onward;
	// depending on the wrong id makes Fabric Loader reject the mod even when the API is installed.
	val fabricApiId = if (stonecutter.eval(mcVersion, "<1.18")) "fabric" else "fabric-api"
	// Each representative build covers a whole API-compatible range, so one jar works across
	// every Minecraft version in its era. Ranges are contiguous and span 1.16.5–1.21.11.
	val minecraftRange = when (mcVersion) {
		"1.16.5"  -> ">=1.16.5 <1.17"
		"1.17.1"  -> ">=1.17 <1.18"
		"1.19.2"  -> ">=1.18 <1.19.3"
		"1.19.3"  -> ">=1.19.3 <1.19.4"
		"1.20.1"  -> ">=1.19.4 <1.20.2"
		"1.20.4"  -> ">=1.20.2 <1.20.5"
		"1.20.6"  -> ">=1.20.5 <1.21"
		"1.21.1"  -> ">=1.21 <1.21.2"
		"1.21.4"  -> ">=1.21.2 <1.21.5"
		"1.21.5"  -> ">=1.21.5 <1.21.6"
		"1.21.8"  -> ">=1.21.6 <1.21.9"
		"1.21.11" -> ">=1.21.9 <1.22"
		else      -> "~$mcVersion"
	}
	filesMatching("fabric.mod.json") {
		expand(
			"version" to modVersion,
			"minecraft_version" to mcVersion,
			"minecraft_range" to minecraftRange,
			"java_version" to requiredJava.majorVersion,
			"fabric_api_id" to fabricApiId,
		)
	}

	// The mixin JSON hard-codes "JAVA_17"; each Minecraft version compiles to a different
	// bytecode level (Java 8/16/17/21), and Mixin's compatibilityLevel must be >= that level.
	// Rewrite it per version at resource-processing time.
	inputs.property("mixinJavaLevel", mixinJavaLevel)
	filesMatching("lifedebt.mixins.json") {
		filter { line -> line.replace("\"JAVA_17\"", "\"$mixinJavaLevel\"") }
	}

	// 1.21 (24w21a) singularized data pack sub-folders: `advancements` -> `advancement`.
	// A single source file lives under `data/*/advancements/`; relocate it at resource-processing
	// time for >=1.21 so the advancement actually loads on those versions.
	inputs.property("singularAdvancementFolder", stonecutter.eval(mcVersion, ">=1.21"))
	if (stonecutter.eval(mcVersion, ">=1.21")) {
		filesMatching("data/**/advancements/**") {
			relativePath = org.gradle.api.file.RelativePath(
				true,
				*relativePath.segments.map { if (it == "advancements") "advancement" else it }.toTypedArray()
			)
		}
	}
}

tasks.jar {
	// LICENSE lives in the repository root, not in the version node's project directory.
	from(rootProject.file("LICENSE")) {
		rename { "${it}_lifedebt" }
	}
}

// Builds this version and copies the resulting jars into `build/libs/{mod_version}/`
// at the repository root. Used by the root `chiseledBuild` task.
tasks.register<Copy>("buildAndCollect") {
	group = "build"
	description = "Builds the mod jar and copies it into the root build/libs/{mod_version}/ directory"
	dependsOn(tasks.build)
	from(tasks.remapJar.map { it.archiveFile }, tasks.remapSourcesJar.map { it.archiveFile })
	into(rootProject.layout.buildDirectory.dir("libs/$modVersionRaw"))
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = "lifedebt"
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
	}
}
