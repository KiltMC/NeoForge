/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import xyz.bluspring.kilt.workarounds.ICommonPacketListenerWorkaround;

/**
 * Extension interface for {@link ClientCommonPacketListener}
 */
public interface IClientCommonPacketListenerExtension extends ICommonPacketListener {
    /**
     * {@inheritDoc}
     */
    @Override
    default void send(CustomPacketPayload payload) {
        ((ICommonPacketListenerWorkaround) this).send(new ServerboundCustomPayloadPacket(payload));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void disconnect(Component reason) {
        this.getConnection().disconnect(reason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default ReentrantBlockableEventLoop<?> getMainThreadEventLoop() {
        return Minecraft.getInstance();
    }
}
