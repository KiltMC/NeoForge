/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model.geometry;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Function;

/**
 * General interface for any model that can be baked, superset of vanilla {@link UnbakedModel}.
 * <p>
 * Instances of this class ar usually created via {@link IGeometryLoader}.
 *
 * @see IGeometryLoader
 * @see IGeometryBakingContext
 */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> extends io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry<T>
{
    BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation);

    /**
     * Resolve parents of nested {@link BlockModel}s which are later used in
     * {@link IUnbakedGeometry#bake(IGeometryBakingContext, ModelBaker, Function, ModelState, ItemOverrides, ResourceLocation)}
     * via {@link BlockModel#resolveParents(Function)}
     */
    default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
    {

    }

    /**
     * {@return a set of all the components whose visibility may be configured via {@link IGeometryBakingContext}}
     */
    default Set<String> getConfigurableComponentNames()
    {
        return Set.of();
    }

    // Kilt: Convert calls for Porting Lib's IUnbakedGeometry to Forge's
    @Override
    default BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
        var ctx = new BlockGeometryBakingContext(context);
        ctx.setGui3d(isGui3d);

        return bake(ctx, baker, spriteGetter, modelState, overrides, modelLocation);
    }

    @Override
    default void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, BlockModel context) {
        var ctx = new BlockGeometryBakingContext(context);

        resolveParents(modelGetter, ctx);
    }
    // Kilt end
}
