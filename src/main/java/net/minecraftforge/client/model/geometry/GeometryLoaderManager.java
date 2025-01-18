/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model.geometry;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.workarounds.FabricGeometryLoaderWrapper;

/**
 * Manager for {@linkplain IGeometryLoader geometry loaders}.
 * <p>
 * Provides a lookup.
 */
public final class GeometryLoaderManager
{
    /**
     * Finds the {@link IGeometryLoader} for a given name, or null if not found.
     */
    @Nullable
    public static IGeometryLoader<?> get(ResourceLocation name)
    {
        var geometryLoader = io.github.fabricators_of_create.porting_lib.models.geometry.GeometryLoaderManager.get(name);

        if (geometryLoader == null)
            return null;

        if (geometryLoader instanceof IGeometryLoader<?> forgeGeometryLoader)
            return forgeGeometryLoader;

        return new FabricGeometryLoaderWrapper<>(geometryLoader);
    }

    /**
     * Retrieves a comma-separated list of all active loaders, for use in error messages.
     */
    public static String getLoaderList()
    {
        return io.github.fabricators_of_create.porting_lib.models.geometry.GeometryLoaderManager.getLoaderList();
    }

    @ApiStatus.Internal
    public static void init()
    {
        //var loaders = new HashMap<ResourceLocation, IGeometryLoader<?>>();
        //var event = new ModelEvent.RegisterGeometryLoaders(loaders);
        //ModLoader.get().postEventWrapContainerInModOrder(event);
        //LOADERS = ImmutableMap.copyOf(loaders);
        //LOADER_LIST = loaders.keySet().stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
    }

    private GeometryLoaderManager()
    {
    }
}
