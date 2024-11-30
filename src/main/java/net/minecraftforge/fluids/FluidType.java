/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fluids;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.mixin.FluidTypeAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A definition of common attributes, properties, and methods that is applied
 * to a {@link Fluid}. This is used to link a flowing and source fluid together
 * without relying on tags. Most accessors do not correlate to in-game features;
 * they are provided for mods to take advantage of.
 *
 * <p>Accessors are typically implemented in a method call chain. As such, it
 * can provide a general implementation while more specific implementations
 * can be implemented by overriding methods further in the call chain (on fluids,
 * entities, etc.).
 */
public class FluidType extends io.github.fabricators_of_create.porting_lib.fluids.FluidType
{
    /**
     * The number of fluid units that a bucket represents.
     */
    public static final int BUCKET_VOLUME = 1000;

    /**
     * A lazy value which computes the number of fluid types within the
     * registry.
     */
    public static final Lazy<Integer> SIZE = Lazy.of(() -> ForgeRegistries.FLUID_TYPES.get().getKeys().size());

    private static final Map<io.github.fabricators_of_create.porting_lib.fluids.FluidType, FluidType> kilt$wrappedFluidTypes = new ConcurrentHashMap<>();

    public static FluidType kilt$tryGetWrappingFluidType(io.github.fabricators_of_create.porting_lib.fluids.FluidType fabricFluidType) {
        if (fabricFluidType instanceof FluidType forgeFluidType) // just in case.
            return forgeFluidType;

        return kilt$wrappedFluidTypes.computeIfAbsent(fabricFluidType, FluidType::new);
    }

    private io.github.fabricators_of_create.porting_lib.fluids.FluidType kilt$wrapped;

    // Kilt: Wrap around the existing Porting Lib fluid type if possible
    private FluidType(io.github.fabricators_of_create.porting_lib.fluids.FluidType wrapped)
    {
        super(io.github.fabricators_of_create.porting_lib.fluids.FluidType.Properties.create()
            .descriptionId(wrapped.getDescriptionId())
            .motionScale(((FluidTypeAccessor) wrapped).getMotionScale())
            .canPushEntity(((FluidTypeAccessor) wrapped).isCanPushEntity())
            .canSwim(((FluidTypeAccessor) wrapped).isCanSwim())
            .canDrown(((FluidTypeAccessor) wrapped).isCanDrown())
            .fallDistanceModifier(((FluidTypeAccessor) wrapped).getFallDistanceModifier())
            .canExtinguish(((FluidTypeAccessor) wrapped).isCanExtinguish())
            .canConvertToSource(((FluidTypeAccessor) wrapped).isCanConvertToSource())
            .supportsBoating(((FluidTypeAccessor) wrapped).isSupportsBoating())
            .pathType(((FluidTypeAccessor) wrapped).getPathType())
            .adjacentPathType(((FluidTypeAccessor) wrapped).getAdjacentPathType())
            .canHydrate(((FluidTypeAccessor) wrapped).isCanHydrate())
            .lightLevel(((FluidTypeAccessor) wrapped).getLightLevel())
            .density(((FluidTypeAccessor) wrapped).getDensity())
            .temperature(((FluidTypeAccessor) wrapped).getTemperature())
            .viscosity(((FluidTypeAccessor) wrapped).getViscosity())
            .rarity(((FluidTypeAccessor) wrapped).getRarity())
        );

        this.kilt$wrapped = wrapped;
        this.initClient();
    }

    /**
     * Default constructor.
     *
     * @param properties the general properties of the fluid type
     */
    public FluidType(final Properties properties)
    {
        super(io.github.fabricators_of_create.porting_lib.fluids.FluidType.Properties.create()
            .descriptionId(properties.descriptionId)
            .motionScale(properties.motionScale)
            .canPushEntity(properties.canPushEntity)
            .canSwim(properties.canSwim)
            .canDrown(properties.canDrown)
            .fallDistanceModifier(properties.fallDistanceModifier)
            .canExtinguish(properties.canExtinguish)
            .canConvertToSource(properties.canConvertToSource)
            .supportsBoating(properties.supportsBoating)
            .pathType(properties.pathType)
            .adjacentPathType(properties.adjacentPathType)
            .canHydrate(properties.canHydrate)
            .lightLevel(properties.lightLevel)
            .density(properties.density)
            .temperature(properties.temperature)
            .viscosity(properties.viscosity)
            .rarity(properties.rarity)
        );

        this.initClient();
    }

    /**
     * Returns the identifier representing the name of the fluid type.
     * If no identifier was specified, then the identifier will be defaulted
     * to {@code fluid_type.<modid>.<registry_name>}.
     *
     * @return the identifier representing the name of the fluid type
     */
    public String getDescriptionId()
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getDescriptionId();

        if (((FluidTypeAccessor) this).kilt$getDescriptionId() == null)
            ((FluidTypeAccessor) this).kilt$setDescriptionId(Util.makeDescriptionId("fluid_type", ForgeRegistries.FLUID_TYPES.get().getKey(this)));

        return ((FluidTypeAccessor) this).kilt$getDescriptionId();
    }


    /**
     * Returns a sound to play when a certain action is performed. If no
     * sound is present, then the sound will be {@code null}.
     *
     * @param action the action being performed
     * @return the sound to play when performing the action
     */
    @Nullable
    public SoundEvent getSound(SoundAction action) {
        if (kilt$wrapped != null)
            return kilt$wrapped.getSound(action.asFabric());

        return this.sounds.get(action.asFabric());
    }

    /**
     * Returns whether the boat can be used on the fluid.
     *
     * @param state the state of the fluid
     * @param boat the boat trying to be used on the fluid
     * @return {@code true} if the boat can be used, {@code false} otherwise
     */
    public boolean supportsBoating(FluidState state, Boat boat)
    {
        return this.supportsBoating(boat);
    }

    /**
     * When {@code false}, the fluid will no longer update its height value while
     * within a boat while it is not within a fluid ({@link Boat#isUnderWater()}.
     *
     * @param state the state of the fluid the rider is within
     * @param boat the boat the rider is within that is not inside a fluid
     * @param rider the rider of the boat
     * @return {@code true} if the fluid height should be updated, {@code false} otherwise
     */
    public boolean shouldUpdateWhileBoating(FluidState state, Boat boat, Entity rider)
    {
        return !this.supportsBoating(state, boat);
    }

    /**
     * Returns whether the entity can ride in this vehicle under the fluid.
     *
     * @param vehicle the vehicle being ridden in
     * @param rider the entity riding the vehicle
     * @return {@code true} if the vehicle can be ridden in under this fluid,
     *         {@code false} otherwise
     */
    public boolean canRideVehicleUnder(Entity vehicle, Entity rider)
    {
        if (this == ForgeMod.WATER_TYPE.get()) return !vehicle.dismountsUnderwater();
        return true;
    }

    /**
     * Returns whether the entity can be hydrated by this fluid.
     *
     * <p>Hydration is an arbitrary word which depends on the entity.
     *
     * @param entity the entity in the fluid
     * @return {@code true} if the entity can be hydrated, {@code false}
     *         otherwise
     */
    public boolean canHydrate(Entity entity)
    {
        return ((FluidTypeAccessor) this).isCanHydrate();
    }

    /**
     * Returns a sound to play when a certain action is performed by the
     * entity in the fluid. If no sound is present, then the sound will be
     * {@code null}.
     *
     * @param entity the entity in the fluid
     * @param action the action being performed
     * @return the sound to play when performing the action
     */
    @Nullable
    public SoundEvent getSound(Entity entity, SoundAction action)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getSound(entity, action.asFabric());

        return this.getSound(entity, action.asFabric());
    }

    /**
     * Returns a sound to play when a certain action is performed at a
     * position. If no sound is present, then the sound will be {@code null}.
     *
     * @param player the player listening to the sound
     * @param getter the getter which can get the fluid
     * @param pos the position of the fluid
     * @param action the action being performed
     * @return the sound to play when performing the action
     */
    @Nullable
    public SoundEvent getSound(@Nullable Player player, BlockGetter getter, BlockPos pos, SoundAction action)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getSound(player, getter, pos, action.asFabric());

        return this.getSound(player, getter, pos, action.asFabric());
    }

    /* Stack-Based Accessors */

    /**
     * Returns whether the fluid can create a source.
     *
     * @param stack the stack holding the fluid
     * @return {@code true} if the fluid can create a source, {@code false} otherwise
     */
    public boolean canConvertToSource(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.canConvertToSource(stack);

        return canConvertToSource((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns a sound to play when a certain action is performed. If no
     * sound is present, then the sound will be {@code null}.
     *
     * @param stack the stack holding the fluid
     * @param action the action being performed
     * @return the sound to play when performing the action
     */
    @Nullable
    public SoundEvent getSound(FluidStack stack, SoundAction action)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getSound(stack, action.asFabric());

        return this.getSound(action.asFabric());
    }

    /**
     * Returns the component representing the name of the fluid type.
     *
     * @param stack the stack holding the fluid
     * @return the component representing the name of the fluid type
     */
    public Component getDescription(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getDescription(stack);

        return Component.translatable(this.getDescriptionId(stack));
    }

    /**
     * Returns the identifier representing the name of the fluid.
     * If no identifier was specified, then the identifier will be defaulted
     * to {@code fluid_type.<modid>.<registry_name>}.
     *
     * @param stack the stack holding the fluid
     * @return the identifier representing the name of the fluid
     */
    public String getDescriptionId(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getDescriptionId(stack);

        return this.getDescriptionId();
    }

    /**
     * Returns whether the fluid can hydrate.
     *
     * <p>Hydration is an arbitrary word which depends on the implementation.
     *
     * @param stack the stack holding the fluid
     * @return {@code true} if the fluid can hydrate, {@code false} otherwise
     */
    public boolean canHydrate(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.canHydrate(stack);

        return this.canHydrate((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the light level emitted by the fluid.
     *
     * <p>Note: This should be a value between {@code [0,15]}. If not specified, the
     * light level is {@code 0} as most fluids do not emit light.
     *
     * @param stack the stack holding the fluid
     * @return the light level emitted by the fluid
     */
    public int getLightLevel(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getLightLevel(stack);

        return this.getLightLevel((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the density of the fluid.
     *
     * <p>Note: This is an arbitrary number. Negative or zero values indicate
     * that the fluid is lighter than air. If not specified, the density is
     * approximately equivalent to the real-life density of water in {@code kg/m^3}.
     *
     * @param stack the stack holding the fluid
     * @return the density of the fluid
     */
    public int getDensity(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getDensity(stack);

        return this.getDensity((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the temperature of the fluid.
     *
     * <p>Note: This is an arbitrary number. Higher temperature values indicate
     * that the fluid is hotter. If not specified, the temperature is approximately
     * equivalent to the real-life room temperature of water in {@code Kelvin}.
     *
     * @param stack the stack holding the fluid
     * @return the temperature of the fluid
     */
    public int getTemperature(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getTemperature(stack);

        return this.getTemperature((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the viscosity, or thickness, of the fluid.
     *
     * <p>Note: This is an arbitrary number. The value should never be negative.
     * Higher viscosity values indicate that the fluid flows more slowly. If not
     * specified, the viscosity is approximately equivalent to the real-life
     * viscosity of water in {@code m/s^2}.
     *
     * @param stack the stack holding the fluid
     * @return the viscosity of the fluid
     */
    public int getViscosity(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getViscosity(stack);

        return this.getViscosity((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the rarity of the fluid.
     *
     * <p>Note: If not specified, the rarity of the fluid is {@link Rarity#COMMON}.
     *
     * @param stack the stack holding the fluid
     * @return the rarity of the fluid
     */
    public Rarity getRarity(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getRarity(stack);

        return this.getRarity((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /* Helper Methods */

    /**
     * Returns the bucket containing the fluid.
     *
     * @param stack the stack holding the fluid
     * @return the bucket containing the fluid
     */
    public ItemStack getBucket(FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getBucket(stack);

        return getBucket((io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns the {@link FluidState} when a {@link FluidStack} is trying to
     * place it.
     *
     * @param getter the getter which can get the level data
     * @param pos the position of where the fluid is being placed
     * @param stack the stack holding the fluid
     * @return the {@link FluidState} being placed
     */
    public FluidState getStateForPlacement(BlockAndTintGetter getter, BlockPos pos, FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.getStateForPlacement(getter, pos, stack);

        return getStateForPlacement(getter, pos, (io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Returns whether the fluid can be placed in the level.
     *
     * @param getter the getter which can get the level data
     * @param pos the position of where the fluid is being placed
     * @param stack the stack holding the fluid
     * @return {@code true} if the fluid can be placed, {@code false} otherwise
     */
    public final boolean canBePlacedInLevel(BlockAndTintGetter getter, BlockPos pos, FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.canBePlacedInLevel(getter, pos, stack);

        return canBePlacedInLevel(getter, pos, (io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Determines if this fluid should be vaporized when placed into a level.
     *
     * <p>Note: Fluids that can turn lava into obsidian should vaporize within
     * the nether to preserve the intentions of vanilla.
     *
     * @param level the level the fluid is being placed in
     * @param pos the position to place the fluid at
     * @param stack the stack holding the fluid being placed
     * @return {@code true} if this fluid should be vaporized on placement, {@code false} otherwise
     *
     * @see BucketItem#emptyContents(Player, Level, BlockPos, BlockHitResult)
     */
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack)
    {
        if (kilt$wrapped != null)
            return kilt$wrapped.isVaporizedOnPlacement(level, pos, stack);

        return isVaporizedOnPlacement(level, pos, (io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    /**
     * Performs an action when a fluid can be vaporized when placed into a level.
     *
     * <p>Note: The fluid will already have been drained from the stack.
     *
     * @param player the player placing the fluid, may be {@code null} for blocks like dispensers
     * @param level the level the fluid is vaporized in
     * @param pos the position the fluid is vaporized at
     * @param stack the stack holding the fluid being vaporized
     *
     * @see BucketItem#emptyContents(Player, Level, BlockPos, BlockHitResult)
     */
    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack)
    {
        if (kilt$wrapped != null) {
            kilt$wrapped.onVaporize(player, level, pos, stack);
            return;
        }

        onVaporize(player, level, pos, (io.github.fabricators_of_create.porting_lib.fluids.FluidStack) stack);
    }

    @Override
    public String toString() {
        if (kilt$wrapped != null)
            return kilt$wrapped.toString();

        @Nullable ResourceLocation name = ForgeRegistries.FLUID_TYPES.get().getKey(this);
        return name != null ? name.toString() : "Unregistered FluidType";
    }

    private Object renderProperties;

    /*
       DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
       Call RenderProperties.get instead
     */
    public Object getRenderPropertiesInternal()
    {
        return renderProperties;
    }

    private void initClient()
    {
        // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData())
        {
            initializeClient(properties ->
            {
                if (properties == this)
                    throw new IllegalStateException("Don't extend IFluidTypeRenderProperties in your fluid type, use an anonymous class instead.");
                this.renderProperties = properties;
            });
        }
    }

    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
    }

    /**
     * The properties of the fluid. The simple forms of each property can
     * be specified while more complex logic can be overridden in the {@link FluidType}.
     */
    public static final class Properties
    {
        private String descriptionId;
        private double motionScale = 0.014D;
        private boolean canPushEntity = true;
        private boolean canSwim = true;
        private boolean canDrown = true;
        private float fallDistanceModifier = 0.5F;
        private boolean canExtinguish = false;
        private boolean canConvertToSource = false;
        private boolean supportsBoating = false;
        @Nullable
        private BlockPathTypes pathType = BlockPathTypes.WATER,
            adjacentPathType = BlockPathTypes.WATER_BORDER;
        private final Map<SoundAction, SoundEvent> sounds = new HashMap<>();
        private boolean canHydrate = false;
        private int lightLevel = 0,
            density = 1000,
            temperature = 300,
            viscosity = 1000;
        private Rarity rarity = Rarity.COMMON;

        private Properties() {}

        /**
         * Creates a new instance of the properties.
         *
         * @return the property holder instance
         */
        public static Properties create()
        {
            return new Properties();
        }

        /**
         * Sets the identifier representing the name of the fluid type.
         *
         * @param descriptionId  the identifier representing the name of the fluid type
         * @return the property holder instance
         */
        public Properties descriptionId(String descriptionId)
        {
            this.descriptionId = descriptionId;
            return this;
        }

        /**
         * Sets how much the velocity of the fluid should be scaled by.
         *
         * @param motionScale a scalar to multiply to the fluid velocity
         * @return the property holder instance
         */
        public Properties motionScale(double motionScale)
        {
            this.motionScale = motionScale;
            return this;
        }

        /**
         * Sets whether the fluid can push an entity.
         *
         * @param canPushEntity if the fluid can push an entity
         * @return the property holder instance
         */
        public Properties canPushEntity(boolean canPushEntity)
        {
            this.canPushEntity = canPushEntity;
            return this;
        }

        /**
         * Sets whether the fluid can be swum in.
         *
         * @param canSwim if the fluid can be swum in
         * @return the property holder instance
         */
        public Properties canSwim(boolean canSwim)
        {
            this.canSwim = canSwim;
            return this;
        }

        /**
         * Sets whether the fluid can drown something.
         *
         * @param canDrown if the fluid can drown something
         * @return the property holder instance
         */
        public Properties canDrown(boolean canDrown)
        {
            this.canDrown = canDrown;
            return this;
        }

        /**
         * Sets how much the fluid should scale the damage done when hitting
         * the ground per tick.
         *
         * @param fallDistanceModifier a scalar to multiply to the fall damage
         * @return the property holder instance
         */
        public Properties fallDistanceModifier(float fallDistanceModifier)
        {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        /**
         * Sets whether the fluid can extinguish.
         *
         * @param canExtinguish if the fluid can extinguish
         * @return the property holder instance
         */
        public Properties canExtinguish(boolean canExtinguish)
        {
            this.canExtinguish = canExtinguish;
            return this;
        }

        /**
         * Sets whether the fluid can create a source.
         *
         * @param canConvertToSource if the fluid can create a source
         * @return the property holder instance
         */
        public Properties canConvertToSource(boolean canConvertToSource)
        {
            this.canConvertToSource = canConvertToSource;
            return this;
        }

        /**
         * Sets whether the fluid supports boating.
         *
         * @param supportsBoating if the fluid supports boating
         * @return the property holder instance
         */
        public Properties supportsBoating(boolean supportsBoating)
        {
            this.supportsBoating = supportsBoating;
            return this;
        }

        /**
         * Sets the path type of this fluid.
         *
         * @param pathType the path type of this fluid
         * @return the property holder instance
         */
        public Properties pathType(@Nullable BlockPathTypes pathType)
        {
            this.pathType = pathType;
            return this;
        }

        /**
         * Sets the path type of the adjacent fluid. Path types with a negative
         * malus are not traversable. Pathfinding will favor paths consisting of
         * a lower malus.
         *
         * @param adjacentPathType the path type of this fluid
         * @return the property holder instance
         */
        public Properties adjacentPathType(@Nullable BlockPathTypes adjacentPathType)
        {
            this.adjacentPathType = adjacentPathType;
            return this;
        }

        /**
         * Sets a sound to play when a certain action is performed.
         *
         * @param action the action being performed
         * @param sound the sound to play when performing the action
         * @return the property holder instance
         */
        public Properties sound(SoundAction action, SoundEvent sound)
        {
            this.sounds.put(action, sound);
            return this;
        }

        /**
         * Sets whether the fluid can hydrate.
         *
         * <p>Hydration is an arbitrary word which depends on the implementation.
         *
         * @param canHydrate if the fluid can hydrate
         * @return the property holder instance
         */
        public Properties canHydrate(boolean canHydrate)
        {
            this.canHydrate = canHydrate;
            return this;
        }

        /**
         * Sets the light level emitted by the fluid.
         *
         * @param lightLevel the light level emitted by the fluid
         * @return the property holder instance
         * @throws IllegalArgumentException if light level is not between [0,15]
         */
        public Properties lightLevel(int lightLevel)
        {
            if (lightLevel < 0 || lightLevel > 15)
                throw new IllegalArgumentException("The light level should be between [0,15].");
            this.lightLevel = lightLevel;
            return this;
        }

        /**
         * Sets the density of the fluid.
         *
         * @param density the density of the fluid
         * @return the property holder instance
         */
        public Properties density(int density)
        {
            this.density = density;
            return this;
        }

        /**
         * Sets the temperature of the fluid.
         *
         * @param temperature the temperature of the fluid
         * @return the property holder instance
         */
        public Properties temperature(int temperature)
        {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the viscosity, or thickness, of the fluid.
         *
         * @param viscosity the viscosity of the fluid
         * @return the property holder instance
         * @throws IllegalArgumentException if viscosity is negative
         */
        public Properties viscosity(int viscosity)
        {
            if (viscosity < 0)
                throw new IllegalArgumentException("The viscosity should never be negative.");
            this.viscosity = viscosity;
            return this;
        }

        /**
         * Sets the rarity of the fluid.
         *
         * @param rarity the rarity of the fluid
         * @return the property holder instance
         */
        public Properties rarity(Rarity rarity)
        {
            this.rarity = rarity;
            return this;
        }
    }
}