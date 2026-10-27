/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.submit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;

@ApiStatus.Internal
public final class ExtendedBlockFeatureRenderer extends BlockFeatureRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();

    public static void renderMultiLayerBlockModelSubmits(
            SubmitNodeCollection nodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            OutlineBufferSource outlineBufferSource,
            QuadInstance quadInstance,
            boolean translucent) {
        for (FabricSubmitNodeCollection.ExtendedBlockModelSubmit submit : nodeCollection.getExtendedBlockModelSubmits()) { // Kilt: use Fabric API's implementation
            if (submit.translucent() != translucent) {
                continue;
            }

            int outlineColor = submit.outlineColor();
            VertexConsumer outlineBuffer = null;
            if (outlineColor != 0) {
                outlineBufferSource.setColor(outlineColor);
                outlineBuffer = outlineBufferSource.getBuffer(RenderTypes.outline(TextureAtlas.LOCATION_BLOCKS));
            }

            quadInstance.setLightCoords(submit.lightCoords());
            quadInstance.setOverlayCoords(submit.overlayCoords());

            PoseStack.Pose pose = submit.pose();
            int[] tintLayers = submit.tintLayers();
            for (BlockStateModelPart part : submit.modelParts()) {
                putMultiLayerPartQuads(pose, part, quadInstance, tintLayers, bufferSource, outlineBuffer);
            }
        }
    }

    private static void putMultiLayerPartQuads(
            PoseStack.Pose pose,
            BlockStateModelPart part,
            QuadInstance quadInstance,
            int[] tintLayers,
            MultiBufferSource.BufferSource bufferSource,
            @Nullable VertexConsumer outlineBuffer) {
        for (Direction direction : DIRECTIONS) {
            for (BakedQuad quad : part.getQuads(direction)) {
                putQuad(pose, quad, quadInstance, tintLayers, getQuadBuffer(bufferSource, quad), outlineBuffer);
            }
        }

        for (BakedQuad quad : part.getQuads(null)) {
            putQuad(pose, quad, quadInstance, tintLayers, getQuadBuffer(bufferSource, quad), outlineBuffer);
        }
    }

    private static VertexConsumer getQuadBuffer(MultiBufferSource.BufferSource bufferSource, BakedQuad quad) {
        return bufferSource.getBuffer(switch (quad.materialInfo().layer()) {
            case SOLID -> NeoForgeRenderTypes.SOLID_BLOCK_SHEET;
            case CUTOUT -> Sheets.cutoutBlockSheet();
            case TRANSLUCENT -> Sheets.translucentBlockSheet();
        });
    }

    private ExtendedBlockFeatureRenderer() {}
}
