/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

@FunctionalInterface
public interface GuiLayer extends HudElement {
    // Kilt: Basically the same.
    @Override
    default void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        render(graphics, deltaTracker);
    }

    void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker);
}
