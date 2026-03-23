/*
 * Copyright (C) 2022-2024 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 */

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/")
		maven("https://maven.architectury.dev/")
		maven("https://oss.sonatype.org/content/repositories/snapshots")
		maven("https://jitpack.io/")
		maven("https://maven.xpdustry.com/releases") {
			name = "xpdustry-releases"
			mavenContent { releasesOnly() }
		}
	}
}



include("annotations")
rootProject.name = "NotEnoughUpdates"
