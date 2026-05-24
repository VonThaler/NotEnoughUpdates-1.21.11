/*
 * Copyright (C) 2026 NotEnoughUpdates contributors
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

package net.minecraft.client.gui.legacy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

/**
 * Legacy compatibility bridge for old 1.8.9-style GUI code.
 * Uses the active Fabric client window for scale and framebuffer dimensions.
 */
public class ScaledResolutionJavaBridge {
	private final int scaleFactor;
	private final int scaledWidth;
	private final int scaledHeight;
	private final int framebufferHeight;

	public ScaledResolution(MinecraftClient client) {
		Window window = client.getWindow();
		this.scaleFactor = (int) Math.max(1, Math.round(window.getScaleFactor()));
		this.scaledWidth = window.getScaledWidth();
		this.scaledHeight = window.getScaledHeight();
		this.framebufferHeight = window.getFramebufferHeight();
	}

	public int getScaleFactor() {
		return scaleFactor;
	}

	public int getScaledWidth() {
		return scaledWidth;
	}

	public int getScaledHeight() {
		return scaledHeight;
	}

	public int getFramebufferHeight() {
		return framebufferHeight;
	}
}

