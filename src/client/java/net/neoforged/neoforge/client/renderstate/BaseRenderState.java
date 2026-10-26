/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.neoforged.neoforge.client.extensions.IRenderStateExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.workarounds.BaseRenderStateWorkaround;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;

/**
 * Extension class for RenderState objects (ie {@link EntityRenderState}).
 * Allows modders to add arbitrary data onto render states for use in custom rendering.
 */
public abstract class BaseRenderState implements IRenderStateExtension, BaseRenderStateWorkaround {
    protected final Map<ContextKey<?>, Object> extensions = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getRenderData(ContextKey<T> key) {
        return (T) extensions.get(key);
    }

    @Override
    public <T> void setRenderData(ContextKey<T> key, @Nullable T data) {
        if (data != null) {
            extensions.put(key, data);
        } else {
            extensions.remove(key);
        }
    }

    @Override
    @ApiStatus.Internal
    public void resetRenderData() {
        extensions.clear();
    }

    @Override
    public Map<ContextKey<?>, Object> kilt$getExtensions() {
        return this.extensions;
    }
}
