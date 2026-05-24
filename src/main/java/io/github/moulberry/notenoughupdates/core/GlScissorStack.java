/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.core;

import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class GlScissorStack {
	private static class Bounds {
		int left;
		int top;
		int right;
		int bottom;

		public Bounds(int left, int top, int right, int bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}

		public Bounds createSubBound(int left, int top, int right, int bottom) {
			left = Math.max(left, this.left);
			top = Math.max(top, this.top);
			right = Math.min(right, this.right);
			bottom = Math.min(bottom, this.bottom);

			if (top > bottom) {
				top = bottom;
			}
			if (left > right) {
				left = right;
			}

			return new Bounds(left, top, right, bottom);
		}

		public void set(int scale, int height) {
			GL11.glScissor(left * scale, height - bottom * scale, (right - left) * scale, (bottom - top) * scale);
		}
	}

	private static final LinkedList<Bounds> boundsStack = new LinkedList<>();

	public static void push(int left, int top, int right, int bottom, int scaleFactor, int framebufferHeight) {
		if (right < left) {
			int temp = right;
			right = left;
			left = temp;
		}
		if (bottom < top) {
			int temp = bottom;
			bottom = top;
			top = temp;
		}
		if (boundsStack.isEmpty()) {
			boundsStack.push(new Bounds(left, top, right, bottom));
		} else {
			boundsStack.push(boundsStack.peek().createSubBound(left, top, right, bottom));
		}
		refresh(scaleFactor, framebufferHeight);
	}

	public static void push(int left, int top, int right, int bottom, Object scaledResolution) {
		push(left, top, right, bottom, getScaleFactor(scaledResolution), getFramebufferHeight(scaledResolution));
	}

	/**
	 * Disable scissors temporarily. Can be reenabled with {@link #refresh(int, int)}
	 */
	public static void disableTemporary() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public static void refresh(int scaleFactor, int framebufferHeight) {
		if (boundsStack.isEmpty()) {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else {
			boundsStack.peek().set(scaleFactor, framebufferHeight);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		}
	}

	public static void refresh(Object scaledResolution) {
		refresh(getScaleFactor(scaledResolution), getFramebufferHeight(scaledResolution));
	}

	public static void pop(int scaleFactor, int framebufferHeight) {
		if (!boundsStack.isEmpty()) {
			boundsStack.pop();
		}
		refresh(scaleFactor, framebufferHeight);
	}

	public static void pop(Object scaledResolution) {
		pop(getScaleFactor(scaledResolution), getFramebufferHeight(scaledResolution));
	}

	public static void clear() {
		boundsStack.clear();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	private static int getScaleFactor(Object scaledResolution) {
		if (scaledResolution == null) {
			return 1;
		}
		try {
			Method method = scaledResolution.getClass().getMethod("getScaleFactor");
			return ((Number) method.invoke(scaledResolution)).intValue();
		} catch (ReflectiveOperationException e) {
			return 1;
		}
	}

	private static int getFramebufferHeight(Object scaledResolution) {
		if (scaledResolution == null) {
			return 0;
		}
		try {
			Method method = scaledResolution.getClass().getMethod("getFramebufferHeight");
			return ((Number) method.invoke(scaledResolution)).intValue();
		} catch (ReflectiveOperationException e) {
			return 0;
		}
	}
}
