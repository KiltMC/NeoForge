/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.extensions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;

public interface IForgeMenuType<T>
{
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory)
    {
        return new MenuType<>(factory);
    }

    default T create(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
        throw new IllegalStateException();
    }
}
