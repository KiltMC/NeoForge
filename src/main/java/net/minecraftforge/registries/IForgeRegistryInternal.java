/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.workarounds.IFabricWrappedForgeRegistry;

public interface IForgeRegistryInternal<V> extends IFabricWrappedForgeRegistry<V>
{
    void setSlaveMap(ResourceLocation name, Object obj);

    void register(int id, ResourceLocation key, V value);
    V getValue(int id);
}
