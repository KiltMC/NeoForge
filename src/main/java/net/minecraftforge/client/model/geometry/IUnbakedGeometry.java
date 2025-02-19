/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model.geometry;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.workarounds.FabricGeometryBakingContextWrapper;

import java.util.Collection;
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
public interface IUnbakedGeometry<T extends io.github.fabricators_of_create.porting_lib.model.geometry.IUnbakedGeometry<T>> extends io.github.fabricators_of_create.porting_lib.model.geometry.IUnbakedGeometry<T>
{
    BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation);

    Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors);

    /**
     * {@return a set of all the components whose visibility may be configured via {@link IGeometryBakingContext}}
     */
    default Set<String> getConfigurableComponentNames()
    {
        return Set.of();
    }

    // Kilt: Convert calls for Porting Lib's IUnbakedGeometry to Forge's
    @Override
    default BakedModel bake(io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryBakingContext iGeometryBakingContext, ModelBakery modelBakery, Function<Material, TextureAtlasSprite> function, ModelState modelState, ItemOverrides itemOverrides, ResourceLocation resourceLocation) {
        return bake(new FabricGeometryBakingContextWrapper(iGeometryBakingContext), modelBakery, function, modelState, itemOverrides, resourceLocation);
    }

    @Override
    default Collection<Material> getMaterials(io.github.fabricators_of_create.porting_lib.model.geometry.IGeometryBakingContext iGeometryBakingContext, Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
        return getMaterials(new FabricGeometryBakingContextWrapper(iGeometryBakingContext), function, set);
    }

    // Kilt end
}
