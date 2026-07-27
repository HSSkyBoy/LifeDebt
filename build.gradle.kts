plugins {
	id("fabric-loom")
	`maven-publish`
}

val mcVersion = property("minecraft_version") as String

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String
base.archivesName = "lifedebt"

val requiredJava = JavaVersion.VERSION_21

loom {
	mods {
		create("lifedebt") {
			sourceSet(sourceSets["main"])
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

java {
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
tasks.processResources {
	inputs.property("version", modVersion)
	inputs.property("minecraft_version", mcVersion)

	filesMatching("fabric.mod.json") {
		expand(
			"version" to modVersion,
			"minecraft_version" to mcVersion,
			"java_version" to requiredJava.majorVersion,
		)
	}
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_lifedebt" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = "lifedebt"
			from(components["java"])
		}
	}

	repositories {
	}
}
