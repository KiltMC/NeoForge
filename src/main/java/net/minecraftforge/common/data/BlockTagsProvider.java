/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.injections.data.tags.TagsProviderInjection;

import java.util.concurrent.CompletableFuture;

public abstract class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block>
{
    @SuppressWarnings("deprecation")
    public BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, Registries.BLOCK, lookupProvider, block -> block.builtInRegistryHolder().key());
        ((TagsProviderInjection) this).kilt$setModId(modId);
        ((TagsProviderInjection) this).kilt$setExistingFileHelper(existingFileHelper);
    }
}
