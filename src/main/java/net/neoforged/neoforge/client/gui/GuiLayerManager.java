/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Adaptation of {@link LayeredDraw} that is used for {@link Gui} rendering specifically,
 * to give layers a name and fire appropriate events.
 *
 * <p>Overlays can be registered using the {@link RegisterGuiLayersEvent} event.
 */
@ApiStatus.Internal
public class GuiLayerManager {
    // Kilt: Add empty layer
    private static final LayeredDraw.Layer KILT_EMPTY_LAYER = (guiGraphics, deltaTracker) -> {};

    public static final float Z_SEPARATION = LayeredDraw.Z_SEPARATION;
    private final List<NamedLayer> layers = new ArrayList<>();
    private boolean initialized = false;

    public record NamedLayer(ResourceLocation name, LayeredDraw.Layer layer, boolean isVanilla) {
        // Kilt: Add component for differentiating between Vanilla and not
        public NamedLayer(ResourceLocation name, LayeredDraw.Layer layer) {
            this(name, layer, false);
        }

        public boolean kilt$isEmptyLayer() {
            return this.layer() == KILT_EMPTY_LAYER;
        }
    }

    public GuiLayerManager add(ResourceLocation name, LayeredDraw.Layer layer) {
        this.layers.add(new NamedLayer(name, layer));
        return this;
    }

    public GuiLayerManager kilt$addVanilla(ResourceLocation id) {
        this.layers.add(new NamedLayer(id, KILT_EMPTY_LAYER, true));
        return this;
    }

    public GuiLayerManager kilt$addVanilla(ResourceLocation id, LayeredDraw.Layer layer) {
        this.layers.add(new NamedLayer(id, layer, true));
        return this;
    }

    public GuiLayerManager add(GuiLayerManager child, BooleanSupplier shouldRender) {
        // Flatten the layers to allow mods to insert layers between vanilla layers.
        for (var entry : child.layers) {
            add(entry.name(), (guiGraphics, partialTick) -> {
                if (shouldRender.getAsBoolean()) {
                    entry.layer().render(guiGraphics, partialTick);
                }
            });
        }
        return this;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(guiGraphics, partialTick)).isCanceled()) {
            return;
        }

        renderInner(guiGraphics, partialTick);

        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, partialTick));
    }

    private void renderInner(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        guiGraphics.pose().pushPose();

        for (var layer : this.layers) {
            if (!NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, layer.name(), layer.layer())).isCanceled()) {
                layer.layer().render(guiGraphics, partialTick);
                NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, partialTick, layer.name(), layer.layer()));
            }

            guiGraphics.pose().translate(0.0F, 0.0F, Z_SEPARATION);
        }

        guiGraphics.pose().popPose();
    }

    public void initModdedLayers() {
        if (initialized) {
            throw new IllegalStateException("Duplicate initialization of NamedLayeredDraw");
        }
        initialized = true;
        ModLoader.postEvent(new RegisterGuiLayersEvent(this.layers));
    }

    public int getLayerCount() {
        return this.layers.size();
    }

    // Kilt: custom compatibility GUI rendering stuff :D (yes I stole this from Porting Lib, I wrote the damn code lmao)
    /**
     * Renders layers starting from one render layer until the next Vanilla layer is reached.
     * @param start The ID of the rendering layer to start from
     */
    public void kilt$renderFrom(ResourceLocation start, GuiGraphics guiGraphics, DeltaTracker partialTick) {
        NamedLayer startingLayer = kilt$getLayer(start);
        if (startingLayer == null) {
            throw new IllegalArgumentException("Layer " + start + " does not exist!");
        }

        kilt$renderFrom(startingLayer, guiGraphics, partialTick);
    }

    public void kilt$renderFrom(NamedLayer startingLayer, GuiGraphics guiGraphics, DeltaTracker partialTick) {
        guiGraphics.pose().pushPose();
        boolean hasStartedRendering = false;

        for (NamedLayer layer : layers) {
            if (layer == startingLayer || startingLayer == null) {
                hasStartedRendering = true;
            }

            if (!hasStartedRendering) {
                continue;
            }

            // Stop rendering entirely if this layer is a Vanilla layer that isn't the starting layer.
            if (layer.isVanilla() && layer != startingLayer) {
                break;
            }

            // Render only non-Vanilla layers - we may end up double-rendering otherwise.
            if (!layer.isVanilla()) {
                kilt$renderLayer(guiGraphics, partialTick, layer);
            }
        }
        guiGraphics.pose().popPose();
    }

    public boolean kilt$callPreRenderEvent(ResourceLocation id, GuiGraphics guiGraphics, DeltaTracker partialTick) {
        NamedLayer layer = kilt$getLayer(id);

        if (layer == null) {
            throw new IllegalArgumentException("Layer " + id + " does not exist!");
        }

        return NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, layer.name(), layer.layer())).isCanceled();
    }

    public void kilt$callPostRenderEvent(ResourceLocation id, GuiGraphics guiGraphics, DeltaTracker partialTick) {
        NamedLayer layer = kilt$getLayer(id);

        if (layer == null) {
            throw new IllegalArgumentException("Layer " + id + " does not exist!");
        }

        NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, partialTick, layer.name(), layer.layer()));
    }

    private void kilt$renderLayer(GuiGraphics guiGraphics, DeltaTracker partialTick, NamedLayer layer) {
        if (!NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, partialTick, layer.name(), layer.layer())).isCanceled()) {
            layer.layer().render(guiGraphics, partialTick);
            NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Post(guiGraphics, partialTick, layer.name(), layer.layer()));
        }

        guiGraphics.pose().translate(0.0F, 0.0F, Z_SEPARATION);
    }

    public NamedLayer kilt$getLayer(ResourceLocation id) {
        for (NamedLayer layer : layers) {
            if (layer.name().equals(id)) {
                return layer;
            }
        }

        return null;
    }
}
