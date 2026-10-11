/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;

import net.neoforged.neoforge.common.Tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;

public final class NeoForgeStructureTagsProvider extends StructureTagsProvider {
    public NeoForgeStructureTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
        this.kilt$setModId("neoforge");
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Tags.Structures.HIDDEN_FROM_DISPLAYERS);
        tag(Tags.Structures.HIDDEN_FROM_LOCATOR_SELECTION);
    }
}
