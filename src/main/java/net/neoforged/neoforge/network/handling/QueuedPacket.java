/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import org.jetbrains.annotations.ApiStatus;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.workarounds.network.PacketProcessorListenerWorkaround;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

@ApiStatus.Internal
public interface QueuedPacket extends Packet<PacketProcessorListenerWorkaround> {
    void handle();

    // Kilt: funny little workarounds
    @Override
    default PacketType<? extends Packet<PacketProcessorListenerWorkaround>> type() {
        return new PacketType<>(PacketFlow.SERVERBOUND, Kilt.id("neoforge/queued_packet"));
    }

    @Override
    default void handle(PacketProcessorListenerWorkaround listener) {
        this.handle();
    }

    record CustomPayload(Runnable task) implements QueuedPacket {
        @Override
        public void handle() {
            task.run();
        }
    }
}
