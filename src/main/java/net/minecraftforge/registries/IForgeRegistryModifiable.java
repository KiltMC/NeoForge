/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.workarounds.IFabricWrappedForgeRegistry;

public interface IForgeRegistryModifiable<V> extends IFabricWrappedForgeRegistry<V>
{
    void clear();
    V remove(ResourceLocation key);
    boolean isLocked();
}
