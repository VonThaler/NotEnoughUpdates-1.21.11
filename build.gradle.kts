/*
 * Copyright (C) 2022-2024 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
	idea
	java
	id("fabric-loom") version "1.14.7"
	`maven-publish`
	kotlin("jvm") version "2.0.0"
	id("com.github.johnrengelman.shadow") version "8.1.1"
	id("net.kyori.blossom") version "2.1.0"
	id("io.gitlab.arturbosch.detekt") version "1.23.6"
	id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

group = "io.github.moulberry"
version = project.property("mod_version") as String

// ── Minecraft / Fabric ────────────────────────────────────────────────────────
loom {
	// Mixin config declared here so loom sets up refmap for us automatically
	@Suppress("UnstableApiUsage")
	mixin {
		defaultRefmapName.set("mixins.notenoughupdates.refmap.json")
		add(sourceSets.main.get(), "mixins.notenoughupdates.refmap.json")
	}

	runConfigs {
		"client" {
			vmArgs.add("-Xmx4G")
			// Enable mixin debug output during dev
			vmArgs.add("-Dmixin.debug.export=true")
		}
		"server" {
			isIdeConfigGenerated = false
		}
	}
}

// ── Repositories ──────────────────────────────────────────────────────────────
repositories {
	mavenCentral()
	mavenLocal()
	maven("https://maven.fabricmc.net/")
	maven("https://maven.notenoughupdates.org/releases")
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	maven("https://jitpack.io")
	maven("https://repo.nea.moe/releases")
	maven("https://maven.terraformersmc.com/") // ModMenu
	maven("https://maven.shedaniel.me/")       // Cloth Config
	maven("https://repo.spongepowered.org/repository/maven-public")     // Mixin
}

// ── Custom configurations ─────────────────────────────────────────────────────
val shadowBundle: Configuration by configurations.creating {
	isCanBeResolved = true
	isCanBeConsumed = false
}

val devEnv: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
	isCanBeResolved = false
	isCanBeConsumed = false
	isVisible = false
}

// ── Dependencies ──────────────────────────────────────────────────────────────
dependencies {
	// --- Core Fabric / Minecraft ---
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	// Using Mojang mappings (mojmap) – widely supported, no license issues at runtime
	mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")

	// --- Kotlin on Fabric (replaces manual kotlin bundling from 1.8.9 build) ---
	// This mod provides the Kotlin runtime to all Fabric mods; no need to shadow kotlin stdlib
	modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")

	// --- Mixin (provided by Fabric Loader, just need annotation processor) ---
	annotationProcessor("net.fabricmc:sponge-mixin:0.15.3+mixin.0.8.7")
	compileOnly("org.spongepowered:mixin:0.8.7")

	// --- KSP / AutoService (unchanged from 1.8.9 build) ---
	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
	implementation("com.google.auto.service:auto-service-annotations:1.0.1")

	// --- Lombok (unchanged) ---
	compileOnly("org.projectlombok:lombok:1.18.32")
	annotationProcessor("org.projectlombok:lombok:1.18.32")

	// --- Annotations (unchanged) ---
	compileOnly("org.jetbrains:annotations:24.0.1")
	compileOnly(project(":annotations"))
	ksp(project(":annotations"))

	// --- MoulConfig (Fabric build) ---
	// IMPORTANT: You must upgrade to a MoulConfig version that supports Fabric 1.21.
	// Check https://maven.notenoughupdates.org for the latest fabric build.
	// Replace "MOULCONFIG_FABRIC_VERSION" below with the real version once confirmed.
	modImplementation("org.notenoughupdates.moulconfig:modern-1.21.11:4.4.0-beta")
	include("org.notenoughupdates.moulconfig:modern-1.21.11:4.4.0-beta") {
		exclude("net.fabricmc.fabric-api")
	}

	// --- libautoupdate (unchanged dependency, works on Fabric) ---
	shadowBundle("moe.nea:libautoupdate:1.3.1")
	include("moe.nea:libautoupdate:1.3.1")

	// --- NEA Lisp (unchanged) ---
	shadowBundle(libs.nealisp) {
		exclude("org.jetbrains.kotlin")
	}
	include(libs.nealisp)

	// --- Brigadier is now bundled by vanilla Minecraft in 1.21 – no longer needs shadowing ---
	// (was: shadowImplementation("com.mojang:brigadier:1.0.18"))
	// It's available on the compile classpath automatically via the minecraft dependency.

	// --- Bliki (wiki parsing – unchanged) ---
	shadowBundle("info.bliki.wiki:bliki-core:3.1.0")
	include("info.bliki.wiki:bliki-core:3.1.0")

	// --- Cloth Config (replaces Forge's config GUI system) ---
	modImplementation("me.shedaniel.cloth:cloth-config-fabric:15.0.130") {
		exclude(group = "net.fabricmc.fabric-api")
	}
	include("me.shedaniel.cloth:cloth-config-fabric:15.0.130")

	// --- ModMenu (optional but recommended – adds NEU to the mods list in-game) ---
	modCompileOnly("com.terraformersmc:modmenu:11.0.3")

	// --- Dev Auth (replaces DevAuth-forge-legacy) ---
	devEnv("me.djtheredstoner:DevAuth-fabric:1.2.1")

	// --- Test ---
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

// ── Java toolchain ────────────────────────────────────────────────────────────
java {
	withSourcesJar()

	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
	jvmToolchain(21)
}

// ── Compile options ───────────────────────────────────────────────────────────
tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(21)
}


// ── Test ──────────────────────────────────────────────────────────────────────
tasks.named<Test>("test") {
	useJUnitPlatform()
	testLogging {
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}

// ── Resource processing ───────────────────────────────────────────────────────
tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("minecraft_version", project.property("minecraft_version"))
	inputs.property("loader_version", project.property("loader_version"))

	filteringCharset = "UTF-8"

	filesMatching("fabric.mod.json") {
		expand(
			"version" to project.version,
			"minecraft_version" to project.property("minecraft_version")!!,
			"loader_version" to project.property("loader_version")!!,
			"fabric_kotlin_version" to project.property("fabric_kotlin_version")!!
		)
	}
}

// ── Shadow jar ────────────────────────────────────────────────────────────────
// With Fabric, most deps are handled via `include()` (JiJ – Jar in Jar).
// Shadow is still useful for relocating deps that might conflict.
tasks.shadowJar {
	configurations = listOf(shadowBundle)
	archiveClassifier.set("dev-shadow")

	exclude("**/module-info.class", "LICENSE.txt")

	// Relocate shadowed deps under NEU's namespace to avoid conflicts
	relocate("moe.nea.libautoupdate", "io.github.moulberry.notenoughupdates.deps.libautoupdate")
	relocate("moe.nea.lisp", "io.github.moulberry.notenoughupdates.deps.lisp")
	relocate("info.bliki", "io.github.moulberry.notenoughupdates.deps.bliki")

	mergeServiceFiles()
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
	archiveClassifier.set("")
	inputFile.set(tasks.shadowJar.get().archiveFile)
	dependsOn(tasks.shadowJar)
	doLast {
		println("Built: ${archiveFile.get().asFile}")
	}
}

tasks.assemble.get().dependsOn(remapJar)

// ── Source sets ───────────────────────────────────────────────────────────────
sourceSets.main {
	this.blossom {
		this.javaSources {
			this.property("neuVersion", project.version.toString())
		}
	}
}

idea {
	module {
		generatedSourceDirs = generatedSourceDirs +
			file("build/generated/ksp/main/kotlin") +
			file("build/generated/ksp/test/kotlin")
	}
}

// ── Publishing ────────────────────────────────────────────────────────────────
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = project.group.toString()
			artifactId = "NotEnoughUpdates"
			version = project.version.toString()
			from(components["java"])
		}
	}
}
