package net.minecraft.client.gui

import net.minecraft.client.MinecraftClient

/**
 * Kotlin-visible compatibility bridge for legacy GUI code.
 */
class ScaledResolution(client: MinecraftClient) {
    private val window = client.window

    val scaleFactor: Int = window.scaleFactor.toInt().coerceAtLeast(1)
    val scaledWidth: Int = window.scaledWidth
    val scaledHeight: Int = window.scaledHeight
    val framebufferHeight: Int = window.framebufferHeight

    fun getScaleFactor(): Int = scaleFactor
    fun getScaledWidth(): Int = scaledWidth
    fun getScaledHeight(): Int = scaledHeight
    fun getFramebufferHeight(): Int = framebufferHeight
}

