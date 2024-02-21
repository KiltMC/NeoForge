/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.world;

import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.chunk.loading.PortingLibChunkManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ForcedChunksSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.bluspring.kilt.mixin.porting_lib.TicketHelperAccessor;
import xyz.bluspring.kilt.mixin.porting_lib.TicketOwnerAccessor;
import xyz.bluspring.kilt.mixin.porting_lib.TicketTrackerAccessor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class ForgeChunkManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Sets the forced chunk loading validation callback for the given mod. This allows for validating and removing no longer valid tickets on level load.
     *
     * @apiNote This method should be called from a {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} using one of the {@link
     * net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent} enqueueWork methods.
     */
    public static void setForcedChunkLoadingCallback(String modId, LoadingValidationCallback callback)
    {
        PortingLibChunkManager.setForcedChunkLoadingCallback(modId, ((level, ticketHelper) -> {
            callback.validateTickets(level, TicketHelper.fromFabric(ticketHelper));
        }));
    }

    /**
     * Checks if a level has any forced chunks. Mainly used for seeing if a level should continue ticking with no players in it.
     */
    public static boolean hasForcedChunks(ServerLevel level)
    {
        return PortingLibChunkManager.hasForcedChunks(level);
    }

    /**
     * Forces a chunk to be loaded for the given mod with the "owner" of the ticket being a given block position.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public static boolean forceChunk(ServerLevel level, String modId, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking)
    {
        return PortingLibChunkManager.forceChunk(level, modId, owner, chunkX, chunkZ, add, ticking);
    }

    /**
     * Forces a chunk to be loaded for the given mod with the "owner" of the ticket being the UUID of the given entity.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public static boolean forceChunk(ServerLevel level, String modId, Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking)
    {
        return PortingLibChunkManager.forceChunk(level, modId, owner.getUUID(), chunkX, chunkZ, add, ticking);
    }

    /**
     * Forces a chunk to be loaded for the given mod with the "owner" of the ticket being a given UUID.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public static boolean forceChunk(ServerLevel level, String modId, UUID owner, int chunkX, int chunkZ, boolean add, boolean ticking)
    {
        return PortingLibChunkManager.forceChunk(level, modId, owner, chunkX, chunkZ, add, ticking);
    }

    /**
     * Reinstates forge's forced chunks when vanilla initially loads a level and reinstates their forced chunks. This method also will validate all of forge's forced
     * chunks using and registered {@link LoadingValidationCallback}.
     *
     * @apiNote Internal
     */
    public static void reinstatePersistentChunks(ServerLevel level, ForcedChunksSavedData saveData)
    {
        PortingLibChunkManager.reinstatePersistentChunks(level, saveData);
    }

    /**
     * Writes the forge forced chunks into the NBT compound. Format is List{modid, List{ChunkPos, List{BlockPos}, List{UUID}}}
     *
     * @apiNote Internal
     */
    public static void writeForgeForcedChunks(CompoundTag nbt, TicketTracker<BlockPos> blockForcedChunks, TicketTracker<UUID> entityForcedChunks)
    {
        PortingLibChunkManager.writeForgeForcedChunks(nbt, blockForcedChunks.toFabric(), entityForcedChunks.toFabric());
    }

    /**
     * Reads the forge forced chunks from the NBT compound. Format is List{modid, List{ChunkPos, List{BlockPos}, List{UUID}}}
     *
     * @apiNote Internal
     */
    public static void readForgeForcedChunks(CompoundTag nbt, TicketTracker<BlockPos> blockForcedChunks, TicketTracker<UUID> entityForcedChunks)
    {
        PortingLibChunkManager.readForgeForcedChunks(nbt, blockForcedChunks.toFabric(), entityForcedChunks.toFabric());
    }

    @FunctionalInterface
    public interface LoadingValidationCallback {
        /**
         * Called back when tickets are about to be loaded and reinstated to allow mods to invalidate and remove specific tickets that may no longer be valid.
         *
         * @param level        The level
         * @param ticketHelper Ticket helper to remove any invalid tickets.
         */
        void validateTickets(ServerLevel level, TicketHelper ticketHelper);
    }

    /**
     * Class to help mods remove no longer valid tickets.
     */
    public static class TicketHelper
    {
        public static TicketHelper fromFabric(PortingLibChunkManager.TicketHelper fabric) {
            var accessor = (TicketHelperAccessor) fabric;
            return new TicketHelper(accessor.getSaveData(), accessor.getModId(), accessor.getBlockTickets(), accessor.getEntityTickets());
        }

        private final Map<BlockPos, Pair<LongSet, LongSet>> blockTickets;
        private final Map<UUID, Pair<LongSet, LongSet>> entityTickets;
        private final ForcedChunksSavedData saveData;
        private final String modId;

        private TicketHelper(ForcedChunksSavedData saveData, String modId, Map<BlockPos, Pair<LongSet, LongSet>> blockTickets, Map<UUID, Pair<LongSet, LongSet>> entityTickets)
        {
            this.saveData = saveData;
            this.modId = modId;
            this.blockTickets = blockTickets;
            this.entityTickets = entityTickets;
        }

        /**
         * Gets all "BLOCK" tickets this mod had registered and which block positions are forcing which chunks. First element of the pair is the non-fully ticking
         * tickets, second element is the fully ticking tickets.
         *
         * @apiNote This map is unmodifiable and does not update to reflect removed tickets so it is safe to call the remove methods while iterating it.
         */
        public Map<BlockPos, Pair<LongSet, LongSet>> getBlockTickets()
        {
            return blockTickets;
        }

        /**
         * Gets all "ENTITY" tickets this mod had registered and which entity (UUID) is forcing which chunks. First element of the pair is the non-fully ticking
         * tickets, second element is the fully ticking tickets.
         *
         * @apiNote This map is unmodifiable and does not update to reflect removed tickets so it is safe to call the remove methods while iterating it.
         */
        public Map<UUID, Pair<LongSet, LongSet>> getEntityTickets()
        {
            return entityTickets;
        }

        /**
         * Removes all tickets that a given block was responsible for; both ticking and not ticking.
         *
         * @param owner Block that was responsible.
         */
        public void removeAllTickets(BlockPos owner)
        {
            removeAllTickets(saveData.getBlockForcedChunks(), owner);
        }

        /**
         * Removes all tickets that a given entity (UUID) was responsible for; both ticking and not ticking.
         *
         * @param owner Entity (UUID) that was responsible.
         */
        public void removeAllTickets(UUID owner)
        {
            removeAllTickets(saveData.getEntityForcedChunks(), owner);
        }

        /**
         * Removes all tickets that a given owner was responsible for; both ticking and not ticking.
         */
        private <T extends Comparable<? super T>> void removeAllTickets(PortingLibChunkManager.TicketTracker<T> tickets, T owner)
        {
            TicketOwner<T> ticketOwner = new TicketOwner<>(modId, owner);
            if (tickets.getChunks().containsKey(ticketOwner) || tickets.getTickingChunks().containsKey(ticketOwner))
            {
                ((TicketTrackerAccessor<T>) tickets).kilt$getChunks().remove(ticketOwner);
                ((TicketTrackerAccessor<T>) tickets).kilt$getTickingChunks().remove(ticketOwner);
                saveData.setDirty(true);
            }
        }

        /**
         * Removes the ticket for the given chunk that a given block was responsible for.
         *
         * @param owner   Block that was responsible.
         * @param chunk   Chunk to remove ticket of.
         * @param ticking Whether or not the ticket to remove represents a ticking set of tickets or not.
         */
        public void removeTicket(BlockPos owner, long chunk, boolean ticking)
        {
            removeTicket(saveData.getBlockForcedChunks(), owner, chunk, ticking);
        }

        /**
         * Removes the ticket for the given chunk that a given entity (UUID) was responsible for.
         *
         * @param owner   Entity (UUID) that was responsible.
         * @param chunk   Chunk to remove ticket of.
         * @param ticking Whether or not the ticket to remove represents a ticking set of tickets or not.
         */
        public void removeTicket(UUID owner, long chunk, boolean ticking)
        {
            removeTicket(saveData.getEntityForcedChunks(), owner, chunk, ticking);
        }

        private <T extends Comparable<? super T>> void removeTicket(PortingLibChunkManager.TicketTracker<T> tickets, T owner, long chunk, boolean ticking)
        {
            if (((TicketTrackerAccessor<T>) tickets).callRemove(new TicketOwner<>(modId, owner).toFabric(), chunk, ticking))
                saveData.setDirty(true);
        }
    }

    /**
     * Helper class to keep track of a ticket owner by modid and owner object
     */
    public static class TicketOwner<T extends Comparable<? super T>> implements Comparable<TicketOwner<T>>
    {
        private final String modId;
        private final T owner;

        private TicketOwner(String modId, T owner)
        {
            this.modId = modId;
            this.owner = owner;
        }

        public PortingLibChunkManager.TicketOwner<T> toFabric() {
            return TicketOwnerAccessor.createTicketOwner(modId, owner);
        }

        @Override
        public int compareTo(TicketOwner<T> other)
        {
            int res = modId.compareTo(other.modId);
            return res == 0 ? owner.compareTo(other.owner) : res;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TicketOwner<?> that = (TicketOwner<?>) o;
            return Objects.equals(modId, that.modId) && Objects.equals(owner, that.owner);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(modId, owner);
        }
    }

    /**
     * Helper class to manage tracking and handling loaded tickets.
     */
    public static class TicketTracker<T extends Comparable<? super T>>
    {
        private final Map<TicketOwner<T>, LongSet> chunks = new HashMap<>();
        private final Map<TicketOwner<T>, LongSet> tickingChunks = new HashMap<>();

        // This is expensive, so let's pray.
        public PortingLibChunkManager.TicketTracker<T> toFabric() {
            var tracker = new PortingLibChunkManager.TicketTracker<T>();
            var accessor = ((TicketTrackerAccessor<T>) tracker);
            this.chunks.forEach((owner, set) -> {
                accessor.kilt$getChunks().put(owner.toFabric(), set);
            });
            this.tickingChunks.forEach((owner, set) -> {
                accessor.kilt$getTickingChunks().put(owner.toFabric(), set);
            });

            return tracker;
        }

        /**
         * Gets an unmodifiable view of the tracked chunks.
         */
        public Map<TicketOwner<T>, LongSet> getChunks()
        {
            return Collections.unmodifiableMap(chunks);
        }

        /**
         * Gets an unmodifiable view of the tracked fully ticking chunks.
         */
        public Map<TicketOwner<T>, LongSet> getTickingChunks()
        {
            return Collections.unmodifiableMap(tickingChunks);
        }

        /**
         * Checks if this tracker is empty.
         *
         * @return {@code true} if there are no chunks or ticking chunks being tracked.
         */
        public boolean isEmpty()
        {
            return chunks.isEmpty() && tickingChunks.isEmpty();
        }

        private Map<TicketOwner<T>, LongSet> getTickets(boolean ticking)
        {
            return ticking ? tickingChunks : chunks;
        }

        /**
         * @return {@code true} if the state changed.
         */
        private boolean remove(TicketOwner<T> owner, long chunk, boolean ticking)
        {
            Map<TicketOwner<T>, LongSet> tickets = getTickets(ticking);
            if (tickets.containsKey(owner))
            {
                LongSet ticketChunks = tickets.get(owner);
                if (ticketChunks.remove(chunk))
                {
                    if (ticketChunks.isEmpty())
                        tickets.remove(owner);
                    return true;
                }
            }
            return false;
        }

        /**
         * @return {@code true} if the state changed.
         */
        private boolean add(TicketOwner<T> owner, long chunk, boolean ticking)
        {
            return getTickets(ticking).computeIfAbsent(owner, o -> new LongOpenHashSet()).add(chunk);
        }
    }
}
