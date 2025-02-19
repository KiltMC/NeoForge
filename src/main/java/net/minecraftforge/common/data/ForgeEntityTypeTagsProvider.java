/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import xyz.bluspring.kilt.injections.data.tags.TagsProviderInjection;

public class ForgeEntityTypeTagsProvider extends TagsProvider<EntityType<?>> implements TagsProviderInjection
{
    
    public ForgeEntityTypeTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Registry.ENTITY_TYPE);
        this.kilt$addConstructorArgs("forge", existingFileHelper);
    }

    @Override
    public void addTags()
    {
        tag(Tags.EntityTypes.BOSSES).add(EntityType.ENDER_DRAGON, EntityType.WITHER);
    }

    @Override
    public String getName()
    {
        return "Forge EntityType Tags";
    }
}
