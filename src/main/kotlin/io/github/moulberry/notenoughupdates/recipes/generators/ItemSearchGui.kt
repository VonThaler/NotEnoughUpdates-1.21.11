/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.recipes.generators

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class ItemSearchGui(initialText: String, val onSelect: (String?) -> Unit) : Screen(Text.literal("Item Search")) {

    private lateinit var textField: TextFieldWidget
    var lastFilter = ""

    inner class ItemScrollingList(
        screenWidth: Int,
        screenHeight: Int,
        val callback: (ItemStack) -> Unit
    ) : AlwaysSelectedEntryListWidget<ItemScrollingList.ItemEntry>(
        MinecraftClient.getInstance(),
        200,
        screenHeight - 30,
        30,
        screenHeight,
        20
    ) {
        var itemsList: List<ItemStack> = listOf()
            private set

        init {
            setLeftPos(screenWidth / 2 - 100)
        }

        fun setItems(newItems: List<ItemStack>) {
            itemsList = newItems
            clearEntries()
            newItems.forEach { addEntry(ItemEntry(it)) }
        }

        inner class ItemEntry(val stack: ItemStack) : AlwaysSelectedEntryListWidget.Entry<ItemEntry>() {
            override fun render(
                context: DrawContext,
                index: Int,
                y: Int,
                x: Int,
                entryWidth: Int,
                entryHeight: Int,
                mouseX: Int,
                mouseY: Int,
                hovered: Boolean,
                tickDelta: Float
            ) {
                context.drawItem(stack, x + 1, y + 1)
                context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    stack.name,
                    x + 18,
                    y + 5,
                    0xFF00FF00.toInt(),
                    true
                )
            }

            override fun getNarration(): Text = stack.name

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                this@ItemScrollingList.setSelected(this)
                if (button == 0) callback(stack)
                return true
            }
        }

        override fun getRowWidth(): Int = 190
        override fun getScrollbarPositionX(): Int = left + width + 4
    }

    private lateinit var items: ItemScrollingList

    override fun init() {
        super.init()
        textField = TextFieldWidget(
            MinecraftClient.getInstance().textRenderer,
            width / 2 - 100,
            5,
            200,
            20,
            Text.literal("")
        )
        textField.setCanLoseFocus(false)
        textField.isFocused = true

        items = ItemScrollingList(width, height) { stack ->
            val name = NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
                .withItemStack(stack)
                .resolveInternalName() ?: return@ItemScrollingList
            onSelect(name)
        }

        addDrawableChild(textField)
        updateItems()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        items.render(context, mouseX, mouseY, delta)
        textField.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun removed() {
        onSelect(null)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (items.mouseClicked(mouseX, mouseY, button)) return true
        if (textField.mouseClicked(mouseX, mouseY, button)) return true
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (items.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (textField.keyPressed(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (textField.charTyped(chr, modifiers)) return true
        return super.charTyped(chr, modifiers)
    }

    fun updateItems() {
        lastFilter = textField.text
        val candidates = NotEnoughUpdates.INSTANCE.manager.search(textField.text)
            .map {
                NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
                    .withKnownInternalName(it)
                    .resolveToItemStack() ?: ItemStack(Items.PAINTING)
            }
        items.setItems(candidates)
    }

    override fun tick() {
        super.tick()
        textField.tick()
        if (textField.text != lastFilter) updateItems()
    }
}
