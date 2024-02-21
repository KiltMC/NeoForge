/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class ForgeRegistry<V> extends FabricWrappedForgeRegistry<V>
{
    public ForgeRegistry(@NotNull RegistryManager stage, @NotNull ResourceLocation registryName, @NotNull RegistryBuilder<V> builder) {
        super(stage, registryName, builder);
    }

    public static class Snapshot extends FabricWrappedForgeRegistry.Snapshot {
    }
}
