/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.capabilities;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal // Modders should use ICapabilityProvider, this is for Forge
public interface ICapabilityProviderImpl<B extends ICapabilityProviderImpl<B>> extends ICapabilityProvider
{
    default boolean areCapsCompatible(CapabilityProvider<B> other) {
        throw new IllegalStateException("what?");
    }
    default boolean areCapsCompatible(@Nullable CapabilityDispatcher other) {
        throw new IllegalStateException("what?");
    }
    default void invalidateCaps() {
        throw new IllegalStateException("what?");
    }
    default void reviveCaps() {
        throw new IllegalStateException("what?");
    }
}
